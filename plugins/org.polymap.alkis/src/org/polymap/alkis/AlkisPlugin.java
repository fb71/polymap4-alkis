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

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

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


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }


    public void stop( BundleContext context ) throws Exception {
        instance = null;
        super.stop( context );
    }

}
