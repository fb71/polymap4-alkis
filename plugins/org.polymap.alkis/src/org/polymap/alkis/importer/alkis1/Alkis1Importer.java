/* 
 * polymap.org
 * polymap2.fs_alb module.
 * created: 05.09.2008 by falko
 * Copyright 2008-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.alkis.importer.alkis1;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.model.alb.ALBRepository;
import org.polymap.alkis.model.alb.Abschnitt;
import org.polymap.alkis.model.alb.Lagehinweis2;
import org.polymap.alkis.model.alb.Flurstueck;

/**
 * Importer for ALKIS1 files.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 *         <li>05.09.2008: created</li>
 *         <li>18.07.2012: start adapting to POLYMAP3</li>
 */
public class Alkis1Importer
        extends Job
        implements Runnable {

    private static Log log = LogFactory.getLog( Alkis1Importer.class );

    public static final FilterFactory   ff = CommonFactoryFinder.getFilterFactory( null );
    
    private ALBRepository           repo;
    
    private UnitOfWork              uow;

    private ReportLog               report;

    private InputStream             in;


    /**
     * 
     * @param in ZIP stream input of the importer.
     * @param report
     * @param repo The repo to use, or null if {@link ALBRepository#instance()} is to
     *        be used.
     * @throws IOException
     */
    public Alkis1Importer( InputStream in, ReportLog report, ALBRepository repo )
    throws IOException {
        super( "ALKIS-Import" );
        setPriority( LONG );
        setSystem( true );

        this.report = report;
        this.in = in;
        
        try {
            this.repo = repo != null ? repo : ALBRepository.instance();
        }
        catch (Exception e) {
            report.error( "Fehler beim �ffnen der Import-Datenquelle.", e );
            throw new IOException( e );
        }
    }


//    public Alkis1Importer( FsAlbProvider provider, int fakeCount )
//    throws Exception {
//
//        // database schema
//        if (!provider.isActive()) {
//            provider.login();
//        }
//        provider.createDatabaseSchema( true, true );
//
//        // import data
//        log.info( "Fake importing into: " + provider.getProviderId() );
//        LineParser parser = new FParser( provider, conversation );
//        for (int i=0; i<fakeCount; i++) {
//            parser.parseLine( "F 9874167    1   a  1$$A$$4167-22$$$$8600#5630###$5630$$$" );
//        }
//        conversation.flush();
//        //conversation.close();
//    }


    /**
     * Import data.
     */
    protected IStatus run( IProgressMonitor monitor ) {
        try {
            ZipInputStream zip = new ZipInputStream( new BufferedInputStream( in ) );
            for (ZipEntry zipEntry = zip.getNextEntry(); zipEntry!=null; zipEntry = zip.getNextEntry()) {
                log.info( "Zip-Eintrag: " + zipEntry.getName() );

                if (zipEntry.getName().endsWith( ".f" )) {
                    log.info( "************ .f file..." );
                    parseFile( zip, new FParser() );
                }
                //    zip.closeEntry();
            }
            zip.close();
            return Status.OK_STATUS;
        }
        catch (IOException e) {
            report.error( "Fehler beim Verarbeiten des ZIP-Files: " + e, e );
            return Status.CANCEL_STATUS;
        }
    }

    
    public void run() {
        run( new NullProgressMonitor() );
    }
    

    /**
     * Parse the next entry from the given zip stream and apply the given
     * parser.
     */
    protected void parseFile( ZipInputStream zip, LineParser parser )
    throws IOException {
        log.info( "************" );
        assert uow == null;
        uow = repo.newUnitOfWork();
        try {
            LineNumberReader reader = new LineNumberReader( new InputStreamReader( zip, "ISO-8859-1" ) );
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                try {
                    log.debug( ":: " + line );
                    parser.parseLine( line );
                }
                catch (Exception e) {
                    log.warn( "Fehler beim Import: " + e.toString(), e );
                    throw new RuntimeException( e.toString(), e );
                    //exceptions.add( e );
                }
            }
            uow.commit();
        }
        catch (Exception e) {
            report.error( "Fehler beim Lesen .", e );
        }
        finally {
            uow.close();
            uow = null;
        }
    }


    /**
     * 
     */
    public interface LineParser {

        public void parseLine( String line ) throws Exception;

    }


    /**
     * The .f file parser. 
     *
     */
    class FParser
            implements LineParser {

        private int             flushCount = 0, count = 0;

        private Pattern         pattern = Pattern.compile( "(F)" // satzkennzeichen
                + "([\\s\\d]{4})"       // gemarkungsschl�ssel 
                + "([\\s\\d]{4})"       // flurst�cksschl�ssel
                + "([\\s\\d]{5})"       // z�hler
                + "([\\s\\d\\w]{4})"    // nenner 
                + "([\\s\\d]{3})"       // version 
                + "\\$" 
                + "([\\s\\d]{0,9})"     // 7: rechtswert 
                + "#?"    
                + "([\\s\\d]{0,9})"     // 8: hochswert 
                + "\\$"
                + "(\\S)"               // 9: status 
                + "\\$"
                + "([^$]*)"             // nicht belegt  
                + "\\$"
                + "([^$]*)"             // entstehung fortf�hrung
                + "\\$"
                + "([^$]*)"             // entstehung fortf�hrung
                + "\\$"
                + "([^$]*)"             // nicht belegt  
                + "\\$"
                + "([^$]*)"             // 14: lagehinweise  
                + "\\$"
                + "([^$]*)"             // 15: abschnitte  
                + "\\$"
                + "([^$]*)"             // 16: gesamtflaeche  
                + "\\$"
                + "([^$]*)"             // frei  
                + "\\$*"
                + "([^$]*)"             // 18: info  
                + "\\$*"
                + "([.\\d]*)"           // rest 
        );
        //F 9874167    1   a  1$$A$$4167-22$$$$8600#5630###$5630$$$
        //F1101   0    1   0  0$457114834#560544292$A$$$$$Fleischerplatz 1#Markt 1$1990#1530###$1530$$$";

        private Pattern         hinweisPattern = Pattern.compile( "(\\D+)(\\d*)(\\S*)" );  // (.+)(\\d*)(\\S*)

        private FeatureSource<SimpleFeatureType, SimpleFeature> flaechenFs;

        
        public FParser() {
            try {
                flaechenFs = repo.ds.getFeatureSource( new NameImpl( "ALK_Flaechen" ) );
            }
            catch (IOException e) {
                log.warn( "No ALK_Flaechen in FeatureSource!!!" );
            }
        }

        
        public void parseLine( String line )
        throws IOException {
            Matcher matcher = pattern.matcher( line );            
            matcher.find();
            Integer rw = coord( matcher.group( 7 ) );
            Integer hw = coord( matcher.group( 8 ) );
            if (log.isDebugEnabled()) {
                System.out.println( "   : " + matcher.group() );
                System.out.println( "--Satzkennzeichen: " + matcher.group( 1 ) );
                System.out.println( "--Gemarkungsschl�ssel: " + matcher.group( 2 ) );
                System.out.println( "--Flurst�cksschl�ssel: " + matcher.group( 3 ) );
                System.out.println( "--Z�hler: " + matcher.group( 4 ).trim() );
                System.out.println( "--Nenner: " + matcher.group( 5 ).trim() );
                System.out.println( "--Version: " + matcher.group( 6 ).trim() );
                System.out.println( "--Rechts/Hoch: " + rw + " / " + hw );
                System.out.println( "--Status: " + matcher.group( 9 ) );
                System.out.println( "--Fortf�hrung1: " + matcher.group( 11 ) );
                System.out.println( "--Fortf�hrung2: " + matcher.group( 12 ) );
                System.out.println( "--Lagehinweise: " + matcher.group( 14 ) );
                //parseLagehinweise( matcher.group( 14 ), null );
                System.out.println( "--Abschnitte: " + matcher.group( 15 ) );
                //parseAbschnitte( matcher.group( 15 ), null );
                System.out.println( "--Gesamtfl�che: " + matcher.group( 16 ) );
                System.out.println( "--frei: " + matcher.group( 17 ) );
                System.out.println( "--Info: " + matcher.group( 18 ) );
            }
            Flurstueck flurstueck = null;
            try {
                String id = "flurstueck." + String.valueOf( count++ );
                flurstueck = uow.newEntity( Flurstueck.class, null, null );
                flurstueck.gemarkungNr.set( matcher.group( 2 ).trim() );
                flurstueck.schluessel.set( matcher.group( 3 ).trim() );
                flurstueck.zaehler.set( matcher.group( 4 ).trim() );
                flurstueck.nenner.set( matcher.group( 5 ).trim() );
                flurstueck.rw.set( coord( matcher.group( 7 ) ) );
                flurstueck.hw.set( coord( matcher.group( 8 ) ) );
                flurstueck.status.set( matcher.group( 9 ).trim() );
                flurstueck.flaeche.set( Float.valueOf( matcher.group( 16 ) ) );
                flurstueck.lagehinweis.set( matcher.group( 14 ) );

                parseLagehinweise( matcher.group( 14 ), id );
                parseAbschnitte( matcher.group( 15 ), id );
            }
            catch (Exception e) {
                report.error( "Fehler bei Zeile: " + count, e );
            }

            // find ALK_Flaeche
            SimpleFeature flaeche = findAlkFlaeche( flurstueck.schluessel.get() );
            if (flaeche != null) {
                flurstueck.geom.set( (MultiPolygon)flaeche.getDefaultGeometry() );
            }
            
            if (flushCount++ > 1000) {
                log.info( count + ": ..." );
                //uow.apply();
                flushCount = 0;
            }
        }

        
        protected Integer coord( String s ) {
            s = s.trim();
            if (s==null || s.length()==0) {
                return Integer.valueOf( 0 );
            } else {
                return Integer.parseInt( s ) / 100;
            }
        }
        
        
        protected SimpleFeature findAlkFlaeche( String schluessel ) {
            if (flaechenFs == null) {
                return null;
            }
            PropertyIsEqualTo filter = ff.equals( ff.property( "info" ), ff.literal( schluessel ) );
            Query query = new DefaultQuery( flaechenFs.getSchema().getTypeName(), filter, 1, null, null );
            FeatureIterator<SimpleFeature> it = null;
            try {
                it = flaechenFs.getFeatures( query ).features();
                return it.hasNext() ? it.next() : null;
            }
            catch (Exception e) {
                report.warn( "Keine Fl�che gefunden f�r: " + schluessel );
                return null;
            }
            finally {
                if (it != null) {it.close();}
            }
        }

        
        protected void parseLagehinweise( String hinweise, String flurstueckId )
        throws IOException {
            StringTokenizer tokenizer = new StringTokenizer( hinweise, "#" );
            for (int i=0; tokenizer.hasMoreTokens(); i++) {
                String hinweis = tokenizer.nextToken();
                Matcher m = hinweisPattern.matcher( hinweis );
                m.find();
                if (log.isDebugEnabled()) {
                    System.out.println( "----Hinweis: " + hinweis );
                    System.out.println( "-----Match: " + m.group( 0 ) );
                    System.out.println( "-----Strasse: " + m.group( 1 ).trim() );
                    System.out.println( "-----HNr: " + m.group( 2 ).trim() );
                    System.out.println( "-----Zusatz: " + m.group( 3 ).trim() );
                }
                String id = "lagehinweis." + i + "." + flurstueckId;
                Lagehinweis2 lagehinweis = uow.newEntity( Lagehinweis2.class, id, null );
                lagehinweis.hinweis.set( hinweis );
                lagehinweis.flurstueckId.set( flurstueckId );
            }
        }

        protected void parseAbschnitte( String abschnitte, String flurstueckId )
        throws IOException {
            //String[] split = StringUtils.splitBy( abschnitte, "#" );
            StrTokenizer tokenizer = new StrTokenizer( abschnitte, "#" );
            tokenizer.setEmptyTokenAsNull( true );
            tokenizer.setIgnoreEmptyTokens( false );
            for (int i=0; tokenizer.hasNext(); i++) {
                try {
                    String nutzung = tokenizer.nextToken().substring( 0, 3 );
                    Float flaeche = Float.valueOf( tokenizer.nextToken() );
                    String dummy1 = tokenizer.hasNext() ? tokenizer.nextToken() : null;
                    String dummy2 = tokenizer.hasNext() ? tokenizer.nextToken() : null;
                    String dummy3 = tokenizer.hasNext() ? tokenizer.nextToken() : null;
                    if (log.isDebugEnabled()) {
                        System.out.println( "----Nutzung: " + nutzung );
                        System.out.println( "----Fl�che: " + flaeche );
                        System.out.println( "----dummy1: " + dummy1 );
                    }
                    String id = "abschnitt." + i + "." + flurstueckId;
                    Abschnitt abschnitt = uow.newEntity( Abschnitt.class, id, null );
                    abschnitt.flaeche.set( flaeche );
                    abschnitt.flurstueckId.set( flurstueckId );

                    // FIXME
//                    Criteria criteria = ((HibernateConversation)conversation).createCriteria( nutzungMetaData );
//                    criteria.add( Restrictions.eq( "id", nutzung ) );
//                    criteria.setProjection( Projections.rowCount() );
//                    int rowCount = (Integer)criteria.list().get( 0 );
//                    if (rowCount > 0) {
//                        abschnittMetaData.getField( "nutzungsartId" ).set( entity, nutzung );                            
//                    } else {
//                        log.info( "--Nutzung: " + nutzung + " -- rowCount: " + rowCount + (rowCount == 0 ? " skipping." : "adding...") );
//                    }

                    //                     Object nutzungObj = abschnittMetaData.getAssociation( "nutzungsart" ).get( entity );
                    //                     System.out.println( "----nutzungObj: " + nutzungObj );
                }
                catch (RuntimeException e) {
                    System.out.println( "---dooofe abschnitte: " + abschnitte );
                }
            }
        }
    }


    // Test ***********************************************
    
//    public static void main( String[] args )
//    throws Exception {
//        //String line = "F 9874167    1   a  1$$A$$4167-22$$$$8600#5630###$5630$$$";
//        //String line = "F1101    1   0  0$     $A$0$Korr-FoVN    $             $13$Fleischerpl. 1, Markt 1$199#   1530###$     1530$$$N47,398,956,1007,1081";    
//        //String line = "F1101   0    1   0  0$457114834#560544292$A$$$$$Fleischerplatz 1#Markt 1$1990#1530###$1530$$$";
//        String line = "F1101   0   53   0  0$457124948#560553068$A$$$$$Obere Schmiedegasse 11$1990#270####6300#140###$410$$$";
//        System.out.println( "Line: " + line );
//
//        new FParser().parseLine( line );
//    }


}
