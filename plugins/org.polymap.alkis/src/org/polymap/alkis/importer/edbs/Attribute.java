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
package org.polymap.alkis.importer.edbs;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class Attribute
        implements IEdbsRecordParser {

    private static Log log = LogFactory.getLog( Attribute.class );

    /**
     * 
     */
    public static class Record
            extends EdbsRecord {

        public static final int     ID = 6;

        public static final Record  TYPE = type( Record.class );

        public Record() {
            state().put( this.typeId.name(), ID );
        }

        public Property<String> objektnummer = new Property( "objektnummer" );
        public Property<String> teilnummer = new Property( "teilnummer" );
        
    }    



    public int canHandle( RecordTokenizer record )
    throws IOException {
        if ((record.operation.equals( "BSPE" ) || record.operation.equals( "FEIN" ))
                && record.infoname.equals( "ULTANN  ")) {
            return Record.ID;
        }
        else {
            return 0;
        }
    }

    
    public List<EdbsRecord> handle( RecordTokenizer record )
    throws IOException {
        int i = record.nextWhf();
        List<EdbsRecord> result = new ArrayList( i );
        while (i-- > 0) {
            result.add( handleOne( record ) );
        }
        return result;
    }
    
    
    protected EdbsRecord handleOne( RecordTokenizer record )
    throws IOException {
        int i = record.nextWhf();       /* i muss 1 sein    */
        if (i != 1) {
            throw new EdbsParseException( "WHF Attributkennzeichen != 1" );
        }

        Record result = new Record();
        
        result.objektnummer.put( record.nextString( 7, "???" ) );/* Objektnummer     */
        result.teilnummer.put( record.nextString( 3, "???" ) ); /* Objektteilnummer     */
        record.skip( 1 );               /* Pruefzeichen     */
        record.skip( 2 );               /* Aktualitaet      */

        i = record.nextWhf();           /* Datengruppe Attribut */
        
        /* auch Attributsaetze ohne Datengruppe Attribut kommen vor */
        while (i-- > 0) {
            String att_typ = record.nextString( 4, "???" );
            String att_wert = record.nextString( 7, "???" );

            result.state().put( att_typ, att_wert );
            
//            fprintf( fp_att, IDFORMAT TRENNER "%3d" TRENNER "%.4s" TRENNER "%7d" "\n",
//                    nummer2id( obj_nr ), atoi( teil_nr ), att_typ, atoi( att_wert ) );
//            if( ferror( fp_att ) ) perror( "Fehler beim Schreiben in fp_att\n" );
        }
        return result;
    }

}
