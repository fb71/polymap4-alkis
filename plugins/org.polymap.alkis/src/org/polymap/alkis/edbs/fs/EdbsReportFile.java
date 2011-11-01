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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsReportFile
        extends DefaultContentNode
        implements IContentFile, IContentDeletable {

    private static Log log = LogFactory.getLog( EdbsReportFile.class );

    private File                f;
    
    
    public EdbsReportFile( IPath parentPath, IContentProvider provider, Object source ) {
        super( ((File)source).getName(), parentPath, provider, source );
        this.f = (File)source;
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

    
    public void delete()
    throws BadRequestException, NotAuthorizedException {
        f.delete();

        try {
            EdbsContentProvider provider = (EdbsContentProvider)getProvider();
            provider.reImport( StringUtils.removeEnd( getName(), ".report" ) );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
            //FileUtils.writeStringToFile( f, e.toString() );
        }
    }

    
    public Long getMaxAgeSeconds() {
        return 60l;
    }

    
    public Date getModifiedDate() {
        return new Date( f.lastModified() );
    }

}
