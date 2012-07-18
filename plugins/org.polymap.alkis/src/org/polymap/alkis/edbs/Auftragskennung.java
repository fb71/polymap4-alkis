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

import java.util.Collections;
import java.util.List;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Liest aus dem EDBS-Auftragskennsatz (AKND, ULQA0000) die
 * Datenelemente 'Datenkennung-ALK' und 'Datenkennung-DLM'
 * und belegt die Variable 'datenmodell'.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class Auftragskennung
        implements IEdbsRecordParser {

    private static Log log = LogFactory.getLog( Auftragskennung.class );

    /**
     * 
     */
    public static class Record
            extends EdbsRecord {

        public static final int     ID = 1;

        public static final Record  TYPE = type( Record.class );

        public Record() {
            state().put( this.typeId.name(), ID );
        }

        public Property<String> dienststelle       = new Property( "dienststelle" );
        public Property<String> ausgabe            = new Property( "ausgabe" );
        public Property<String> alk                = new Property( "alk" );
        public Property<String> atkis              = new Property( "atkis" );
        public Property<String> count              = new Property( "count" );
        public Property<String> ersteintragdatum   = new Property( "ersteintragdatum" );
        public Property<String> ausgabedatum       = new Property( "ausgabedatum" );
        public Property<String> modifikationsdatum = new Property( "modifikationsdatum" );
        
    }    

    
    public int canHandle( RecordTokenizer record ) throws IOException {
        if (record.operation.equals( "AKND" )
                && record.infoname.equals( "ULQA0000")) {
            return Record.ID;
        }
        else {
            return 0;
        }
    }


    public List<? extends EdbsRecord> handle( RecordTokenizer record ) throws IOException {
            Record result = new Record();

            if (record.nextWhf() != 1 ) {
                throw new EdbsParseException( "Datengruppe 'Auftragskenndaten' kommt != 1-mal vor." );
            }
            
            result.dienststelle.put( record.nextString( 14, "???" ) ); /* Dienststelle */
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
            result.ausgabe.put( record.nextString( 32, "???" ) );    /* Text fuer die Ausgabe */
            record.skip( 2 );  /* Verarbeitungsmodus */
            record.skip( 2 );  /* Anzahl Ausfertigungen */

            record.skip( 2 );  /* Punktdatenkennung */

            result.alk.put( record.nextString( 2, "???" ) );  /* Datenkennung-ALK */

            record.skip( 2 );  /* Messungselementekennung */

            result.atkis.put( record.nextString( 2, "???" ) ); /* Datenkennung-DLM */

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
            result.count.put( record.nextString( 6, "???" ) ); /* Anzahl der weiteren Saetze */

            result.ersteintragdatum.put( record.nextString( 6, "???" ) ); /* Datum Ersteintrag */
            result.modifikationsdatum.put( record.nextString( 6, "???" ) ); /* Datum Modifikation */
            result.ausgabe.put( record.nextString( 6, "???" ) ); /* Datum Ausgabe */

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
