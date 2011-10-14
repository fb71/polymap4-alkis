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

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.polymap.alkis.recordstore.QueryExpression;

/**
 * Provides common base methods.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class ValueCoders {
    
    public static final LuceneValueCoder[] DEFAULT_CODERS = new LuceneValueCoder[] {
            new StringValueCoder()
    };

    // instance *******************************************
    
    private LuceneRecordStore           store;
    
    private LuceneValueCoder[]          valueCoders = DEFAULT_CODERS;
    

    protected ValueCoders( LuceneRecordStore store ) {
        this.store = store;
    }


    public void addValueCoder( LuceneValueCoder valueCoder ) {
        // add first, keep StringValueCoder last
        valueCoders = (LuceneValueCoder[])ArrayUtils.add( valueCoders, 0, valueCoder );
    }

    
    public final Fieldable encode( String key, Object value, boolean indexed ) {
        for (LuceneValueCoder valueCoder : valueCoders) {
            Fieldable result = valueCoder.encode( key, value, indexed );
            if (result != null) {
                return result;
            }
        }
        throw new RuntimeException( "No LuceneValueCoder found for value: " + value );
    }
    
    
    public final <T> T decode( Fieldable field ) {
        if (field == null) {
            return null;
        }
        for (LuceneValueCoder valueCoder : valueCoders) {
            T result = (T)valueCoder.decode( field );
            if (result != null) {
                return result;
            }
        }
        throw new RuntimeException( "No LuceneValueCoder found for field: " + field );
    }
    

    public final Query searchQuery( QueryExpression exp ) {
        assert exp != null;
        
        for (LuceneValueCoder valueCoder : valueCoders) {
            Query result = valueCoder.searchQuery( exp );
            if (result != null) {
                return result;
            }
        }
        throw new RuntimeException( "No LuceneValueCoder found for query expression: " + exp );
    }
    
}
