package org.polymap.alkis.edbs;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import org.polymap.alkis.edbs.EdbsReader.Datenmodell;

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
    
    
    RecordTokenizer( EdbsReader reader, String data ) {
        this.reader = reader;
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
    public Point nextPoint() {
        Point p;

        double x = 100000.0 * nextInt( 2 );    /* NBZ [100 km] */
        double y = 100000.0 * nextInt( 2 );

        if (reader.datenmodell == Datenmodell.ALK) {
            x = x + 1000.0 * nextInt( 2 );    /* NBZ ALK [1 km] */
            y = y + 1000.0 * nextInt( 2 );

            x = x + 0.001 * nextInt( 6 );     /* rel. Rechtswert [1 mm] */
            y = y + 0.001 * nextInt( 6 );     /* rel. Hochwert   [1 mm] */    
        } 
        else if (reader.datenmodell == Datenmodell.ATKIS) {
            x = x + 10000.0 * nextInt( 1 );     /* NBZ ATKIS [10 km] */
            skip( 1 );                          /* Leerzeichen */
            y = y + 10000.0 * nextInt( 1 );
            skip( 1 );

            x = x + 0.01 * nextInt( 6 );        /* rel. Rechtswert [1 cm] */
            y = y + 0.01 * nextInt( 6 );        /* rel. Hochwert   [1 cm] */
        }
        else {
            throw new IllegalStateException( "Unbekanntes Datenmodell: " + reader.datenmodell );
        }

        return EdbsReader.gf.createPoint( new Coordinate( x, y ) );
    }

    
    /**
     * Zaehlt den Positionszeiger pos fuer satz um n hoch.
     */
    public void skip( int n ) {
        pos += n;
    }

}