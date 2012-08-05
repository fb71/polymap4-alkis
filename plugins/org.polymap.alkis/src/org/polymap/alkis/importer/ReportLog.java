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
package org.polymap.alkis.importer;

import java.util.Date;

import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ReportLog
        extends SimpleLog
        implements Log {

    private PrintStream[]           outs;
    
    
    public ReportLog( PrintStream... outs ) {
        super( "Import" );
        this.outs = outs;
        info( "Start: " + new Date() );
    }

    protected void write( StringBuffer buffer ) {
        String msg = buffer.toString();
        for (PrintStream out : outs) {
            out.println( msg );
        }
    }

    public void flush() {
        for (PrintStream out : outs) {
            out.flush();
        }
    }
    
}
