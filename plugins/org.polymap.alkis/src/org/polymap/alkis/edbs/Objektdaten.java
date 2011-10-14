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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Point;

import org.polymap.alkis.recordstore.IRecordState;


/**
 * Parser für 'ULOBNN' Datensätze.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class Objektdaten
        implements IEdbsRecordParser {

    private static Log log = LogFactory.getLog( Objektdaten.class );

    public static final int     ID = 5;

    public static final int     RECHTS = 0;
    public static final int     LINKS = 1;
    public static final int     BEIDE = 2;


    /**
     * Ein 'Objekt' Datensatz. 
     */
    public static class ObjektRecord
            extends EdbsRecord {

        @SuppressWarnings("hiding")
        public static final int             ID = 51;

        public static final ObjektRecord    TYPE = type( ObjektRecord.class );

        public ObjektRecord() {
            state().put( this.typeId.name(), ID );
        }

        public ObjektRecord( IRecordState record ) {
            super( record );
            assert typeId.get() != null /*&& typeId.get().equals( ID )*/;
        }

        public Property<Point> anfang = new Property( "anfang" );
        public Property<Integer> geomArt = new Property( "geomart" );
        public Property<Integer> folie = new Property( "folie" );
        public Property<Integer> objektArt = new Property( "objektArt" );
        public Property<String> objekttyp = new Property( "objekttyp" );
        public Property<String> objektnummer = new Property( "objektnummer" );
        public Property<String> teilnummer = new Property( "teilnummer" );
        public Property<Point> punkte = new Property( "punkte" );
        public Property<String> info = new Property( "info" );
        public Property<Integer> infoArt = new Property( "infoArt" );
    }    


    /**
     * Ein 'Linie' Datensatz. 
     */
    public static class LinieRecord
            extends EdbsRecord {

        @SuppressWarnings("hiding")
        public static final int         ID = 52;

        public static final LinieRecord TYPE = type( LinieRecord.class );

        public LinieRecord() {
            state().put( this.typeId.name(), ID );
        }
        
        public Property<Point> anfang = new Property( "anfang" );
        public Property<Point> enden = new Property( "enden" );
        public Property<Integer> geomArt = new Property( "geomart" );
        public Property<Integer> folien = new Property( "folien" );
        public Property<String> objektArten = new Property( "objektArten" );
        public Property<String> objektnummern1 = new Property( "objektnummern1" );
        public Property<String> objektnummern2 = new Property( "objektnummern2" );
        public Property<String> teilnummern1 = new Property( "teilnummern1" );
        public Property<String> teilnummern2 = new Property( "teilnummern2" );
        public Property<Integer> richtungen = new Property( "richtungen" );
        public Property<Point> lapa = new Property( "lapa" );
    }    


    // instance *******************************************
    
    /** Der aktuelle Datensatz. */
    private RecordTokenizer     tokenizer;

    
    @SuppressWarnings("hiding")
    public int canHandle( RecordTokenizer tokenizer )
    throws IOException {
        if ((tokenizer.operation.equals( "BSPE" ) || tokenizer.operation.equals( "FEIN" ))
                && tokenizer.infoname.equals( "ULOBNN  ")) {
            return ID;
        }
        else {
            return 0;
        }
    }


    @SuppressWarnings("hiding")
    public List<EdbsRecord> handle( RecordTokenizer tokenizer )
    throws IOException {
        int i = tokenizer.nextWhf();
        List<EdbsRecord> result = new ArrayList( i );
        while (i-- > 0) {
            result.addAll( handleOne( tokenizer ) );
        }
        return result;
    }
    
    
    @SuppressWarnings("hiding")
    protected List<EdbsRecord> handleOne( RecordTokenizer tokenizer )
    throws IOException {
        int i = tokenizer.nextWhf();       /* i muss 1 sein    */
        if (i != 1) {
            throw new EdbsParseException( "WHF Grundriss-Koordinate != 1" );
        }

        this.tokenizer = tokenizer;
        
        Point anfang = tokenizer.nextPoint(); /* globale Variable */
        tokenizer.skip( 1 );               /* 1 Byte fuer Pruefzeichen, nicht verwertet */

        List<EdbsRecord> result = new ArrayList();
        
        i = tokenizer.nextWhf();           /* Datengruppe 'Endpunkt der Linie' und abhaengige */
        while (i-- > 0) {
            result.add( linie( anfang ) );
        }
        i = tokenizer.nextWhf();           /* Datengruppe 'Funktion des Objekts' und abhaengige */
        while (i-- > 0) {
            result.add( objekt( anfang ) );
        }
        return result;
    }

    
    /**
     * Speichert Endpunkt der Linie in globaler Var. 'ende'.
     *
     * Liest 'anzahl_doppel' = Anzahl der Funktionen (Objektarten), die
     * fuer diese Linie folgen, z.B. 2, Linie als _Weg_ und zugleich als
     * _Grenze eines Siedlungspolygons_.
     * Laesst von 'linie_funktion()' Folie, Objektart, -nummern fuer
     * rechts und links liegendes Objekt (bei Polygonen) und
     * Objektteilnummern auslesen und global abspeichern.
     *
     * Liest Anzahl der Lageparameter (Interpolationspunkte)
     * und speichert Punkte in 'lapa[]'.
     *
     * Laesst Linie abspeichern ('linie_schreiben()'), falls Art
     * der Liniengeometrie = 11 (Gerade) oder = 15 (Vektor).
     *
     * @throws EdbsParseException 
     */
    private LinieRecord linie( Point anfang )
    throws EdbsParseException {
        int n = tokenizer.nextWhf();                /* fuer Endpunkt; muss 1 sein */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Linienendpunkt != 1" );
        }
        
        LinieRecord result = new LinieRecord();
        Point ende = tokenizer.nextPoint();
        result.anfang.put( anfang );
        result.enden.add( ende );
        
        int art_geo = tokenizer.nextInt( 2 );       /* Art der Geometrie */
        if (art_geo != 11 && art_geo != 15 ) {      /* keine Gerade/Vektor */
            throw new EdbsParseException( "Unbekannte Art der Geometrie: " + art_geo );
        }
        result.geomArt.add( art_geo );

        n = tokenizer.nextWhf();
        while (n-- > 0) {
            linieFunktion( result );
        }

        int anzahl_lapa = tokenizer.nextWhf();      /* Anzahl der Lageparameter */
        for (int i=0; i<anzahl_lapa; i++) {         /* Datengruppe "Lageparameter" */
            Point lapa = tokenizer.nextPoint();
            result.lapa.add( lapa );
        }
        return result;
    }

    
    /**
     * Bearbeitet anzahl_doppel mal die Datengruppe "Funktion der Linie".
     * @param result 
     * 
     * @throws EdbsParseException 
     */
    private void linieFunktion( LinieRecord result ) 
    throws EdbsParseException {
        
        if (tokenizer.nextWhf() != 1) {
            throw new EdbsParseException( "WHF Linienfunktion != 1" );
        }
        //folie[i] = lies_zahl( 3 );          /* Folie */
        //objektart[i] = lies_zahl( OA_LAENGE );  /* Objektart */
        result.folien.add( tokenizer.nextInt( 3 ) );
        result.objektArten.add( tokenizer.nextString( 4 ) );

        String objektnummer1 = tokenizer.nextString( 7 );  /* Objektnummer 1 (rechts)  */
        result.objektnummern1.add( objektnummer1 );
        String objektnummer2 = tokenizer.nextString( 7 );  /* Objektnummer 2 (link)  */
        result.objektnummern2.add( objektnummer1 );

        if (objektnummer1.equals( "       " )) {        /* Nr. rechts frei  */
            result.richtungen.add( LINKS );
        }
        else if (objektnummer1.equals( "       " )) {   /* Nr. links frei   */
            result.richtungen.add( RECHTS );
        }
        else {                                          /* beide Nummernfelder belegt   */
            result.richtungen.add( BEIDE );

            //                /* Nachbarschaft merken - Objektteilnummer beruecksichtigen?    */
            //                /* CR 2001-03-10                        */
            //                fprintf( fp_nachbar, IDFORMAT TRENNER IDFORMAT "\n",
            //                    nummer2id( objekt1[i] ), nummer2id( objekt2[i] ) );
            //                if( ferror( fp_att ) ) perror( "Fehler beim Schreiben in fp_nachbar\n" );
        }

        String teilnummer1 = tokenizer.nextString( 3 );          /* Objektteilnummer 1 (rechts)  */
        result.teilnummern1.add( teilnummer1 );
        String teilnummer2 = tokenizer.nextString( 3 );          /* Objektteilnummer 2 (links)  */
        result.teilnummern2.add( teilnummer2 );

        tokenizer.skip( 2 );                               /* Linienteilung1 + Linienteilung2 : nicht verwertet */

        int n = tokenizer.nextWhf();
        if (n != 0 ) {                                  /* fuer Fachparameter   */
            log.warn( "WHF Fachparameter > 0" );
            while (n-- > 0) {
                tokenizer.skip( 20 );                      /* Fachparameter ueberlesen */
            }
        }
    }

    
    /*
     * Bearbeitet einmal die 'Funktion des Objekts'.
     * Laesst die Datenelemente von 'objekt_funktion()'
     * auslesen und abspeichern.
     * Verzweigt dann ggfs. n-mal zu 'Besondere Information'
     * bearbeiten ('besondere_info()').
     */
    private ObjektRecord objekt( Point anfang ) 
    throws EdbsParseException {
        int n = tokenizer.nextWhf();   /* == 1, einmal Funktion des Objekts */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Objektfunktion != 1" );
        }        

        ObjektRecord result = new ObjektRecord();
        result.anfang.put( anfang );

        /* Ergebnis in folie[0], objektart[0], objekttyp, objekt1[0] */
        objektFunktion( result );

        n = tokenizer.nextWhf();       /* Wiederholungsfaktor Datengruppe 'Besondere Information' */
        while (n-- > 0) {
            besondereInfo( result );
        }
        return result;
    }

    
    /*
     * Liest einmal die Datengruppe 'Funktion des Objekts' und
     * speichert die Angaben zu Folie, Objektart, -typ und -nummer
     * in globalen Variablen.
     * Schreibt sie gleichzeitig in fp_objects.
     *
     * Laesst aktuellen Anfangspunkt von 'label_schreiben(...)' in
     * eine von Objekttyp und Objektart abhaengige Datei schreiben.
     */
    private void objektFunktion( ObjektRecord result ) {
        result.folie.add( tokenizer.nextInt( 3 ) );
        result.objektArt.add( tokenizer.nextInt( 4 ) );

        tokenizer.skip( 2 );               /* Aktualitaet des Objekts */

        char objekttyp = tokenizer.nextChar();
        result.objekttyp.put( String.valueOf( objekttyp ) );

        String objektnummer = tokenizer.nextString( 7 );
//        id = nummer2id( objekt1[0] );
        result.objektnummer.put( objektnummer );

        tokenizer.skip( 2 );               /* Modelltyp */
        tokenizer.skip( 6 );               /* Entstehungsdatum */
        tokenizer.skip( 1 );               /* Veraenderungskennung */

//        /* Abspeichern der allg. Objektinformationen in fp_objects */
//        fprintf( fp_objects, IDFORMAT TRENNER "%.7s", id, objekt1[0] );
//        fprintf( fp_objects, TRENNER OAFORMAT TRENNER "%c", objektart[0], objekttyp );
//        fprintf( fp_objects, TRENNER "%d", folie[0] );
//        fprintf( fp_objects, "\n" );
//        if( ferror( fp_objects ) ) perror( "Fehler beim Schreiben in fp_objects" );

//        /*
//         * Anfangspunkt als Punktgeometrie (Objektart 'P')
//         * oder als Labelpunkt ('L' oder 'F')
//         * in fp_points bzw. fp_llx000 oder fp_plx000 abspeichern.
//         */
//        return label_schreiben( objekttyp, objektart[0], id );
    }

    
