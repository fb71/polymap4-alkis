package org.polymap.alkis.importer.edbs;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;

import org.polymap.alkis.importer.edbs.EdbsReader.Datenmodell;

/**
 * Repräsentiert eine Zeile in der EDBS-Datei und stellt Methoden
 * für die {@link IEdbsRecordParser} bereit.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RecordTokenizer {
    
    private static Log log = LogFactory.getLog( Objektdaten.class );
    
    /** edbsReader */
    private EdbsReader      reader;

    String                  data;

    /** aktuelle Position im satz */
    int                     pos = 0;

    int                     laenge = 36;

    public String           operation, infoname;
    
    public char             zugehoerig;
    
    public PrintStream      debugOut = null; //System.out;
    
    public int              fieldCount = 0;
    
    
    RecordTokenizer( EdbsReader reader, String data ) {
        this.reader = reader;
        this.data = data;
    }

    
    public void init() throws IOException {
        if (!nextString( 4, "???" ).equals( "EDBS" )) {
            throw new IOException( "Input ist kein EDBS-Satz." );
        }

        /* Satzlaenge lesen (gezaehlt ab Pos. 13), Laenge des Headers (36) abziehen */
        /* ergibt: Erhoehung von satz_laenge um Laenge des "Inhalts der Information"    */
        laenge += (12 + nextInt( 4, "???" ) - 36);

        skip( 4 );                          /* Anfangsadresse Suchkriterium */
        operation = nextString( 4, "???" ); /* Operation */
        skip( 6 );                          /* EDBS-Satznummer */
        zugehoerig = nextChar( "???" );     /* Zugehoerigkeitsschluessel */
        skip( 1 );                          /* Editierschluessel */
        skip( 4 );                          /* Quittungsschluessel */
        infoname = nextString( 8, "???" );  /* Name der Information */
    }
    
    
    public String toString() {
        return "RecordTokenizer [" + data + "]";
    }

    
    public int length() {
        return data.length();
    }
    
    
    private void log( String fieldName, int indent, Object value ) {
        if (debugOut != null) {
            String fill = ((fieldCount++ % 2) != 0) ? " " : ".";
            debugOut.println( fieldName 
                    + StringUtils.repeat( fill, indent-fieldName.length() ) 
                    + value );
        }
    }
    
    
    /**
     * Liefert naechstes Zeichen aus satz.
     * 
     * @param fieldName Der Name des Felder für Debug-Ausgaben.
     */
    public char nextChar( String fieldName ) {
        char result = data.charAt( pos++ );
        log( fieldName, pos-1, result );
        return result;
    }

    
    /**
     * Liest n Zeichen aus satz ab Position pos.
     * Setzt pos auf pos+n.
     *
     * @param fieldName Der Name des Felder für Debug-Ausgaben.
     */
    public String nextString( int n, String fieldName ) {
        String result = data.substring( pos, pos + n );
        log( fieldName, pos, result );
        pos += n;
        return result;
    }

    
    /**
     * Liefert die naechsten n Zeichen in satz als ganze Zahl.
     *
     * @param fieldName Der Name des Felder für Debug-Ausgaben.
     */
    public int nextInt( int n, String fieldName ) {
        return Integer.parseInt( nextString( n, fieldName ) );
    }

    
    /**
     * Liefert die naechsten n Zeichen in satz als 4stellige ganze Zahl.
     */ 
    public int nextWhf() {
        int whf = nextInt( 4, "WHF" );
        if (whf > 200) {
            log.warn( "WHF > 200 koennte fehlerhaft sein." );
        }
        return whf;
    }

    
    /**
     *  liest ab der aktuellen Position in 'satz'
     *  Numerierungsbezirk und relativen Rechts- und
     *  Hochwert (= 20 Zeichen, ALK/ATKIS-Format), und
     *  schreibt Gauss-Krueger-Koordinaten ins Feld 'p'.
     */
    public Coordinate nextCoordinate() {
        double x = 100000.0 * nextInt( 2, "X x 100000" );    /* NBZ [100 km] */
        double y = 100000.0 * nextInt( 2, "Y x 100000" );

        if (reader.datenmodell == Datenmodell.ALK) {
            x = x + 1000.0 * nextInt( 2, "X x 1000" );    /* NBZ ALK [1 km] */
            y = y + 1000.0 * nextInt( 2, "Y x 1000" );

            x = x + 0.001 * nextInt( 6, "X x 0.001" );     /* rel. Rechtswert [1 mm] */
            y = y + 0.001 * nextInt( 6, "Y x 0.001" );     /* rel. Hochwert   [1 mm] */    
        } 
        else if (reader.datenmodell == Datenmodell.ATKIS) {
            x = x + 10000.0 * nextInt( 1, "???" );     /* NBZ ATKIS [10 km] */
            skip( 1 );                          /* Leerzeichen */
            y = y + 10000.0 * nextInt( 1, "???" );
            skip( 1 );

            x = x + 0.01 * nextInt( 6, "???" );        /* rel. Rechtswert [1 cm] */
            y = y + 0.01 * nextInt( 6, "???" );        /* rel. Hochwert   [1 cm] */
        }
        else {
            throw new IllegalStateException( "Unbekanntes Datenmodell: " + reader.datenmodell );
        }

        return new Coordinate( x, y );
    }

    
    /**
     * Zaehlt den Positionszeiger pos fuer satz um n hoch.
     */
    public void skip( int n ) {
        log( "skip", pos, "-" );
        pos += n;
    }

}