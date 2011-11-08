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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.RecordModel;


/**
 * Modelliert einen EDBS Datensatz als {@link RecordModel}. Tatsächlich is kein Store
 * vorhanden, die Werte werden im Speicher in einer {@link Map} gehalten.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsRecord
        extends RecordModel {

    private static Log log = LogFactory.getLog( EdbsRecord.class );
    
    public Property<Integer> typeId = new Property( "typeId" );

    
    protected EdbsRecord() {
        super( new RecordState() );
    }

    
    public EdbsRecord( IRecordState record ) {
        super( record );
    }


    /**
     * Fake record state that stores properties in a {@link Map}.
     */
    static class RecordState
            implements IRecordState {
        
        private Map<String,Object>  properties = new HashMap();


        public Object id() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public <T> RecordState put( String name, T value ) {
            Object old = properties.put( name, value );
            //        if (old != null) {
            //            throw new IllegalStateException( "Property existiert bereits: " + name );
            //        }
            return this;
        }


        public <T> T get( String name ) {
            return (T)properties.get( name );
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


        public RecordState add( String name, Object value ) {
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
            return this;
        }

        
        public RecordState remove( String key ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public Iterator<Entry<String, Object>> iterator() {
            return properties.entrySet().iterator();
        }
    }
    
}
