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


/**
 * Parser Interface für die Behandlung der einzelnen Datensätze (Zeilen).
 * </p> 
 * Die einzelnen Zeilen werden vom {@link EdbsRecord} gelesen, wenn nötig
 * verschmolzen, und dann als {@link RecordTokenizer} an die Parser gegeben.
 * Der Parser entscheided ob er diesen Datensatz behandeln kann.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IEdbsRecordParser {

    /**
     * Prüft, ob der übergebene Datensatz von diesem Parser verarbeitet werden kann.
     * 
     * @param record
     * @return Die Satzart oder 0, wenn dieser Parser den {@link RecordTokenizer}
     *         nicht verarbeiten kann.
     * @throws EdbsParseException
     * @throws IOException
     */
    public int canHandle( RecordTokenizer record )
    throws IOException;

    /**
     * Liefert einen oder mehrere {@link EdbsRecord}s, die aus dem Datensatz geparst
     * wrden konnten.
     * 
     * @throws EdbsParseException
     * @throws IOException
     */
    public List<EdbsRecord> handle( RecordTokenizer record )
    throws IOException;
    
}
