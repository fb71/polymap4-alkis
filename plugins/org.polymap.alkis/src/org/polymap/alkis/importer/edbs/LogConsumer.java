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

import java.util.Map;

/**
 * Log-Ausgabe der ankommenden {@link EdbsRecord}s.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LogConsumer
        implements IEdbsConsumer {

    public void consume( EdbsRecord record ) {
        System.out.println( "EDBS-Objekt: " + record.getClass().getSimpleName() );
        for (Map.Entry<String,Object> prop : record.state()) {
            System.out.println( "    Property: key=" + prop.getKey() + ", value=" + prop.getValue() );
        }
    }

    public void endOfRecords() {
    }
    
}