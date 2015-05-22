/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.Association;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.EntityRuntimeContext;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ComputedAssociation<T extends Entity>
        implements Association<T> {

    private static Log log = LogFactory.getLog( ComputedAssociation.class );
    
    private EntityRuntimeContext    context;

    private Property<String>        prop;

    private Class<T>                targetClass;


    public ComputedAssociation( Class<T> targetClass , EntityRuntimeContext context , Property<String> prop ) {
        this.targetClass = targetClass;
        this.context = context;
        this.prop = prop;
    }

    @Override
    public T get() {
        return context.getUnitOfWork().entity( targetClass, prop.get() );
    }

    @Override
    public void set( T value ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyInfo info() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
