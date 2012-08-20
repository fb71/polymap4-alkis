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

import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;

import com.bradmcevoy.http.FileItem;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

import org.polymap.alkis.importer.edbs.EdbsImporter;

/**
 * 
 * <p/>
 * Impl.: Implements IContentWriteable to allow upload via browser.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class EdbsFolder
        extends DefaultContentFolder
        implements IContentPutable, IContentWriteable {

    public EdbsFolder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }

    
    public ImportContentProvider getProvider() {
        return (ImportContentProvider)super.getProvider();
    }

    protected ImportConfigFile getConfigFile() {
        return getProvider().getConfigFile();
    }
    
    protected File getDataDir() {
        getProvider();
        File edbsDir = new File( ImportContentProvider.getDataDir(), "edbs" );
        edbsDir.mkdir();
        return edbsDir;
    }

    
    public void sendDescription( OutputStream out, Range range, Map<String, String> params,
            String contentType )
            throws IOException {
        // FIXME hard coded servlet path
        String basePath = FilenameUtils.normalizeNoEndSeparator( getPath().toString() );
        String path = "/webdav" + basePath; // + "/" + r.getName();

        OutputStreamWriter writer = new OutputStreamWriter( out, "UTF-8" );
        writer.write( 
            "<h1>EDBS-Schnittstelle von POLYMAP3</h1>" +
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
            "</form>" );
        writer.flush();
        
        super.sendDescription( out, range, params, contentType );        
    }


    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        ImportContentProvider provider = getProvider();
        
        // ZIP
        if (newName.toLowerCase().endsWith( "zip" )) {
            ZipInputStream zipIn = new ZipInputStream( in );
            ZipEntry entry = null;
            while ((entry = zipIn.getNextEntry()) != null) {
                createNew( entry.getName(), zipIn );
                entry.clone();
            }
            return null;
        }
        // import.conf
        // XXX if the client sends changes via PUT -> redirect to ImportConfigFile 
        else if (newName.equals( getConfigFile().getName() )) {
            getConfigFile().replaceContent( in, length );
            return getConfigFile();
        }
        // plain file
        else {
            return createNew( newName, in );
        }
    }


    public void replaceContent( InputStream in, Long length )
    throws IOException, BadRequestException, NotAuthorizedException {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * Upload a new file via the browser interface created in
     * {@link #sendDescription(OutputStream, Range, Map, String)}.
     */
    public String processForm( Map<String,String> params, Map<String,FileItem> files )
    throws IOException, NotAuthorizedException, BadRequestException {
        for (FileItem item : files.values()) {
            createNew( item.getName(), item.getInputStream(), item.getSize(), "text/plain" );
        }
        return null;
    }
    
    
    /**
     * Creates a new EDBS file for the given input. The file is imported and features
     * are created by the {@link EdbsImporter} according the import.conf in the
     * edbsFolder. A report file is created containing the result.
     * 
     * @param newName
     * @param in
     * @return Newly created {@link DataFile} reflecting the created file.
     */
    protected IContentFile createNew( String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        OutputStream reportOut = null;
        try {
            File f = new File( getDataDir(), newName );

            // fileOut
            fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
            TeeInputStream teeIn = new TeeInputStream( in, fileOut );

            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( getDataDir(), newName + ".report" ) ) );

            // run import
            EdbsImporter importer = new EdbsImporter( getProvider().getConfigFile().properties(), 
                    new InputStreamReader( teeIn, "ISO-8859-1" ), 
                    new PrintStream( reportOut, false, "ISO-8859-1" ) );
            importer.run();
            fileOut.flush();

            // reload node
            getSite().invalidateFolder( this );

            return new DataFile( getPath(), getProvider(), f );
        }
        finally {
            IOUtils.closeQuietly( fileOut );
            IOUtils.closeQuietly( reportOut );
        }
    }

    
    /**
     * 
     */
    public void reImport( String name )
    throws IOException, NotAuthorizedException, BadRequestException {
        InputStream fileIn = null;
        OutputStream reportOut = null;
        try {
            File f = new File( getDataDir(), name );
            fileIn = new BufferedInputStream( new FileInputStream( f ) );
            
            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( getDataDir(), name + ".report" ) ) );

            // run import
            EdbsImporter importer = new EdbsImporter( getProvider().getConfigFile().properties(), 
                    new InputStreamReader( fileIn, "ISO-8859-1" ), 
                    new PrintStream( reportOut, false, "ISO-8859-1" ) );
            importer.run();

            // reload edbsFolder
            getSite().invalidateFolder( this );
        }
        finally {
            IOUtils.closeQuietly( fileIn );
            IOUtils.closeQuietly( reportOut );
        }
    }

}