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

import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import com.bradmcevoy.http.FileItem;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * 
 * <p/>
 * Impl.: Implements IContentWriteable to allow upload via browser.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsFolder
        extends DefaultContentFolder
        implements IContentPutable, IContentWriteable {

    public EdbsFolder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }

    
    public String getDescription( String contentType ) {
        // FIXME hard coded servlet path
        String basePath = FilenameUtils.normalizeNoEndSeparator( getPath().toString() );
        String path = "/webdav" + basePath; // + "/" + r.getName();

        return "<b>Dieses Verzeichnis ist die EDBS-Schnittstelle von POLYMAP3.</b>" +
            "<p/>" +
            "<form action=\"" + path + "\"" +
            "  enctype=\"multipart/form-data\" method=\"post\">" +
            "  <p>" +
            "    Waehlen Sie eine Datei fuer den Import (EDBS, *.zip):<br/>" +
            "    <input type=\"file\" name=\"datafile\" size=\"40\">" +
            "  </p>" +
            "  <div>" +
            "    <input type=\"submit\" value=\"Senden\">" +
            "  </div>" +
            "</form>";
    }

    
    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        EdbsContentProvider provider = (EdbsContentProvider)getProvider();
        
        // ZIP
        if (newName.toLowerCase().endsWith( "zip" )) {
            ZipInputStream zipIn = new ZipInputStream( in );
            ZipEntry entry = null;
            while ((entry = zipIn.getNextEntry()) != null) {
                provider.createNew( entry.getName(), zipIn );
                entry.clone();
            }
            return null;
        }
        // import.conf
        // XXX if the client sends changes via PUT -> redirect to EdbsConfigFile 
        else if (newName.equals( provider.configFile.getName() )) {
            provider.configFile.replaceContent( in, length );
            return provider.configFile;
        }
        // plain file
        else {
            return provider.createNew( newName, in );
        }
    }


    public void replaceContent( InputStream in, Long length )
    throws IOException, BadRequestException, NotAuthorizedException {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * Upload a new file via the browser interface created in
     * {@link #getDescription(String)}.
     */
    public String processForm( Map<String,String> params, Map<String,FileItem> files )
    throws IOException, NotAuthorizedException, BadRequestException {
        
        for (FileItem item : files.values()) {
            createNew( item.getName(), item.getInputStream(), item.getSize(), "text/plain" );
        }
        
        return null;
    }
    
}