//    /**
//     * Schreibt den aktuellen Anfangspunkt als Punktgeometrie in fp_points,
//     * bzw. bei linien- oder flaechenfoermigen Objekten abhaengig von der
//     * Objektart in fp_llx000 oder fp_plx000 (Linien- oder Polygonlabel).
//     */
//    private void labelSchreiben( char typ, int art, long unsigned id ) {
//        if (typ == 'P') /* Ausgabe eines Punkt-Objekts in fp_points */
//        {
//            fprintf(fp_points, IDFORMAT TRENNER PFORMAT TRENNER PFORMAT "\n", id, anfang.x, anfang.y);
//            if( ferror( fp_points ) ) perror( "Fehler beim Schreiben in fp_points" );
//        }
//
//        if (typ == 'F') /* Ausgabe eines Polygon-Labels abhaengig von Objektart */
//        {
//    /*      fprintf(fp_plabel, IDFORMAT TRENNER PFORMAT TRENNER PFORMAT "\n", id, anfang.x, anfang.y);  */
//    /*      if( ferror( fp_plabel ) ) perror( "Fehler beim Schreiben in fp_plabel" );   */
//
//            if( datenmodell == ALK ) {
//                fp_plx000 = fp_pl_alk;
//            } else {
//                switch( art / 1000 ) {
//                case 1: fp_plx000 = fp_pl1000; break;
//                case 2: fp_plx000 = fp_pl2000; break;
//                case 3: fp_plx000 = fp_pl3000; break;
//                case 4: fp_plx000 = fp_pl4000; break;
//                case 5: fp_plx000 = fp_pl5000; break;
//                case 6: fp_plx000 = fp_pl6000; break;
//                case 7: fp_plx000 = fp_pl7000; break;
//                default:fprintf(stderr, "Objektart _%d_ bei Polygonlabelpunkt!!\n", art); return -1;
//                }
//            }
//
//                    fprintf(fp_plx000, IDFORMAT TRENNER PFORMAT TRENNER PFORMAT "\n", id, anfang.x, anfang.y);
//            if( ferror( fp_plx000 ) ) perror( "Fehler beim Schreiben in fp_plx000" );
//
//        }
//
//        if (typ == 'L') /* Ausgabe eines Line-Labels abhaengig von Objektart */
//        {
//    /*      fprintf(fp_llabel, IDFORMAT TRENNER PFORMAT TRENNER PFORMAT "\n", id, anfang.x, anfang.y);  */
//    /*      if( ferror( fp_llabel ) ) perror( "Fehler beim Schreiben in fp_llabel" );   */
//
//            if( datenmodell == ALK ) {
//                fp_llx000 = fp_ll_alk;
//            } else {
//                switch( art / 1000 ) {
//                case 1: fp_llx000 = fp_ll1000; break;
//                case 2: fp_llx000 = fp_ll2000; break;
//                case 3: fp_llx000 = fp_ll3000; break;
//                case 4: fp_llx000 = fp_ll4000; break;
//                case 5: fp_llx000 = fp_ll5000; break;
//                case 6: fp_llx000 = fp_ll6000; break;
//                case 7: fp_llx000 = fp_ll7000; break;
//                default:fprintf(stderr, "Objektart _%d_ bei Linienlabelpunkt!!\n", art); return -1;
//                }
//            }
//
//                    fprintf(fp_llx000, IDFORMAT TRENNER PFORMAT TRENNER PFORMAT "\n", id, anfang.x, anfang.y);
//            if( ferror( fp_llx000 ) ) perror( "Fehler beim Schreiben in fp_llx000" );
//
//        }
//    }

    
    /**
     * Liest einmal die Datengruppe 'Besondere Information'.
     * Der Text wird in Abhaengigkeit vom Datenelement
     * 'Art der Information' als geographischer, Kurz- oder
     * Zweitname oder als Ueber- oder Unterfuehrungsreferenz
     * oder als Objektnummer eines Teilobjekt eines komplexen
     * Objekts interpretiert und entsprechend abgespeichert.
     * 
     * @throws EdbsParseException 
     */
    private void besondereInfo( ObjektRecord result ) 
    throws EdbsParseException {
        int n = tokenizer.nextWhf();               /* == 1, einmal 'Besondere Information' */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Besondere Information != 1" );
        }

        int infoArt = tokenizer.nextInt( 2 );      /* 'Art der Information' */
        result.infoArt.put( infoArt );

        tokenizer.skip( 2 );                       /* Kartentyp */
        tokenizer.skip( 6 );                       /* Signaturteilnummer */

        String text = tokenizer.nextString( 33 );  /* Datenelement 'Text' */
        result.info.put( text );

        int geometrieArt = tokenizer.nextInt( 2 );  /* Art der Geometrieangabe */
        result.geomArt.put( geometrieArt );
        String teilnummer = tokenizer.nextString( 3 );
        result.teilnummer.put( teilnummer );        /* Objektteilnummer */

