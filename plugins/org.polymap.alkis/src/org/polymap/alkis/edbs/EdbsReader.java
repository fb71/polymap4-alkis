/*
    polymap.org
    Copyright (C) 1995 Claus Rinner
    Copyright 2011, Polymap GmbH. All rights reserved.

    Dieses Programm ist freie Software. Sie können es unter
    den Bedingungen der GNU General Public License, wie von der
    Free Software Foundation herausgegeben, weitergeben und/oder
    modifizieren, entweder unter Version 2 der Lizenz oder (wenn
    Sie es wünschen) jeder späteren Version.

    Die Veröffentlichung dieses Programms erfolgt in der
    Hoffnung, daß es Ihnen von Nutzen sein wird, aber OHNE JEDE
    GEWÄHRLEISTUNG - sogar ohne die implizite Gewährleistung
    der MARKTREIFE oder der EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Details finden Sie in der GNU General Public License.
*/
package org.polymap.alkis.edbs;

import java.util.ArrayList;
import java.util.List;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.polymap.core.runtime.Timer;

/**
 * 
 * @author <a href="http://www.rinners.de/edbs/download.html">Claus Rinner</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsReader {

    private static Log log = LogFactory.getLog( EdbsReader.class );
    
    public static final GeometryFactory gf = new GeometryFactory();
    
    /**
     * Format der Koordinatenangaben. 
     */
    public enum Datenmodell     { ALK, ATKIS };
    
    public static final int     ABBRUCH = -1;
    public static final int     UNDEFINIERT = 0;
//    public static final int     AUFTRAGSKENNSATZ = 1;
//    public static final int     GEBIETSKENNZEICHNUNG = 2;
//    public static final int     AUFTRAGSKENNDATEN = 3;
//    public static final int     AUFTRAGSTEXTDATEN = 4;
//    public static final int     OBJEKTDATEN = 5;
//    public static final int     ATTRIBUTE = 6;
//    public static final int     AUFTRAGSENDE = 7;

    /* maximale EDBS-Satzlaenge gemaess Dokumentation */
    public static final int     MAX_SATZLAENGE = 2000;

    /* max. Anzahl verarbeitbarer Fortsetzungssaetze */
    public static final int     MAX_FORTSATZ = 100;

    private PrintStream         stderr = System.err;
    private PrintStream         stdout = System.out;
    private LineNumberReader    stdin;
    
    private int                 satz_zaehler = 0;   /* Anzahl gelesener Saetze */

    private List<IEdbsRecordHandler> handlers = new ArrayList();
    
    private Datenmodell         datenmodell = Datenmodell.ALK;

    /**
     * Repräsentiert eine Zeile in der EDBS-Datei und stellt Methoden
     * für die {@link IEdbsRecordHandler} bereit.
     */
    public class RecordTokenizer {
        
        private String          data;

        /** aktuelle Position im satz */
        private int             pos = 0;

        private int             laenge = 36;

        public String           operation, infoname;
        
        public char             zugehoerig;
        
        
        RecordTokenizer( String data ) {
            this.data = data;
        }

        public void init() throws IOException {
            if (!nextString( 4 ).equals( "EDBS" )) {
                throw new IOException( "Input ist kein EDBS-Satz." );
            }

            /* Satzlaenge lesen (gezaehlt ab Pos. 13), Laenge des Headers (36) abziehen */
            /* ergibt: Erhoehung von satz_laenge um Laenge des "Inhalts der Information"    */
            laenge += (12 + nextInt( 4 ) - 36);

            skip( 4 );                  /* Anfangsadresse Suchkriterium */
            operation = nextString( 4 );/* Operation */
            skip( 6 );                  /* EDBS-Satznummer */
            zugehoerig = nextChar();    /* Zugehoerigkeitsschluessel */
            skip( 1 );                  /* Editierschluessel */
            skip( 4 );                  /* Quittungsschluessel */
            infoname = nextString( 8 ); /* Name der Information */
        }
        
        public String toString() {
            return "RecordTokenizer [" + data + "]";
        }

        public int length() {
            return data.length();
        }
        
        /*
         * Liefert naechstes Zeichen aus satz.
         */
        public char nextChar() {
            return data.charAt( pos++ );            
        }

        /**
         * Liest n Zeichen aus satz ab Position pos.
         * Setzt pos auf pos+n.
         */
        public String nextString( int n ) {
            String result = data.substring( pos, pos + n );
            pos += n;
            return result;
        }

        /**
         * Liefert die naechsten n Zeichen in satz als ganze Zahl.
         */
        public int nextInt( int n ) {
            return Integer.parseInt( nextString( n ) );
        }

        /**
         * Liefert die naechsten n Zeichen in satz als 4stellige ganze Zahl.
         */ 
        public int nextWhf() {
            int whf = nextInt( 4 );
            if (whf > 200) {
                stderr.printf( "WHF > 200 koennte fehlerhaft sein." );
            }
            return whf;
        }

        
        /**
         *  liest ab der aktuellen Position in 'satz'
         *  Numerierungsbezirk und relativen Rechts- und
         *  Hochwert (= 20 Zeichen, ALK/ATKIS-Format), und
         *  schreibt Gauss-Krueger-Koordinaten ins Feld 'p'.
         */
        public Point nextPoint() {
            Point p;

            double x = 100000.0 * nextInt( 2 );    /* NBZ [100 km] */
            double y = 100000.0 * nextInt( 2 );

            if (datenmodell == Datenmodell.ALK) {
                x = x + 1000.0 * nextInt( 2 );    /* NBZ ALK [1 km] */
                y = y + 1000.0 * nextInt( 2 );

                x = x + 0.001 * nextInt( 6 );     /* rel. Rechtswert [1 mm] */
                y = y + 0.001 * nextInt( 6 );     /* rel. Hochwert   [1 mm] */    
            } 
            else if (datenmodell == Datenmodell.ATKIS) {
                x = x + 10000.0 * nextInt( 1 );     /* NBZ ATKIS [10 km] */
                skip( 1 );                          /* Leerzeichen */
                y = y + 10000.0 * nextInt( 1 );
                skip( 1 );

                x = x + 0.01 * nextInt( 6 );        /* rel. Rechtswert [1 cm] */
                y = y + 0.01 * nextInt( 6 );        /* rel. Hochwert   [1 cm] */
            }
            else {
                throw new IllegalStateException( "Unbekanntes Datenmodell: " + datenmodell );
            }

            return gf.createPoint( new Coordinate( x, y ) );
        }

        
        /**
         * Zaehlt den Positionszeiger pos fuer satz um n hoch.
         */
        public void skip( int n ) {
            pos += n;
        }

    }

    
    public EdbsReader( LineNumberReader in ) {
       this.stdin = in;
       handlers.add( new Auftragskennung() );
       handlers.add( new Attribute() );
       handlers.add( new Objektdaten() );
    }

    
