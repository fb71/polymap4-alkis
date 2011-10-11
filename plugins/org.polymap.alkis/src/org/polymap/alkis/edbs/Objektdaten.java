/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.alkis.edbs;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Point;

import org.polymap.alkis.edbs.EdbsReader.RecordTokenizer;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class Objektdaten
        implements IEdbsRecordHandler {

    private static Log log = LogFactory.getLog( Objektdaten.class );

    public static final int     ID = 5;

    public static final int     RECHTS = 0;
    public static final int     LINKS = 1;
    public static final int     BEIDE = 2;

    /**
     * Die möglichen Properties, die in {@link EdbsRecord} geschrieben werden. 
     */
    public enum Property {
            FOLIE,
            OBJEKTART,
            OBJEKTTYP,
            OBJEKTNUMMER,
            TEILNUMMER,
            RICHTUNG,
            LAPA,
            PUNKTE,
            ANFANG,
            ENDE,
            GEOM_ART,
            OBJEKTNUMMER1,
            OBJEKTNUMMER2,
            TEILNUMMER1,
            TEILNUMMER2,
            INFO,
            INFO_ART
    }
    
    /** Der aktuelle Datensatz. */
    private RecordTokenizer     record;
    /** Das aktuelle Ergebnisobjekt. */
    private EdbsRecord          result;

    
    @SuppressWarnings("hiding")
    public int canHandle( RecordTokenizer record )
    throws IOException {
        if ((record.operation.equals( "BSPE" ) || record.operation.equals( "FEIN" ))
                && record.infoname.equals( "ULOBNN  ")) {
            return ID;
        }
        else {
            return 0;
        }
    }


    @SuppressWarnings("hiding")
    public List<EdbsRecord> handle( RecordTokenizer record )
    throws IOException {
        int i = record.nextWhf();
        List<EdbsRecord> result = new ArrayList( i );
        while (i-- > 0) {
            result.add( handleOne( record ) );
        }
        return result;
    }
    
    
    @SuppressWarnings("hiding")
    protected EdbsRecord handleOne( RecordTokenizer record )
    throws IOException {
        int i = record.nextWhf();       /* i muss 1 sein    */
        if (i != 1) {
            throw new EdbsParseException( "WHF Grundriss-Koordinate != 1" );
        }

        this.record = record;
        this.result = new EdbsRecord( ID );
        
        Point anfang = record.nextPoint(); /* globale Variable */
        result.put( Property.ANFANG, anfang );
        record.skip( 1 );               /* 1 Byte fuer Pruefzeichen, nicht verwertet */

        i = record.nextWhf();           /* Datengruppe 'Endpunkt der Linie' und abhaengige */
        while (i-- > 0) {
            linie();
        }
        i = record.nextWhf();           /* Datengruppe 'Funktion des Objekts' und abhaengige */
        while (i-- > 0) {
            objekt();
        }
        return this.result;
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
    private void linie() 
    throws EdbsParseException {
        int n = record.nextWhf();               /* fuer Endpunkt; muss 1 sein */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Linienendpunkt != 1" );
        }        
        Point ende = record.nextPoint();
        result.add( Property.ENDE, ende );
        
        int art_geo = record.nextInt( 2 );  /* Art der Geometrie */
        if (art_geo != 11 && art_geo != 15 ) { /* keine Gerade/Vektor */
            throw new EdbsParseException( "\nUnbekannte Art der Geometrie: " + art_geo );
        }
        result.add( Property.GEOM_ART, art_geo );

        n = record.nextWhf();
        while (n-- > 0) {
            linieFunktion();
        }

        int anzahl_lapa = record.nextWhf(); /* Anzahl der Lageparameter */
        for (int i=0; i<anzahl_lapa; i++) { /* Datengruppe "Lageparameter" */
            Point lapa = record.nextPoint();
            result.add( Property.LAPA, lapa );
        }
//        return linie_schreiben();   /* Ausgabe in 'fp' */
    }

    
    /**
     * Bearbeitet anzahl_doppel mal die Datengruppe "Funktion der Linie".
     * 
     * @throws EdbsParseException 
     */
    private void linieFunktion() 
    throws EdbsParseException {
        
        if (record.nextWhf() != 1) {
            throw new EdbsParseException( "WHF Linienfunktion != 1" );
        }
        //folie[i] = lies_zahl( 3 );          /* Folie */
        //objektart[i] = lies_zahl( OA_LAENGE );  /* Objektart */
        result.add( Property.FOLIE, record.nextInt( 3 ) );
        result.add( Property.OBJEKTART, record.nextString( 4 ) );

        Object objektnummer1 = record.nextString( 7 );  /* Objektnummer 1 (rechts)  */
        result.add( Property.OBJEKTNUMMER1, objektnummer1 );
        Object objektnummer2 = record.nextString( 7 );  /* Objektnummer 2 (link)  */
        result.add( Property.OBJEKTNUMMER2, objektnummer1 );

        if (objektnummer1.equals( "       " )) {        /* Nr. rechts frei  */
            result.add( Property.RICHTUNG, LINKS );
        }
        else if (objektnummer1.equals( "       " )) {   /* Nr. links frei   */
            result.add( Property.RICHTUNG, RECHTS );
        }
        else {                                          /* beide Nummernfelder belegt   */
            result.add( Property.RICHTUNG, BEIDE );

            //                /* Nachbarschaft merken - Objektteilnummer beruecksichtigen?    */
            //                /* CR 2001-03-10                        */
            //                fprintf( fp_nachbar, IDFORMAT TRENNER IDFORMAT "\n",
            //                    nummer2id( objekt1[i] ), nummer2id( objekt2[i] ) );
            //                if( ferror( fp_att ) ) perror( "Fehler beim Schreiben in fp_nachbar\n" );
        }

        String teilnummer1 = record.nextString( 3 );          /* Objektteilnummer 1 (rechts)  */
        result.add( Property.TEILNUMMER1, teilnummer1 );
        String teilnummer2 = record.nextString( 3 );          /* Objektteilnummer 2 (links)  */
        result.add( Property.TEILNUMMER2, teilnummer2 );

        record.skip( 2 );                               /* Linienteilung1 + Linienteilung2 : nicht verwertet */

        int n = record.nextWhf();
        if (n != 0 ) {                                  /* fuer Fachparameter   */
            log.warn( "WHF Fachparameter > 0" );
            while (n-- > 0) {
                record.skip( 20 );                      /* Fachparameter ueberlesen */
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
    private void objekt() 
    throws EdbsParseException {
        int n = record.nextWhf();   /* == 1, einmal Funktion des Objekts */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Objektfunktion != 1" );
        }        

        /* Ergebnis in folie[0], objektart[0], objekttyp, objekt1[0] */
        objektFunktion();

        n = record.nextWhf();       /* Wiederholungsfaktor Datengruppe 'Besondere Information' */
        while (n-- > 0) {
            besondereInfo();
        }
    }

    
    /*
     * Liest einmal die Datengruppe 'Funktion des Objekts' und
     * speichert die Angaben zu Folie, Objektart, -typ und -nummer
     * in globalen Variablen.
     * Schreibt sie gleichzeitig in fp_objects.
     *
     * Laesst aktuellen Anfangspunkt von 'label_schreiben(...)' in
     * eine von Objekttyp und Objektart abhaengige Datei schreiben.
     *
     * Gibt 0 zurueck, falls alles OK, sonst -1.
     */
    private void objektFunktion() {
        result.put( Property.FOLIE, record.nextInt( 3 ) );
        result.put( Property.OBJEKTART, record.nextInt( 4 ) );

        record.skip( 2 );               /* Aktualitaet des Objekts */

        char objekttyp = record.nextChar();
        result.put( Property.OBJEKTTYP, objekttyp );

        String objektnummer = record.nextString( 7 );
//        id = nummer2id( objekt1[0] );
        result.put( Property.OBJEKTNUMMER, objektnummer );

        record.skip( 2 );               /* Modelltyp */
        record.skip( 6 );               /* Entstehungsdatum */
        record.skip( 1 );               /* Veraenderungskennung */

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
     * @throws EdbsParseException 
     */
    private void besondereInfo() 
    throws EdbsParseException {
        int n = record.nextWhf();               /* == 1, einmal 'Besondere Information' */
        if (n != 1 ) {
            throw new EdbsParseException( "WHF Besondere Information != 1" );
        }

        int infoArt = record.nextInt( 2 );      /* 'Art der Information' */
        result.put( Property.INFO_ART, infoArt );

        record.skip( 2 );                       /* Kartentyp */
        record.skip( 6 );                       /* Signaturteilnummer */

        String text = record.nextString( 33 );  /* Datenelement 'Text' */
        result.put( Property.INFO, text );

        int geometrieArt = record.nextInt( 2 ); /* Art der Geometrieangabe */
        result.put( Property.GEOM_ART, geometrieArt );
        String teilnummer = record.nextString( 3 );
        result.put( Property.TEILNUMMER, teilnummer );   /* Objektteilnummer */

//        objektInfo( record );
        n = record.nextWhf();       /* Wiederholungsfaktor Datengruppe 'Geometrieangabe' */
        
        // Art 53 = Text in freier Ausrichtung (mit Richtungswinkel "TT")
        if (geometrieArt == 53) {
            record.skip( "TT13613             ".length() );
            n--;
        }
        
        while (n-- > 0) {
            geoAngabe( geometrieArt );
        }
    }
    
    
    /**
     * Liest die Geometrieangabe und speichert gefundene Punkte
     * mit der globalen 'id' und Art der Geometrie in fp_npos
     * (Namenpositionierung) ab.
     * @param geometrieArt 
     */
    private void geoAngabe( int geometrieArt ) {

        /* evtl. hier switch nach Art der Geometrie (Gerade, Schraffur, ...) */
//        if( n > 0 ) fprintf( fp_npos, IDFORMAT TRENNER "%2d", id, adg );

        Point gp = record.nextPoint();
        result.add( Property.PUNKTE, gp );
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
