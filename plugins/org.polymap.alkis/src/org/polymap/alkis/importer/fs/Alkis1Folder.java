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
import java.util.Properties;

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

import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.importer.alkis1.Alkis1Importer;
import org.polymap.alkis.importer.alkis1.GmkImporter;
import org.polymap.alkis.importer.alkis1.NutzungenImporter;
import org.polymap.alkis.importer.edbs.EdbsImporter;

/**
 * 
 * <p/>
 * Impl.: Implements IContentWriteable to allow upload via browser.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Alkis1Folder
        extends DefaultContentFolder
        implements IContentPutable, IContentWriteable {

    public Alkis1Folder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, new File( ImportContentProvider.getDataDir(), "alkis1" ) );
        getDataDir().mkdir();
    }
    
    public ImportContentProvider getProvider() {
        return (ImportContentProvider)super.getProvider();
    }

    protected ImportConfigFile getConfigFile() {
        return getProvider().getConfigFile();
    }

    protected File getDataDir() {
        return (File)getSource();
    }

    public void sendDescription( OutputStream out, Range range, Map<String, String> params,
            String contentType )
            throws IOException {
        // FIXME hard coded servlet path
        String basePath = FilenameUtils.normalizeNoEndSeparator( getPath().toString() );
        String path = "/webdav" + basePath; // + "/" + r.getName();

        OutputStreamWriter writer = new OutputStreamWriter( out, "UTF8" );
        writer.write( 
            "<h1>ALKIS1-Schnittstelle von POLYMAP3</h1>" +
            "<p/>" +
            "<form action=\"" + path + "\"" +
            "  enctype=\"multipart/form-data\" method=\"post\">" +
            "  <p>" +
            "    Waehlen Sie eine Datei fuer den Import (ALKIS1, *.zip):<br/>" +
            "    <input type=\"file\" name=\"datafile\" size=\"40\">" +
            "  </p>" +
            "  <div>" +
            "    <input type=\"submit\" value=\"Senden\">" +
            "  </div>" +
            "</form>" );
        writer.flush();
        
        super.sendDescription( out, range, params, contentType );        
    }


    /**
     * Upload ZIP or plan file.
     */
    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        ImportContentProvider provider = getProvider();
        
        // ALKIS1 ZIP
        if (newName.toLowerCase().endsWith( "zip" )) {
            importAlkis1( newName, in );
            return null;
        }
        // Nutzungsarten
        else if (newName.toLowerCase().contains( "nutzung" )) {
            importNutzungen( newName, in );
            return null;
        }
        // Gemarkungen
        else if (newName.toLowerCase().startsWith( "gmk" )) {
            importGemarkungen( newName, in );
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
            throw new BadRequestException( "ALKIS-Daten müssen als ZIP-Datei übergeben werden." );
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
     * Creates a new ALKIS file for the given input. The file is imported and features
     * are created by the {@link Alkis1Importer} according the import.conf in the
     * edbsFolder. A report file is created containing the result.
     * 
     * @param newName
     * @param in
     * @return Newly created {@link DataFile} reflecting the created file.
     */
    protected IContentFile importAlkis1( String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        OutputStream reportOut = null;
        try {
            getProvider();
            File f = new File( getDataDir(), newName );

            // fileOut
            fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
            TeeInputStream teeIn = new TeeInputStream( in, fileOut );

            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( getDataDir(), newName + ".report" ) ) );
            ReportLog report = new ReportLog( new PrintStream( reportOut, false, "ISO-8859-1" ) );

            // run import
            Properties properties = getProvider().getConfigFile().properties();
            Alkis1Importer importer = new Alkis1Importer( teeIn, report, null );
            importer.run();
            
            fileOut.flush();
            reportOut.flush();

            return new DataFile( getPath(), getProvider(), f );
        }
        finally {
            IOUtils.closeQuietly( fileOut );
            IOUtils.closeQuietly( reportOut );
            
            // reload node
            getSite().invalidateFolder( this );
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
            getProvider();
            File edbsDir = new File( ImportContentProvider.getDataDir(), "edbs" );
            File f = new File( edbsDir, name );
            fileIn = new BufferedInputStream( new FileInputStream( f ) );
            
            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( edbsDir, name + ".report" ) ) );

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

    
    /**
     * The stream should contain a CSV file. The file is imported and features
     * are created by the {@link Alkis1Importer} according the import.conf in the
     * edbsFolder. A report file is created containing the result.
     * 
     * @param newName
     * @param in
     * @return Newly created {@link DataFile} reflecting the created file.
     */
    protected IContentFile importNutzungen( String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        OutputStream reportOut = null;
        try {
            getProvider();
            File f = new File( getDataDir(), newName );

            // fileOut
            fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
            TeeInputStream teeIn = new TeeInputStream( in, fileOut );

            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( getDataDir(), newName + ".report" ) ) );
            ReportLog report = new ReportLog( new PrintStream( reportOut, false, "ISO-8859-1" ) );

            // run import
            Properties properties = getProvider().getConfigFile().properties();
            NutzungenImporter importer = new NutzungenImporter( teeIn, report, null );
            importer.run();
            
            fileOut.flush();
            reportOut.flush();

            return new DataFile( getPath(), getProvider(), f );
        }
        finally {
            IOUtils.closeQuietly( fileOut );
            IOUtils.closeQuietly( reportOut );
            
            // reload node
            getSite().invalidateFolder( this );
        }
    }    

    
    /**
     * The stream should contain a CSV file. The file is imported and features
     * are created by the {@link Alkis1Importer} according the import.conf in the
     * edbsFolder. A report file is created containing the result.
     * 
     * @param newName
     * @param in
     * @return Newly created {@link DataFile} reflecting the created file.
     */
    protected IContentFile importGemarkungen( String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        OutputStream reportOut = null;
        try {
            getProvider();
            File f = new File( getDataDir(), newName );

            // fileOut
            fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
            TeeInputStream teeIn = new TeeInputStream( in, fileOut );

            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( getDataDir(), newName + ".report" ) ) );
            ReportLog report = new ReportLog( new PrintStream( reportOut, false, "ISO-8859-1" ) );

            // run import
            Properties properties = getProvider().getConfigFile().properties();
            GmkImporter importer = new GmkImporter( teeIn, report, null );
            importer.run();
            
            fileOut.flush();
            reportOut.flush();

            return new DataFile( getPath(), getProvider(), f );
        }
        finally {
            IOUtils.closeQuietly( fileOut );
            IOUtils.closeQuietly( reportOut );
            
            // reload node
            getSite().invalidateFolder( this );
        }
    }    

}
