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
package org.polymap.alkis.recordstore;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The state of a single record in an {@link IRecordStore}. Concrete implementations
 * are provided by the different store implementations. The record state gives plain
 * access to the values of a record, no caching of values is done nore any other
 * fancy stuff.
 * <p/>
 * This interface can be used by clients to directly access the state of an record,
 * or a {@link RecordModel} can be used to model properties with type and name.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IRecordState
        extends Iterable<Map.Entry<String,Object>> {

    /**
     * The identifier of this record. The identifier is assigned by the store.
     * 
     * @return The identifier of this record, or null if this record is not yet
     *         stored.
     */
    public abstract Object id();
    
    /**
     * Stores the given value for the given key.
     *
     * @param key
     * @param value
     * @return The value previously mapped to the given key, or null.
     */
    public abstract <T> T put( String key, T value );

    /**
     * F�gt einen neuen Wert zum Property mit dem angegebenen Namen. Wenn
     * bereits ein oder mehrere Werte existieren, dann wird eine {@link List}
     * initialisiert.
     * 
     * @param name
     * @param value
     */
    public abstract void add( String key, Object value );

    /**
     *  
     */
    public abstract <T> T get( String key );

    /**
     * 
     * @param <T>
     * @return The list of values, or the empty list if there is no mapping for the
     *         given key.
     */
    public abstract <T> List<T> getList( String key );
    
    public abstract void remove( String key );
    
    /*
     * 
     */
    public abstract Iterator<Map.Entry<String, Object>> iterator();

}
