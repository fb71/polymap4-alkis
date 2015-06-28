/* 
 * polymap.org
 * Copyright (C) 2011-2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.alkis;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

import org.polymap.alkis.model.AlkisRepository;

/**
 *
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AlkisPlugin
        extends AbstractUIPlugin {

	public static final String             ID = "org.polymap.alkis";

	public static final MinWidthConstraint MIN_COLUMN_WIDTH = new MinWidthConstraint( 450, 1 );

	private static AlkisPlugin             instance;
	

    public static AlkisPlugin instance() {
        return instance;
    }


    // instance *******************************************
    
    private ServiceTracker httpServiceTracker;


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        
        AlkisRepository.init();
        
        new Job( "ALKIS-Volltext-Update" ) {
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                try {
                    AlkisRepository repo = AlkisRepository.instance.get();
                    repo.updateFulltext( Integer.MAX_VALUE );
                    return Status.OK_STATUS;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        }.schedule( 3000 );

        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                HttpService httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    try {
                        httpService.registerResources( "/alkisres", "/resources", null );
                    }
                    catch (NamespaceException e) {
                        throw new RuntimeException( e );
                    }
                }
                return httpService;
            }
        };
        httpServiceTracker.open();
    }


    public void stop( BundleContext context ) throws Exception {
        httpServiceTracker.close();
        httpServiceTracker = null;
        
        instance = null;
        super.stop( context );
    }

}