//        objektInfo( tokenizer );
        n = tokenizer.nextWhf();       /* Wiederholungsfaktor Datengruppe 'Geometrieangabe' */
        
        // Art 53 = Text in freier Ausrichtung (mit Richtungswinkel "TT")
        if (geometrieArt == 53) {
            tokenizer.skip( "TT13613             ".length() );
            n--;
        }
        
        while (n-- > 0) {
            geoAngabe( result, geometrieArt );
        }
    }
    
    
    /**
     * Liest die Geometrieangabe und speichert gefundene Punkte
     * mit der globalen 'id' und Art der Geometrie in fp_npos
     * (Namenpositionierung) ab.
     * 
     * @param result 
     * @param geometrieArt 
     */
    private void geoAngabe( ObjektRecord result, int geometrieArt ) {

        /* evtl. hier switch nach Art der Geometrie (Gerade, Schraffur, ...) */
//        if( n > 0 ) fprintf( fp_npos, IDFORMAT TRENNER "%2d", id, adg );

        Point gp = tokenizer.nextPoint();
        result.punkte.add( gp );
    }
    
    
//    int objekt_info( void )
//    /*
//     * Speichert Inhalt des Texts der 'Besonderen Information'
//     * (globale Var. 'text') entsprechend der 'art_info' ab.
//     */
//    {
//        char    nummer[ 8 ];    /* Objektnummer aus Textfeld */
//
//        switch( art_info ) {
//        case 16:
//            if( (text[0] == 'H') && (text[1] == 'A') )  /* ALK - Hausnummer */
//                fprintf( fp_ha_alk, IDFORMAT TRENNER "%.13s" TRENNER "%.4s" "\n", id, text+2, text+15 );
//
//        case 41:
//            /* Text enthaelt Nummer des kompl. Obj., zu dem akt. Objekt gehoert */
//            /* nicht abgespeichert, da redundante Information (siehe 'case 42') */
//            break;
//
//        case 42:
//            /* Text enthaelt Nummer eines der Teilobjekte des akt. kompl. Objekts */
//            fprintf( fp_complex, IDFORMAT TRENNER, id );
//            /*  fprintf( fp_complex,   "%.7s" TRENNER, text );  */
//            strncpy( nummer, text, 7 ); nummer[ 7 ] = '\0';
//            fprintf( fp_complex, IDFORMAT    "\n", nummer2id( nummer ) );
//            if( ferror( fp_complex ) ) perror( "Fehler beim Schreiben in fp_complex" );
//
//            break;
//
//        case 44:
//            /* kartographische Bezeichnung ab 3. Zeichen in fp_xname ausgeben */
//            if( (text[0] == 'G') && (text[1] == 'N') )
//            {
//                fprintf( fp_gname, IDFORMAT TRENNER, id );
//                fprintf( fp_gname,     "%s"    "\n", text+2 );
//                if( ferror( fp_gname ) ) perror( "Fehler beim Schreiben in fp_gname" );
//            }
//            else if( (text[0] == 'K') && (text[1] == 'N') )
//            {
//                fprintf( fp_kname, IDFORMAT TRENNER, id );
//                fprintf( fp_kname,     "%s"    "\n", text+2 );
//                if( ferror( fp_kname ) ) perror( "Fehler beim Schreiben in fp_kname" );
//            }
//            else if( (text[0] == 'Z') && (text[1] == 'N') )
//            {
//                fprintf( fp_zname, IDFORMAT TRENNER, id );
//                fprintf( fp_zname,     "%s"    "\n", text+2 );
//                if( ferror( fp_zname ) ) perror( "Fehler beim Schreiben in fp_zname" );
//            }
//
//            break;
//
//        case 46:
//        case 47:
//            /* Ueber- oder Unterfuehrungsreferenz fuer Objekt mit 'art_info' in fp_refer */
//
//            fprintf( fp_refer, IDFORMAT TRENNER  "%2d" TRENNER, id,  art_info );
//            /* erste Referenz  (Objektnummer ab text[0], -teilnummer ab text[7])        */
//            /*  fprintf( fp_refer,   "%.7s" TRENNER "%.3s" TRENNER, text,     text+7 ); */
//            strncpy( nummer, text,    7 ); nummer[ 7 ] = '\0';  /* Objektnummer -> ID   */
//            fprintf( fp_refer, IDFORMAT TRENNER "%.3s" TRENNER, nummer2id( nummer ), text+7 );
//            /* zweite Referenz (Objektnummer ab text[10], -teilnummer ab text[17])      */
//            /*  fprintf( fp_refer,   "%.7s" TRENNER "%.3s"    "\n", text+10, text+17 ); */
//            strncpy( nummer, text+10, 7 ); nummer[ 7 ] = '\0';  /* Objektnummer -> ID   */
//            fprintf( fp_refer, IDFORMAT TRENNER "%.3s"    "\n", nummer2id( nummer ), text+17 );
//
//            if( ferror( fp_refer ) ) perror( "Fehler beim Schreiben in fp_refer" );
//
//            break;
//
//        case 48:
//            /* Text ist leer, ??? */
//            break;
//
//        default:
//            /* unbekannte Art der Information */
//            fprintf( fp_noname, IDFORMAT TRENNER "%2d" TRENNER "%s\n", id, art_info, text );
//            if( ferror( fp_noname ) ) perror( "Fehler beim Schreiben in fp_noname" );
//            break;
//        }
//
//        return 0;
//    } /* ******* objekt_info ******* */

}
