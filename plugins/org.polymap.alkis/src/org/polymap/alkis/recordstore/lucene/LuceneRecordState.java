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
package org.polymap.alkis.recordstore.lucene;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.polymap.alkis.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordState
        implements IRecordState {

    private static Log log = LogFactory.getLog( LuceneRecordState.class );

    public static final String  ID_FIELD = "identifier";
    
    private LuceneRecordStore   store;
    
    private Integer             id;

    private Document            doc;
    
    
    protected LuceneRecordState( LuceneRecordStore store, Integer id, Document doc ) {
        this.store = store;
        this.id = id;
        this.doc = doc;
    }

    
    Document getDocument() {
        return doc;
    }


    public Object id() {
        return id;
    }

    
    public <T> T put( String key, T value ) {
        assert key != null;
        assert value != null : "Value must not be null.";
        
        Fieldable old = doc.getFieldable( key );
        if (old != null) {
            doc.removeField( key );
        }
        boolean indexed = store.getIndexFieldSelector().accept( key );
        Fieldable field = store.valueCoders.encode( key, value, indexed );
        doc.add( field );
        log.debug( "Field: " + key + " = " + field.stringValue() );
        
        return (T)store.valueCoders.decode( old );
    }

    
    public void add( String key, Object value ) {
        assert key != null;
        assert value != null : "Value must not be null.";

        Field lengthField = (Field)doc.getFieldable( key + "_length" );
        int length = -1;
        
        if (lengthField == null) {
            length = 1;
            doc.add( new Field( key + "_length", "1", Store.YES, Index.NO ) );
        }
        else {
            length = Integer.parseInt( lengthField.stringValue() ) + 1;
            lengthField.setValue( String.valueOf( length ) );
        }
        
        StringBuilder arrayKey = new StringBuilder( 32 )
                .append( key ).append( '[' ).append( length-1 ).append( ']' );
        put( arrayKey.toString(), value );
    }

    
    public <T> T get( String key ) {
        Fieldable field = doc.getFieldable( key );
        return (T)store.valueCoders.decode( field );
    }

    public <T> List<T> getList( String key ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public void remove( String key ) {
        doc.removeField( key );
    }

    public Iterator<Entry<String, Object>> iterator() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
