/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.h2.H2DataStoreFactory;
import org.geotools.jdbc.JDBCDataStoreFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.EntityRepositoryConfiguration;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;

import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.importer.alkis1.Alkis1Importer;
import org.polymap.alkis.importer.alkis1.GmkImporter;
import org.polymap.alkis.importer.alkis1.NutzungenImporter;
import org.polymap.alkis.model.alb.ALBRepository;
import org.polymap.alkis.model.alb.Abschnitt;
import org.polymap.alkis.model.alb.Flurstueck;
import org.polymap.alkis.model.alb.Gemarkung;
import org.polymap.alkis.model.alb.Lagehinweis2;
import org.polymap.alkis.model.alb.Nutzungsart;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Alkis1ImportTest
        extends TestCase {

    private static Log log = LogFactory.getLog( Alkis1ImportTest.class );

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.alkis.importer.alkis1", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore.lucene", "trace" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2.store.feature", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.jdbc", "trace" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data", "trace" );
    }
    
    protected ALBRepository         repo;
    

    public Alkis1ImportTest( String name ) {
        super( name );
    }

    
    protected void setUp() throws Exception {
        super.setUp();
        // H2
        Map<String,Serializable> params = new HashMap();
        params.put( H2DataStoreFactory.DBTYPE.key, "h2" );
        params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.polymap.org/" + "alkis1" );
        params.put( H2DataStoreFactory.DATABASE.key, "alkis1" );
        params.put( H2DataStoreFactory.USER.key, "polymap" );

        // clear database
        File baseDir = new File( "/tmp/Alkis1ImportTest/" );
        FileUtils.deleteDirectory( baseDir );
        
        // create ds
        H2DataStoreFactory factory = new H2DataStoreFactory();
        factory.setBaseDirectory( baseDir );
        DataStore ds = factory.createDataStore( params );
        
        // create repo
        EntityRepositoryConfiguration repoConfig = EntityRepository.newConfiguration()
                .setEntities( new Class[] {
                        Flurstueck.class, 
                        Abschnitt.class, 
                        Gemarkung.class, 
                        Lagehinweis2.class,
                        Nutzungsart.class } )
                .setStore( new FeatureStoreAdapter( ds ) );
        repo = new ALBRepository( repoConfig );
    }

    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    public void testMusterdaten() throws Exception {
        // EDBS
        FileInputStream in = new FileInputStream( new File(
               "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/edbs.ALK_Muster_EDBS_BSPE.dbout.1.001" ) );
//       
//        EdbsImporter importer = new EdbsImporter( (DataStore)repo.ds, 
//                new InputStreamReader( in, "ISO-8859-1" ), 
//                new PrintStream( System.out, false, "ISO-8859-1" ) );
//        importer.run();

        // Nutzungen
        in = new FileInputStream( new File( 
                "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/Schluessel_Nutzungen_0706.csv" ) );
        ReportLog report = new ReportLog( System.out );
        new NutzungenImporter( in, report, repo ).run();
        in.close();
        
        // check Nutzung
        UnitOfWork uow = repo.newUnitOfWork();
        for (Nutzungsart nutzungsart : uow.find( Nutzungsart.class )) {
            assertNotNull( nutzungsart.id.get() );
            assertNotNull( nutzungsart.nutzung.get() );
        }
        
        // Gemarkungen
        in = new FileInputStream( new File( 
                "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/gmk_sachsen.csv" ) );
        report = new ReportLog( System.out );
        new GmkImporter( in, report, repo ).run();
        in.close();
        
        // check Gemarkungen
        uow = repo.newUnitOfWork();
        for (Gemarkung gemarkung : uow.find( Gemarkung.class )) {
            assertNotNull( gemarkung.gemarkung.get() );
            assertNotNull( gemarkung.gemeinde.get() );
        }
        
        // ALKIS1
        in = new FileInputStream( new File( 
                "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/ALKIS1_Erstdaten.zip" ) );
                //"/home/falko/Data/mittelsachen-alkis/ALB_Gemarkung_2xxx.zip" ) );
        report = new ReportLog( System.out );
        new Alkis1Importer( in, report, repo ).run();
        in.close();
        
        // check Flurstuecke
        uow = repo.newUnitOfWork();
        // FIXME "object already closed" when iterating directly over result :(
        Collection<Flurstueck> flurstuecke = new ArrayList( uow.find( Flurstueck.class ) );
        for (Flurstueck flurstueck : flurstuecke) {
            assertTrue( flurstueck.id.get().startsWith( "flurstueck" ) );
            
            // Lagehinweise
            Collection<Lagehinweis2> lagehinweise = flurstueck.lagehinweise();
            for (Lagehinweis2 hinweis : lagehinweise) {
                log.info( "    Hinweis: " + hinweis );
            }
            assertTrue( lagehinweise.size() >= 0 && lagehinweise.size() < 100 );
            
            // Abschnitte
            for (Abschnitt abschnitt : flurstueck.abschnitte() ) {
                log.info( "    Abschnitt: " + abschnitt );
                log.info( "        Nutzungsart: " + abschnitt.nutzungsart() );
                //assertNotNull( abschnitt.nutzungsart() );
            }
        }
    }
    
    
    public void testFParser() throws Exception {
        TestAlkis1Importer importer = new TestAlkis1Importer( repo );
        
        //String line = "F 9874167    1   a  1$$A$$4167-22$$$$8600#5630###$5630$$$";
        //String line = "F1101    1   0  0$     $A$0$Korr-FoVN    $             $13$Fleischerpl. 1, Markt 1$199#   1530###$     1530$$$N47,398,956,1007,1081";    
        //String line = "F1101   0    1   0  0$457114834#560544292$A$$$$$Fleischerplatz 1#Markt 1$1990#1530###$1530$$$";
        String line = "F1101   0   53   0  0$457124948#560553068$A$$$$$Obere Schmiedegasse 11$1990#270####6300#140###$410$$$";
        log.info( "Line: " + line );
        
        Flurstueck flurstueck = importer.parseFLine( line );
        assertEquals( "Obere Schmiedegasse 11", flurstueck.strasse.get() );
    }
    
    
    /**
     * Just give access to the inner classes of {@link Alkis1Importer}.  
     */
    class TestAlkis1Importer
            extends Alkis1Importer {

        public TestAlkis1Importer( ALBRepository repo ) throws IOException {
            super( null, null, repo );
        }
        
        public Flurstueck parseFLine( String line ) throws IOException {
            return new FParser().parseLine( line );
        }
    }
    
}
