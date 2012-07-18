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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.alkis.edbs.EdbsImporter;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * Provides content nodes for {@link IMap} and {@link ILayer} and a 'projects' node
 * as root for this structure.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( EdbsContentProvider.class );

    private File                    dir;
    
    private EdbsFolder              folder;
    
    EdbsConfigFile                  configFile;
    
    
    public EdbsContentProvider() {
        dir = new File( Polymap.getWorkspacePath().toFile(), "edbs" );
        dir.mkdirs();
    }

    
    public File getDataDir() {
        return dir;
    }

    
    public List<? extends IContentNode> getChildren( IPath path ) {
        // check admin
        if (!SecurityUtils.isAdmin( Polymap.instance().getPrincipals())) {
            return null;
        }

        // folder
        if (path.segmentCount() == 0) {
            if (folder == null) {
                folder = new EdbsFolder( "EDBS", path, this );
            }
            return Collections.singletonList( folder );
        }

        // files
        IContentFolder parent = getSite().getFolder( path );
        if (parent instanceof EdbsFolder) {
            List<IContentNode> result = new ArrayList();
            
            if (configFile == null) {
                configFile = new EdbsConfigFile( parent.getPath(), this, dir );
            }
            result.add( configFile );
            
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith( ".report" )) {
                    result.add( new EdbsReportFile( parent.getPath(), this, f ) );
                }
                else {
                    result.add( new EdbsFile( parent.getPath(), this, f ) );
                }
            }
            return result;
        }
        return null;
    }


    /**
     * Creates a new EDBS file for the given input. The file is imported and features
     * are created by the {@link EdbsImporter} according the import.conf in the
     * folder. A report file is created containing the result.
     * 
     * @param newName
     * @param in
     * @return Newly created {@link EdbsFile} reflecting the created file.
     */
    public IContentFile createNew( String newName, InputStream in )
    throws IOException, NotAuthorizedException, BadRequestException {
        OutputStream fileOut = null;
        OutputStream reportOut = null;
        try {
            File f = new File( dir, newName );

            // fileOut
            fileOut = new BufferedOutputStream( new FileOutputStream( f ) );
            TeeInputStream teeIn = new TeeInputStream( in, fileOut );

            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( dir, newName + ".report" ) ) );

            // run import
            EdbsImporter importer = new EdbsImporter( configFile.properties(), 
                    new InputStreamReader( teeIn, "ISO-8859-1" ), 
                    new PrintStream( reportOut, false, "ISO-8859-1" ) );
            importer.run();
            fileOut.flush();

            // reload node
            getSite().invalidateFolder( folder );

            return new EdbsFile( folder.getPath(), this, f );
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
            File f = new File( dir, name );
            fileIn = new BufferedInputStream( new FileInputStream( f ) );
            
            // reportOut
            reportOut = new BufferedOutputStream( 
                    new FileOutputStream( new File( dir, name + ".report" ) ) );

            // run import
            EdbsImporter importer = new EdbsImporter( configFile.properties(), 
                    new InputStreamReader( fileIn, "ISO-8859-1" ), 
                    new PrintStream( reportOut, false, "ISO-8859-1" ) );
            importer.run();

            // reload folder
            getSite().invalidateFolder( folder );
        }
        finally {
            IOUtils.closeQuietly( fileIn );
            IOUtils.closeQuietly( reportOut );
        }
    }


}
