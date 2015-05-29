/* 
 * polymap.org
 * Copyright (C) 2015 Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.jdbc.AutoGeneratedPrimaryKeyColumn;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.PrimaryKeyFinder;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.PlainLazyInit;

import org.polymap.rhei.fulltext.FullQueryProposalDecorator;
import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.indexing.LowerCaseTokenFilter;
import org.polymap.rhei.fulltext.store.lucene.LuceneFulltextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFulltextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFulltextIndex.Updater;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.model2.Composite;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.runtime.CompositeInfo;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.geotools.FeatureStoreAdapter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AlkisRepository {

    private static Log log = LogFactory.getLog( AlkisRepository.class );
    
    public static final String              DB_NAME = "WBV";
    
    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );
    
    /**
     * The global instance, lazily initialized.
     */
    public static final Supplier<AlkisRepository> instance = new LockedLazyInit( () -> new AlkisRepository() );
    
    
    // instance *******************************************

    private EntityRepository                repo;

    private LuceneFulltextIndex             fulltextIndex;

    private DataStore                       ds;
    
    public Lazy<Map<String,AX_Gemarkung>>   gemarkungen = new PlainLazyInit( () -> {
        return newUnitOfWork().query( AX_Gemarkung.class ).execute().stream()
                // gemarkungen kommen leider (in Wittenberg) doppelt vor
                .collect( Collectors.toMap( g -> g.gemarkungsnummer.get(), g -> g, (g1,g2) -> g1 ) );
    });
    
    public Lazy<Map<String,AX_Gemeinde>>    gemeinden = new PlainLazyInit( () -> {
        return newUnitOfWork().query( AX_Gemeinde.class ).execute().stream()
                .collect( Collectors.toMap( g -> g.gemeindenummer.get(), g -> g, (g1,g2) -> g1 ) );
    });
    
    
    /**
     * Configure and initializing the one and only global instance.
     */
    private AlkisRepository() {
        try {
            log.info( "Assembling repository..." );
            
//            Logging.GEOTOOLS.setLoggerFactory( "org.geotools.util.logging.CommonsLoggerFactory" );
            
            //
            BooleanQuery.setMaxClauseCount( 4 * 1024 );
            log.info( "Maximale Anzahl Lucene-Klauseln erh�ht auf: " + BooleanQuery.getMaxClauseCount() );
            
            // init fulltext
            File dataDir = new File( "/tmp"/*Polymap.getDataDir()*/, AlkisPlugin.ID );
            fulltextIndex = new LuceneFulltextIndex( new File( dataDir, "fulltext" ) );
            fulltextIndex.addTokenFilter( new LowerCaseTokenFilter() );
            
//            WaldbesitzerFulltextTransformer wbTransformer = new WaldbesitzerFulltextTransformer();
//            wbTransformer.setHonorQueryableAnnotation( true );

            // store
            Map<String,Object> params = new HashMap<String,Object>();
            params.put( "dbtype", "postgis");
            params.put( "host", "localhost");
            params.put( "port", 5432);
            params.put( "schema", "public");
            params.put( "database", "ALKIS");
            params.put( "user", "postgres");
            params.put( "passwd", "postgres");
            ds = DataStoreFinder.getDataStore( params );

            // primary key: gml_id
            ((JDBCDataStore)ds).setPrimaryKeyFinder( new PrimaryKeyFinder() {
                @Override
                public PrimaryKey getPrimaryKey( JDBCDataStore store, String schema, String table, Connection cx ) throws SQLException {
                    // Alkis_Beziehungen
                    if (table.equals( Alkis_Beziehungen.TYPE.info().getNameInStore() )) {
                        AutoGeneratedPrimaryKeyColumn col = new AutoGeneratedPrimaryKeyColumn( "ogc_fid", String.class );
                        return new PrimaryKey( table, Collections.singletonList( col ) );                        
                    }
                    // AX_*
                    else {
                        AutoGeneratedPrimaryKeyColumn col = new AutoGeneratedPrimaryKeyColumn( "gml_id", String.class );
                        return new PrimaryKey( table, Collections.singletonList( col ) );
                    }
                }
            });

            FeatureStoreAdapter store = new FeatureStoreAdapter( ds ).createOrUpdateSchemas.put( false );
            
            // repo
            repo = EntityRepository.newConfiguration()
                    .entities.set( new Class[] {
                            AX_Flurstueck.class,
                            AX_Buchungsstelle.class,
                            AX_Buchungsblatt.class,
                            AX_Person.class,
                            AX_Anschrift.class,
                            AX_LagebezeichnungMitHausnummer.class,
                            AX_Namensnummer.class,
                            AX_Gemarkung.class,
                            Alkis_Beziehungen.class} )
                    .store.set( 
                            //new FulltextIndexer( fulltextIndex, new TypeFilter( Waldbesitzer.class ), newArrayList( wbTransformer ),
                            store ) //)
                    .create();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public EntityRepository repo() {
        return repo;
    }

    
    public FulltextIndex fulltextIndex() {
        return new FullQueryProposalDecorator(
               new LowerCaseTokenFilter( fulltextIndex ) );
    }


    public <T extends Composite> CompositeInfo<T> infoOf( Class<T> compositeClass ) {
        return repo.infoOf( compositeClass );
    }


    public UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }

    
    public void updateFulltextIndex() {
        Updater updater = ((UpdateableFulltextIndex)fulltextIndex).prepareUpdate();
    }
    
    
    /**
     * Simple access test.
     */
    public static void main( String[] args ) throws Exception {
        AlkisRepository r = new AlkisRepository();
        
        // type names
        String[] typeNames = r.ds.getTypeNames();
        println( "TypeNames:", Arrays.asList( typeNames ) );
        
        println( "\n", AX_Flurstueck.class.getSimpleName(), " =============================================" );
        println( r.ds.getSchema( AX_Flurstueck.TYPE.info().getNameInStore() ) );
        
        //
        UnitOfWork uow = r.newUnitOfWork();
        
//        // Gemarkungen pr�fen
//        Map<String,AX_Gemarkung> gems = new HashMap();
//        uow.query( AX_Gemarkung.class ).execute().stream().forEach( g -> {
//            println( "Gemarkung: " + g.gemarkungsnummer.get() + " - " + g.bezeichnung.get() );
//            if (gems.put( g.gemarkungsnummer.get(), g ) != null) {
//                println( "   !!!" );
//            }
//        });

        ResultSet<AX_Flurstueck> rs = uow.query( AX_Flurstueck.class ).maxResults( 10 ).execute();
        for (AX_Flurstueck fst : rs) {
//            EntityHierachyPrinter.on( fst, (entity,assocname,assocType) -> {
//                return true;
//            }).run();
            
            println( "Gemarkung: " + fst.gemarkung().bezeichnung.get() );
            println( "Gemeinde: " + fst.gemeinde().bezeichnung.get() );
            println( new FlurstueckFulltext( fst ).transform().toString( 4 ) );
        }
        
//        // id query
//        AX_Flurstueck fst = uow.entity( AX_Flurstueck.class, "DESTLIKA0002XWdg" );
//        println( "-> " + fst.lagebezeichnung.get() );
        
//        println( "\n", AX_Person.class.getSimpleName(), " =============================================" );
//        uow.query( AX_Person.class ).maxResults( 1 ).execute().stream().forEach( person -> {
//            EntityHierachyPrinter.on( person, (entity,assocname,assocType) -> {
//                return true;
//            }).run();
//        });

//        println( "\nAX_Namensnummer =============================================" );
//        uow.query( AX_Namensnummer.class ).maxResults( 1 ).execute().stream().forEach( nn -> {
//            EntityHierachyPrinter.on( nn, (entity,assocname,assocType) -> {
//                return true;
//            }).run();
//        });

//        println( "\nAX_Buchungsstelle ===========================================" );
//        uow.query( AX_Buchungsstelle.class ).maxResults( 1 ).execute().stream().forEach( bs -> {
//            EntityHierachyPrinter.on( bs, (entity,assocname,assocType) -> {
//                return true;
//            }).run();
//        });

//        println( "\nAX_Buchungsblatt ===========================================" );
//        uow.query( AX_Buchungsblatt.class ).maxResults( 10 ).execute().stream().forEach( bb -> {
//            EntityHierachyPrinter.on( bb, (entity,assocname,assocType) -> {
//                return true;
//            }).run();
//        });
        
//      AX_Namensnummer nn = uow.entity( AX_Namensnummer.class, "DESTLIKA00028H3S" );
//      EntityHierachyPrinter.on( nn, (entity,assocname,assocType) -> {
//          return true;
//      }).run();
        
    }
    
    
    protected static void println( Object... parts ) {
        print( parts );
        System.out.println( "" );
    }

    
    protected static void print( Object... parts ) {
        Arrays.asList( parts ).stream().forEach( part -> { 
            System.out.print( part != null ? part.toString() : "[null]" );
        });
    }
    
}