//    public int lies() throws IOException {
//        int undefiniert = 0;    /* Zaehler fuer nicht identifizierte EDBS-Satzarten */
//        int n;                  /* Wiederholungsfaktor fuer den kompletten EDBS-Parameter 'Inhalt */
//                                /* der Information'; ist unabhaengig von der Satzart immer = 1 */
//
//        /* Endlosschleife zum Einlesen und Bearbeiten der EDBS-Saetze;          */
//        /* Abbruch der Schleife bei Auftragsendesatz oder               */
//        /* mehr als MAX_UNDEFINIERT-maligem Auftreten der Satzart UNDEFINIERT oder  */
//        /* bei Satzart ABBRUCH oder nicht definierter Satzart (default: ... )       */
//        while (true) {
//            switch (lies_edbs()) {
//                case AUFTRAGSKENNSATZ:
//                    stdout.printf( "Satz Nr. %d: Auftragskennsatz\n", satz_zaehler );
////                    auftragskennung();
//                    break;
//                case GEBIETSKENNZEICHNUNG:
//                    stdout.printf( "Satz Nr. %d: Gebietskennzeichnung\n", satz_zaehler );
////                    gebietskennzeichnung();
//                    break;
//                case AUFTRAGSKENNDATEN:
//                    stdout.printf( "Satz Nr. %d: Auftragskenndaten\n", satz_zaehler );
//                case AUFTRAGSTEXTDATEN:
//                    stdout.printf( "Satz Nr. %d: Auftragstextdaten\n", satz_zaehler );
//                    n = satz.nextWhf();
////                    while( n-- > 0 ) auftragstext();
//                    break;
//                case OBJEKTDATEN:
//                    stdout.printf( "Satz Nr. %d: Objektdaten\n", satz_zaehler );
//                    n = satz.nextWhf();
////                    while( n-- > 0 ) grundrisskennzeichen();
//                    break;
//                case ATTRIBUTE:
//                    stdout.printf( "Satz Nr. %d: Attribute\n", satz_zaehler );
//                    n = satz.nextWhf();
////                    while( n-- > 0 ) attributkennzeichen();
//                    break;
//                case AUFTRAGSENDE:
//                    stdout.printf( "Satz Nr. %d: Auftragsende\n", satz_zaehler );
////                    fp_close();
//                    return 0;
//                case UNDEFINIERT:
//                    stderr.printf( "Die Satzart der Inputzeile %d ", satz_zaehler );
//                    stderr.printf( "konnte nicht identifiziert werden: \n" );
//                    stderr.printf( satz.toString() );
//                    stderr.printf( "\nDer Satz wurde nicht bearbeitet !\n" );
//                    if( undefiniert++ > 100 ) {
//                        stderr.printf( "\n\nZuviele Einlese-Fehler !\n" );
//                        return 1;
//                    }
//                    break;
//                default:
//                    stderr.printf( "\n\nIrregulaerer Programmabbruch !!!\n" );
//                    return 1;
//            }
//        }
//    }
    
    
//    /**
//     * Liefert Flag fuer EDBS-Satzart je nach Operation op und Infoname in.
//     */
//    protected int vgl_satzinfo( String op, String in ) {
//        if( !strcmp(op, "BSPE") || !strcmp(op, "FEIN") )
//        {
//            if( strcmp(in, "ULOBNN  ") ) return OBJEKTDATEN;
//            if( strcmp(in, "ULTANN  ") ) return ATTRIBUTE;
//        }
//        if( !strcmp(op, "BINF") && !strcmp(in, "ULOTEX  "))
//            return AUFTRAGSKENNDATEN;
//        if( !strcmp(op, "OTEX") && !strcmp(in, "ULOTEX  "))
//            return AUFTRAGSTEXTDATEN;
//        if( !strcmp(op, "BKRT") && !strcmp(in, "IBENKRT "))
//            return GEBIETSKENNZEICHNUNG;
//        if( !strcmp(op, "AKND") && !strcmp(in, "ULQA0000"))
//            return AUFTRAGSKENNSATZ;
//        if( !strcmp(op, "AEND") )
//            return AUFTRAGSENDE;
//    
//        stderr.printf( "\nOperation _%s_, Infoname _%s_: Auftragsart unbekannt\n", op, in);
//        return UNDEFINIERT;
//    }


    /**
     * Liest naechsten EDBS-Satz. Falls misslungen, Programmabbruch;
     * d.h. in main() kontrollieren, ob noch Saetze vorhanden.
     * Laesst Satzart und Z-Schluessel auslesen.
     * Laesst F-Saetze lesen, falls Z_Schluessel 'A' und solange 'F'.
     * Falls letzter Z_Schluessel nicht 'E', UNDEFINIERT liefern.
     * Ebenso, falls Satzart bei F-Saetzen wechselt.
     * Ebenso, falls Z-Schluessel weder ' ' noch 'A'.
     * Sonst liefere satzart.
     * 
     * @throws IOException 
     */
    protected RecordTokenizer next() throws IOException {
        int satzart, fsatzart;
    
        RecordTokenizer satz = lies_satz();
        if (satz == null) {
            return null;
        }
        
        satzart = satzart( satz );
    
        if (satz.zugehoerig == 'A') {
            lies_fortsatz( satz );
            fsatzart = satzart( satz );
    
            while (satz.zugehoerig == 'F' && fsatzart == satzart ) {
                lies_fortsatz( satz );
                fsatzart = satzart( satz );
            }
    
            if (fsatzart != satzart) {
                stderr.printf( "\nSatzart wechselt in Fortsetzungssaetzen\n" );
                return null;
            }
    
            else if (satz.zugehoerig != 'E') {
                stderr.printf( "\nKein Endesatz fuer Fortsetzungssaetze\n" );
                return null;
                /* Achtung: In diesem Fall koennen der A-Satz, alle evtl. folgenden F-  */
                /* Saetze UND DER DARAUFFOLGENDE NICHT-E-SATZ nicht bearbeitet werden.  */
                /* D.h., bedingt durch das Zusammenschmelzen der Fortsetzungssaetze in  */
                /* lies_fortsatz() geht ein "unbeteiligter" Satz verloren.      */
            }
        }
    
        else if (satz.zugehoerig != ' ') {
            stderr.printf( "\nEDBS-Folge- oder Endesatz ohne Anfangssatz\n" );
            return null;
        }
        return satz;
    }


    /**
     * Liest naechsten EDBS-Satz in satz ein.
     * Setzt satz_laenge auf 36 und pos auf 0.
     * Liefert 0, falls Inputfehler.
     * Liefert String-Laenge von satz sonst.
     * @throws IOException 
     */
    protected RecordTokenizer lies_satz() throws IOException {
        RecordTokenizer satz = new RecordTokenizer( null );
        if ((satz.data = stdin.readLine()) == null) {
            return null;
        }
        satz.init();
        satz_zaehler++;
        return satz;
    }


    /**
     * Liest naechsten EDBS-Satz in fsatz ein.
     * Setzt pos auf 0 (satz_laenge wird in lies_satzart erhoeht).
     * Haengt Inhalt der Information von fsatz (ab Position 36)
     * hinten an satz (ab Position satz+satz_laenge) an.
     * Kopiert Pos. 0-35 von fsatz ueber 0-35 von satz.
     * 
     * Liefert 0, falls Inputfehler.
     * Liefert String-Laenge von fsatz sonst.
     * @throws IOException 
     */
    protected int lies_fortsatz( RecordTokenizer satz ) throws IOException {
        String fsatz;
        int laenge;

        if ((fsatz = stdin.readLine()) == null) {
            return 0;
        }
        satz_zaehler++;
        satz.pos = 0;

        if (satz.laenge + fsatz.length() - 36 > MAX_FORTSATZ * MAX_SATZLAENGE) {
            stderr.printf( "\nSpeicher reicht nicht fuer alle Folgesaetze\n" );
            return UNDEFINIERT;
        }
        
        // XXX als methode von RecordTokenizer
        StringBuilder buf = new StringBuilder( satz.length() + fsatz.length() );
        buf.append( fsatz, 0, 36 );
        buf.append( satz.data, 36, satz.length() );
        buf.append( fsatz, 36, fsatz.length() );
        
        satz.data = buf.toString();
        satz.init();
        
//        strncpy( satz, fsatz, 36 );
//        strcpy( satz + satz_laenge, fsatz + 36 );

        return fsatz.length();
    }

    
    protected int satzart( RecordTokenizer satz ) 
    throws IOException {
        for (IEdbsRecordHandler handler : handlers) {
            int satzart = handler.canHandle( satz );
            if (satzart != 0) {
                return satzart;
            }
        }
        return 0;
    }

    
    public List<EdbsRecord> edbsRecords( RecordTokenizer satz ) 
    throws IOException {
        for (IEdbsRecordHandler handler : handlers) {
            if (handler.canHandle( satz ) != 0) {
                return handler.handle( satz );
            }
        }
        return ListUtils.EMPTY_LIST;
    }


