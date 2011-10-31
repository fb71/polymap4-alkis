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
package org.polymap.alkis.edbs.fs;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.FileItem;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsConfigFile
        extends DefaultContentNode
        implements IContentFile, IContentWriteable {

    private static Log log = LogFactory.getLog( EdbsConfigFile.class );

    private File                f;
    
    
    public EdbsConfigFile( IPath parentPath, IContentProvider provider, Object source ) {
        super( ((File)source).getName(), parentPath, provider, source );
        this.f = (File)source;
        
        if (!f.exists()) {
            Properties conf = new Properties();
            //jdbc.postgis://postgres:lka2010@10.0.16.15:5432/osm
            conf.put( "serviceURL", "jdbc.h2:ALK-Mittelsachsen" );
            conf.put( "param", "wert" );
            
            FileWriterWithEncoding out = null;
            try {
                out = new FileWriterWithEncoding( f, "ISO-8859-1" );
                conf.store( out, "Kommentar: ..." );
            }
            catch (IOException e) {
                log.warn( "", e );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }
    }

    public Long getContentLength() {
        return f.length();
    }

    public String getContentType( String accepts ) {
        return "text/plain";
    }

    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, BadRequestException {
        FileInputStream in = null;
        try {
            in = new FileInputStream( f );
            IOUtils.copy( in, out );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }

    public void replaceContent( InputStream in, Long length )
            throws IOException, BadRequestException, NotAuthorizedException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public String processForm( Map<String, String> params, Map<String, FileItem> files )
    throws IOException, BadRequestException, NotAuthorizedException {
        throw new RuntimeException( "not yet implemented." );
    }

    public Long getMaxAgeSeconds() {
        return 60l;
    }

    public Date getModifiedDate() {
        return new Date( f.lastModified() );
    }

}
