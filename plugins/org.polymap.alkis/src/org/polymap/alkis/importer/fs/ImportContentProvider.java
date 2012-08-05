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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * Provides content nodes for {@link IMap} and {@link ILayer} and a 'projects' node
 * as root for this structure.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImportContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( ImportContentProvider.class );

    private static File             dataDir;
    
    public static File getDataDir() {
        if (dataDir == null) {
            dataDir = new File( Polymap.getDataDir(), "org.polymap.alkis" );
            dataDir.mkdirs();
        }
        return dataDir;
    }
    
    
    // instance *******************************************
    
    private ImportConfigFile        configFile;
    
    
    public ImportContentProvider() {
    }

    
    public ImportConfigFile getConfigFile() {
        if (configFile == null) {
            configFile = new ImportConfigFile( Path.fromPortableString( "/" ), this, dataDir );
        }
        return configFile;
    }


    public List<? extends IContentNode> getChildren( IPath path ) {
        // check admin
        if (!SecurityUtils.isAdmin( Polymap.instance().getPrincipals())) {
            return null;
        }

        // importFolder
        if (path.segmentCount() == 0) {
            return Collections.singletonList( new ImportFolder( "ALKIS", path ) );
        }
                
        // folders
        IContentFolder parent = getSite().getFolder( path );
        if (parent instanceof ImportFolder) {
            List<IContentNode> result = new ArrayList();
            result.add( new EdbsFolder( "EDBS", path, this ) );
            result.add( new Alkis1Folder( "ALKIS1", path, this ) );
            result.add( getConfigFile() );
            return result;
        }

        // EDBS files
        else if (parent instanceof EdbsFolder) {
            List<IContentNode> result = new ArrayList();
            for (File f : ((EdbsFolder)parent).getDataDir().listFiles()) {
                if (f.getName().endsWith( ".report" )) {
                    result.add( new EdbsReportFile( parent.getPath(), this, f ) );
                }
                else {
                    result.add( new DataFile( parent.getPath(), this, f ) );
                }
            }
            return result;
        }
        return null;
    }

    
    /*
     * 
     */
    class ImportFolder
            extends DefaultContentFolder
            implements IContentFolder {

        public ImportFolder( String name, IPath parentPath ) {
            super( name, parentPath, ImportContentProvider.this, null );
        }
        
        public void sendDescription( OutputStream out, Range range, Map<String, String> params,
                String contentType )
                throws IOException {
            // FIXME hard coded servlet path
            String basePath = FilenameUtils.normalizeNoEndSeparator( getPath().toString() );
            String path = "/webdav" + basePath; // + "/" + r.getName();

            OutputStreamWriter writer = new OutputStreamWriter( out, "ISO-8859-1" );
            writer.write( 
                "<h1>ALKIS-Import-Schnittstellen von POLYMAP3</h1>" +
                "<p>" +
                "Die Unterverzeichnisse stellen jeweils eigene Schittstellen für verschiedene " + 
                "Datenformate bereit. Alle importierten Daten werden dabei in einen Datenbestand " +
                "integriert." +  
                "<p>" +
                "Die Konfiguration erfolgt über das Konfigurationsfile in diesem Verzeichnis. Nähere " +
                "Informationen zur Bedienung entnehmen Sie der Datei selber." +
                "</p>" );
            writer.flush();
            
            super.sendDescription( out, range, params, contentType );        
        }

    }
    
}