//    static boolean strcmp( String s1, String s2 ) {
//        return s1.equals( s2 );
//    }
    
    
    /**
     *  konvertiert 5 letzten Zeichen aus aktuelle_nummer
     *  in long unsigned integer
     *  Grund fuer die Beschraenkung auf 5 Zeichen:
     *  ARC/INFO - Data Models, Concepts & Key Terms, Seite B-3:
     *      "User-IDs are stored as 4-byte binary integers
     *      and can range from +-1 to +-2147483647."
     */
//    private long nummer2id( char* nr ) {
//        long unsigned rval = 0;
//            int l = strlen(nr)-1;
//
//            if (l >= 5) {
//              rval = (nr[l]-48) + (nr[l-1]-48)*45 + (nr[l-2]-48)*2025 + (nr[l-3]-48)*91125 + (nr[l-4]-48)*2025*2025;
//              return rval; }
//            else
//              return 0;
//    }
    
    
    /*
     * 
     */
    public static void main( String[] args )
    throws Exception {

        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.alkis", "debug" );

        LineNumberReader in = new LineNumberReader( new FileReader( 
                "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/edbs.ALK_Muster_EDBS_BSPE.dbout.1.001" ) );
        
        List<IEdbsConsumer> consumers = new ArrayList();

        // consumers
//        consumers.add( new LogConsumer() );
        consumers.add( new FeatureBuilder() );
        
        EdbsReader reader = new EdbsReader( in );
        RecordTokenizer satz = null;
        int count = 0, errorCount = 0;
        Timer timer = new Timer();
        
        while ((satz = reader.next()) != null) {
            count++;
//            reader.stdout.println( count + ": " + satz.toString() );
            
            try {
                List<EdbsRecord> objs = reader.edbsRecords( satz );
                for (EdbsRecord obj : objs) {
                    for (IEdbsConsumer consumer : consumers) {
                        consumer.consume( obj );       
                    }
                }
            }
            catch (Exception e) {
                log.warn( "" + e.toString()/*, e*/ );
                errorCount++;
            }
        }
        
        // endOfRecords()
        for (IEdbsConsumer consumer : consumers) {
            consumer.endOfRecords();       
        }
        reader.stdout.println( "Fehler: " + errorCount + ", Zeit: " + timer.elapsedTime() + "ms" );
    }

}
