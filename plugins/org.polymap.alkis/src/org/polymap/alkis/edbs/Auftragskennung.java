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

import java.util.List;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;

import org.polymap.alkis.edbs.EdbsReader.RecordTokenizer;


/**
 * Liest aus dem EDBS-Auftragskennsatz (AKND, ULQA0000) die
 * Datenelemente 'Datenkennung-ALK' und 'Datenkennung-DLM'
 * und belegt die Variable 'datenmodell'.
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class Auftragskennung
        implements IEdbsRecordHandler {

    private static Log log = LogFactory.getLog( Auftragskennung.class );

    public static final int     ID = 1;

    public static final String  PROP_DIENSTSTELLE = "dienststelle";
    public static final String  PROP_AUSGABE = "ausgabe";
    public static final String  PROP_KENNUNG_ALK = "alk";
    public static final String  PROP_KENNUNG_ATKIS = "atkis";
    public static final String  PROP_COUNT = "count";
    public static final String  PROP_ERSTEINTRAG_DATUM = "ersteintragdatum";
    public static final String  PROP_AUSGABE_DATUM = "ausgabedatum";
    public static final String  PROP_MODIFIKATION_DATUM = "modifikationsdatum";
    
        
    public int canHandle( RecordTokenizer record ) throws IOException {
        if (record.operation.equals( "AKND" )
                && record.infoname.equals( "ULQA0000")) {
            return ID;
        }
        else {
            return 0;
        }
    }


    public List<EdbsRecord> handle( RecordTokenizer record ) throws IOException {
            EdbsRecord result = new EdbsRecord( ID );

            if (record.nextWhf() != 1 ) {
                throw new EdbsParseException( "Datengruppe 'Auftragskenndaten' kommt != 1-mal vor." );
            }
            
            result.put( PROP_DIENSTSTELLE, record.nextString( 14 ) ); /* Dienststelle */
            record.skip( 5 );  /* Auftragsnummer */
            record.skip( 1 );  /* weitere Gliederung */

            record.skip( 1 );  /* Pruefzeichen */
            record.skip( 1 );  /* Auftragsart */
            record.skip( 2 );  /* Aktualitaet des Auftrags */
            record.skip( 1 );  /* Integrationshinweis */

            record.skip( 2 );  /* Nummer der BGDB */

            record.skip( 11 ); /* Antragshinweis */

            record.skip( 1 );  /* Auftragskennung */
            record.skip( 8 );  /* Benutzungs-/Fortf.-art */
            result.put( PROP_AUSGABE, record.nextString( 32 ) );    /* Text fuer die Ausgabe */
            record.skip( 2 );  /* Verarbeitungsmodus */
            record.skip( 2 );  /* Anzahl Ausfertigungen */

            record.skip( 2 );  /* Punktdatenkennung */

            result.put( PROP_KENNUNG_ALK, record.nextString( 2 ) );  /* Datenkennung-ALK */

            record.skip( 2 );  /* Messungselementekennung */

            result.put( PROP_KENNUNG_ATKIS, record.nextString( 2 ) ); /* Datenkennung-DLM */

            /* ************************************************************************ */
            /* Auskommentiert am 29.5.96 wg. Problem der Erkennung von EDBS/ATKIS-Input */
            /* ************************************************************************ */
            /* if( strcmp( kennung_atkis,"  " ) != 0 ) {
                fprintf( stderr, "EBDS-Datei enthaelt ATKIS-Daten\n" ); */
//                datenmodell = ATKIS;
            /* }
            else if( strcmp( kennung_alk,"  " ) != 0 ) {
                fprintf( stderr, "EBDS-Datei enthaelt ALK-Daten\n" );
                datenmodell = ALK;
            }
            else {
                fprintf( stderr, "Datenmodell (ALK oder ATKIS) konnte nicht identifiziert werden." );
                exit( 1 );
            } */

            record.skip( 2 );  /* Datenkennung-DKM */
            record.skip( 50 ); /* unbelegt */

            record.skip( 1 );  /* Meridianstreifensystem der Ausgabe */
            record.skip( 1 );  /* Verarbeitungsstop */

            record.skip( 3 );  /* Verarbeitungsstatus */

            record.skip( 6 );  /* hoechste weitere Satznummer */
            result.put( PROP_COUNT, record.nextString( 6 ) ); /* Anzahl der weiteren Saetze */

            result.put( PROP_ERSTEINTRAG_DATUM, record.nextString( 6 ) ); /* Datum Ersteintrag */
            result.put( PROP_MODIFIKATION_DATUM, record.nextString( 6 ) ); /* Datum Modifikation */
            result.put( PROP_AUSGABE_DATUM, record.nextString( 6 ) ); /* Datum Ausgabe */

            record.skip( 14 ); /* zustaendige Stelle */
            record.skip( 12 ); /* Plausibilitaetssteuerung */
            record.skip( 1 );  /* Hinweis fuer Geometriebehandlung */

            record.skip( 8 );  /* Folgeverarbeitung */
            record.skip( 8 );
            record.skip( 8 );
            record.skip( 8 );

            record.skip( 1 );
            record.skip( 10 );
            record.skip( 10 );

            //fprintf( fp_auftrag, "\n" );
            return Collections.singletonList( result );
        }
}
