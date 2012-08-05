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
package org.polymap.alkis.importer.fs;

import java.util.Date;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * An imported or generated or files in the data/ALKIS/... directory. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DataFile
        extends DefaultContentNode
        implements IContentFile {

    private static Log log = LogFactory.getLog( DataFile.class );

    
    public DataFile( IPath parentPath, IContentProvider provider, Object source ) {
        super( ((File)source).getName(), parentPath, provider, source );
    }

    public ImportContentProvider getProvider() {
        return (ImportContentProvider)super.getProvider();
    }

    public File getSource() {
        return (File)super.getSource();
    }

    public Long getContentLength() {
        return getSource().length();
    }

    public String getContentType( String accepts ) {
        return "text/plain";
    }

    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, BadRequestException {
        FileInputStream in = null;
        try {
            in = new FileInputStream( getSource() );
            IOUtils.copy( in, out );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }

    public Long getMaxAgeSeconds() {
        return 60l;
    }

    public Date getModifiedDate() {
        return new Date( getSource().lastModified() );
    }

}
