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

import java.util.Arrays;
import java.util.Collection;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.opengis.feature.simple.SimpleFeature;

import org.apache.commons.lang3.StringUtils;

import org.polymap.alkis.model.AA_Objekt.Association;
import org.polymap.alkis.model.AA_Objekt.ManyAssociation;
import org.polymap.alkis.model.AA_Objekt.OptionalAssociation;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityHierachyPrinter {

    public static EntityHierachyPrinter on( Entity entity, AssociationFilter assocFilter ) {
        return new EntityHierachyPrinter( entity, assocFilter );
    }
    
    // instance *******************************************
    
    private PrintStream         out = System.out;
    
    private Entity              start;
    
    private int                 prefix;
    
    private int                 indent = 4;
    
    private AssociationFilter   assocFilter;
    
    /**
     * 
     */
    public interface AssociationFilter {
        boolean apply( Entity entity, String assocName, Class assocType );
    }
    
    public EntityHierachyPrinter( Entity start, AssociationFilter assocFilter ) {
        this.start = start;
        this.assocFilter = assocFilter;
    }


    public void run() {
        printEntity( start );
    }

    
    protected void printEntity( Entity entity ) {
        println( entity.info().getName() );
        printFeature( (SimpleFeature)entity.state() );
        printProperties( entity );
        printAssociations( entity );
    }

    
    protected void printAssociations( Entity entity ) {
        prefix += indent;
        println( "Associations:" );
        prefix += indent;
        for (Field f : entity.getClass().getFields()) {
            Class<?> type = f.getType();
            if (type.equals( Association.class ) 
                    || type.equals( ManyAssociation.class ) 
                    || type.equals( OptionalAssociation.class ) ) {
                
                String assocName = f.getName();
                Class<?> assocType = (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
                if (assocFilter.apply( entity, assocName, assocType )) {
                    try {
                        if (type.equals( Association.class )) {
                            printEntity( ((Association)f.get( entity )).get() );
                        }
                        else if (type.equals( OptionalAssociation.class )) {
                            printEntity( ((Association)f.get( entity )).get() );
                        }
                        else if (type.equals( ManyAssociation.class )) {
                            ManyAssociation assoc = (ManyAssociation)f.get( entity );
                            assoc.get().stream().forEach( e -> printEntity( (Entity)e ) );
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }
        }
        prefix -= indent*2;
    }

    
    protected void printProperties( Entity entity ) {
        prefix += indent;
        println( "Properties:" );
        prefix += indent;
        Collection<PropertyInfo> pInfos = entity.info().getProperties();
        pInfos.stream().forEach( pInfo -> {
            try {
                println( pInfo.getName(), " = ", ((Property)pInfo.get( entity )).get() );
            }
            catch (Exception e) {
                println( "Fehler bei: " + pInfo.getName() );
            }
        });
        prefix -= indent*2;
    }
    
    
    protected void printFeature( SimpleFeature feature ) {
        prefix += indent;
        println( "Feature: " + feature.getID() );
        prefix += indent;
        feature.getProperties().stream().forEach( p -> println( p.getName(), " = ", p.getValue() ) );
        prefix -= indent*2;
    }
    
    
    protected void println( Object... parts ) {
        print( StringUtils.leftPad( "", prefix, ' ' ) );
        print( parts );
        out.println( "" );
    }

    
    protected void print( Object... parts ) {
        Arrays.asList( parts ).stream().forEach( part -> { 
            out.print( part != null ? part.toString() : "[null]" );
        });
    }

}