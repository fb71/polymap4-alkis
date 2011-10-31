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
import java.util.Properties;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;

import com.bradmcevoy.http.FileItem;

import org.eclipse.core.runtime.IPath;

import org.polymap.alkis.edbs.EdbsImporter;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * 
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
            "enctype=\"multipart/form-data\" method=\"post\">" +
            "<p>" +
            "Waehlen Sie eine Datei fuer den Import (EDBS, *.zip):<br/>" +
            "<input type=\"file\" name=\"datafile\" size=\"40\">" +
            "</p>" +
            "<div>" +
            "<input type=\"submit\" value=\"Senden\">" +
            "</div>" +
            "</form>";
    }

    
    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        // ZIP
        if (newName.toLowerCase().endsWith( "zip" )) {
            throw new BadRequestException( "Upload von ZIP-Dateien noch nicht verf�gbar." );
        }
        // plain file
        else {
            OutputStream fileOut = null;
            OutputStream reportOut = null;
            InputStream confIn = null;
            try {
                File dir = ((EdbsContentProvider)getProvider()).getDataDir();
                File f = new File( dir, newName );

                // fileOut
                fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
                TeeInputStream teeIn = new TeeInputStream( in, fileOut );
                
                // reportOut
                reportOut = new BufferedOutputStream( 
                        new FileOutputStream( new File( dir, newName + ".report" ) ) );
                
                // conf
                confIn = new FileInputStream( new File( dir, "import.conf" ) );
                Properties conf = new Properties();
                conf.load( confIn );
                
                // run import
                EdbsImporter importer = new EdbsImporter( conf, 
                        new InputStreamReader( teeIn, "ISO-8859-1" ), 
                        new PrintStream( reportOut, false, "ISO-8859-1" ) );
                importer.run();
                fileOut.flush();
                
                // reload node
                ((EdbsContentProvider)getProvider()).getContentSite().invalidateFolder( this );
                
                return new EdbsFile( getPath(), getProvider(), f );
            }
            finally {
                IOUtils.closeQuietly( fileOut );
                IOUtils.closeQuietly( reportOut );
                IOUtils.closeQuietly( confIn );
            }
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
