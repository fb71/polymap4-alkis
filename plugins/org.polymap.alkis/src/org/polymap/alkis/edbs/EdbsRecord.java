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
package org.polymap.alkis.edbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsRecord
        implements Iterable<Map.Entry<String,Object>> {

    private static Log log = LogFactory.getLog( EdbsRecord.class );
    
    private int                 id;
    
    private Map<String,Object>  properties = new TreeMap();

    
    EdbsRecord( int id ) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    
    /**
     * Setzt den Wert für das Property mit dem angegebenen Namen.
     *
     * @param name
     * @param value
     */
    public void put( String name, Object value ) {
        Object old = properties.put( name, value );
//        if (old != null) {
//            throw new IllegalStateException( "Property existiert bereits: " + name );
//        }
    }

    /**
     * Setzt den Wert für das Property mit dem angegebenen Namen.
     *
     * @param name
     * @param value
     */
    public void put( Enum name, Object value ) {
        Object old = properties.put( name.name(), value );
//        if (old != null) {
//            throw new IllegalStateException( "Property existiert bereits: " + name );
//        }
    }

    /**
     * Liefert den Wert für das Property mit dem angegebenen Namen. Eventuell
     * kann das auch eine Liste mit Werten sein. 
     */
    public <T> T get( String name ) {
        return (T)properties.get( name );
    }

    /**
     * Liefert den Wert für das Property mit dem angegebenen Namen. Eventuell
     * kann das auch eine Liste mit Werten sein. 
     */
    public <T> T get( Enum name ) {
        return get( name.name() );
    }

    public <T> List<T> getList( String name ) {
        Object value = properties.get( name );
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        else if (value instanceof List) {
            return (List)value;
        }
        else {
            return Collections.singletonList( (T)value );
        }
    }
    
    public <T> List<T> getList( Enum name ) {
        return getList( name.name() );
    }
    
    /**
     * Fügt einen neuen Wert zum Property mit dem angegebenen Namen. Wenn
     * bereits ein oder mehrere Werte existieren, dann wird eine {@link List}
     * initialisiert.
     * 
     * @param name
     * @param value
     */
    public void add( String name, Object value ) {
        Object old = properties.put( name, value );
        if (old != null) {
            if (old instanceof List) {
                ((List)old).add( value );
                properties.put( name, old );
            }
            else {
                List list = new ArrayList();
                list.add( old );
                list.add( value );
                properties.put( name, list );
            }
        }
    }

    /**
     * Fügt einen neuen Wert zum Property mit dem angegebenen Namen. Wenn
     * bereits ein oder mehrere Werte existieren, dann wird eine {@link List}
     * initialisiert.
     * 
     * @param name
     * @param value
     */
    public void add( Enum name, Object value ) {
        add( name.name(), value );
    }
    
    public Iterator<Entry<String, Object>> iterator() {
        return properties.entrySet().iterator();
    }
    
}
