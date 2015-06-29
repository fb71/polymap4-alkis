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

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.geotools.data.DataStore;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.jdbc.AutoGeneratedPrimaryKeyColumn;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.PrimaryKeyFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.Identifier;

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

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.model.fulltext.FlurstueckTransformer;
import org.polymap.alkis.model.fulltext.FlurstueckUpdater;
import org.polymap.model2.Composite;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.runtime.CompositeInfo;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.geotools.FeatureStoreAdapter;
import org.polymap.model2.store.geotools.FilterWrapper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AlkisRepository {

    private static Log log = LogFactory.getLog( AlkisRepository.class );
    
    public static final String              DB_NAME = "ALKIS";
    
    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );

    /** Maximale Gr��e der Ergebnismenge f�r Volltextsuche und Anzeige in Tabelle. */
    public static final int                 MAX_RESULTS = Integer.parseInt( System.getProperty( "org.polymap.alkis.maxResults", "1000" ) );
    

    /**
     * Initialize global things.
     */
    public static void init() {
        instance.get();
    }
    
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
    
    public Lazy<Map<String,AX_LagebezeichnungKatalog>> lageKatalog = new PlainLazyInit( () -> {
        return newUnitOfWork().query( AX_LagebezeichnungKatalog.class ).execute().stream()
                .collect( Collectors.toMap( e -> e.lage.get(), e -> e, (e1,e2) -> e1 ) );
    });
    
    
    /**
     * Configure and initializing the one and only global instance.
     */
    private AlkisRepository() {
        try {
            log.info( "Assembling repository..." );
            
//            Logging.GEOTOOLS.setLoggerFactory( "org.geotools.util.logging.CommonsLoggerFactory" );
            
            //
            BooleanQuery.setMaxClauseCount( MAX_RESULTS*2 );
            log.info( "Maximale Anzahl Lucene-Klauseln erh�ht auf: " + BooleanQuery.getMaxClauseCount() );
            
            // init fulltext
            File dataDir = new File( "/tmp"/*Polymap.getDataDir()*/, AlkisPlugin.ID );
            fulltextIndex = new LuceneFulltextIndex( new File( dataDir, "fulltext" ) );
            fulltextIndex.addTokenFilter( new LowerCaseTokenFilter() );
            
            // store
            Map<String,Object> params = new HashMap<String,Object>();
            params.put( "dbtype", "postgis" );
            params.put( "host", "localhost" );
            params.put( "port", 5432 );
            params.put( "schema", "public" );
            params.put( "database", "ALKIS" );
            params.put( "user", "postgres" );
            params.put( "passwd", "postgres" );
            params.put( "max connections", 25 );
            ds = new PostgisNGDataStoreFactory().createDataStore( params );

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
                            AX_LagebezeichnungOhneHausnummer.class,
                            AX_LagebezeichnungKatalog.class,
                            AX_Namensnummer.class,
                            AX_Gemarkung.class,
                            AX_Gemeinde.class,
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
        FulltextIndex result = new LowerCaseTokenFilter( fulltextIndex );
        // gro�e Datenbank mit vielen W�rtern: mehr Chancen etwas zu finden
        return new FullQueryProposalDecorator( result )
                .proposalIncreaseFactor.put( 10 );
    }

    
    public void updateFulltext( int max ) throws Exception {
        log.info( "Checking fulltext index..." );
        if (fulltextIndex.isEmpty()) {
            log.info( "    Starting update..." );
            new FlurstueckUpdater( AlkisRepository.instance.get(), fulltextIndex ).max.put( max ).run();
        }
    }

    
    public Query<AX_Flurstueck> fulltextQuery( String query, int maxResults, UnitOfWork uow ) throws Exception {
        Set<Identifier> ids = new HashSet( 1024 );
        fulltextIndex.search( query, maxResults ).forEach( record -> {
            String id = substringAfterLast( record.getString( FulltextIndex.FIELD_ID ), "." );
            if (id.length() > 0) {
                ids.add( ff.featureId( id ) );
            }
            else {
                log.warn( "No FIELD_ID in record: " + record );
            }
        });
        log.info( "Filter:" + ff.id( ids ) );
        return uow.query( AX_Flurstueck.class ).where( new FilterWrapper( ff.id( ids ) ) );
    }
    
    
    public <T extends Composite> CompositeInfo<T> infoOf( Class<T> compositeClass ) {
        return repo.infoOf( compositeClass );
    }


    public UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
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

        ResultSet<AX_Flurstueck> rs = uow.query( AX_Flurstueck.class )
                //.where( new FilterWrapper( ff.) )
                .maxResults( 20 ).execute();
        for (AX_Flurstueck fst : rs) {
            //EntityHierachyPrinter.on( fst, (entity,assocname,assocType) -> true ).run();

//            fst.lagebezeichnung.get().forEach( lbz -> 
//                    EntityHierachyPrinter.on( lbz, (entity,assocname,assocType) -> true ).run() );
//    
//            fst.lagebezeichnungOhne.get().forEach( lbz -> 
//                    EntityHierachyPrinter.on( lbz, (entity,assocname,assocType) -> true ).run() );
    
            println( "JSON:" + new FlurstueckTransformer().apply( fst ).toString( 4 ) );
            
//            println( "Blattart: " + fst.buchungsstelle.get().buchungsblatt.get().blattart.get() );
//            println( "Gemarkung: " + fst.gemarkung().bezeichnung.get() );
//            println( "Gemeinde: " + fst.gemeinde().bezeichnung.get() );
        }
        
//        // id queryString
//        AX_Flurstueck fst = uow.entity( AX_Flurstueck.class, "DESTLIKA0002XWdg" );
//        println( "-> " + fst.lagebezeichnung.get() );
        
//        println( "\n", AX_LagebezeichnungMitHausnummer.class.getSimpleName(), " =============================================" );
//        uow.query( AX_LagebezeichnungMitHausnummer.class ).maxResults( 1000 ).execute().stream().forEach( lbz -> {
//            lbz.katalogeintrag().ifPresent( e -> println( "Lage-Katalog: " + e.bezeichnung.get() ) );
//        });

//      println( "\n", AX_Person.class.getSimpleName(), " =============================================" );
//      uow.query( AX_Person.class ).maxResults( 1 ).execute().stream().forEach( person -> {
//          EntityHierachyPrinter.on( person, (entity,assocname,assocType) -> {
//              return true;
//          }).run();
//      });

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
