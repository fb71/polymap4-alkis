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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;

import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * Provides content nodes for {@link IMap} and {@link ILayer} and a 'projects' node
 * as root for this structure.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( EdbsContentProvider.class );

    private IContentSite            contentSite;
    
    private File                    dir;
    
    
    public EdbsContentProvider() {
        dir = new File( Polymap.getWorkspacePath().toFile(), "edbs" );
        dir.mkdirs();
    }

    
    public File getDataDir() {
        return dir;
    }

    
    public IContentSite getContentSite() {
        return contentSite;
    }


    public List<? extends IContentNode> getChildren( IPath path, IContentSite site ) {
        this.contentSite = site;
        
        // folder
        if (path.segmentCount() == 0) {
            String name = "EDBS";
            return Collections.singletonList( new EdbsFolder( name, path, this ) );
        }

        // files
        IContentFolder parent = site.getFolder( path );
        if (parent instanceof EdbsFolder) {
            List<IContentNode> result = new ArrayList();
            
            result.add( new EdbsConfigFile( parent.getPath(), this,
                    new File( dir, "import.conf" ) ) );
            
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

}
