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

import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.NotAuthorizedException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Alkis1ReportFile
        extends DataFile
        implements IContentFile, IContentDeletable {

    private static Log log = LogFactory.getLog( Alkis1ReportFile.class );

    
    public Alkis1ReportFile( IPath parentPath, IContentProvider provider, Object source ) {
        super( parentPath, provider, source );
    }

    
    public void delete()
    throws BadRequestException, NotAuthorizedException {
        getSource().delete();

        try {
            Alkis1Folder folder = (Alkis1Folder)getSite().getFolder( getParentPath() );
            folder.reImport( StringUtils.removeEnd( getName(), ".report" ) );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
            //FileUtils.writeStringToFile( f, e.toString() );
        }
    }
    
}
