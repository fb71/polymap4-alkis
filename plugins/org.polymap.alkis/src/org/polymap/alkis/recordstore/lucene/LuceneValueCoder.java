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

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;

import org.polymap.alkis.recordstore.QueryExpression;

/**
 * 
 * <p/>
 * Implementations have to be thread-safe.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface LuceneValueCoder {

    public Fieldable encode( String key, Object value, boolean indexed );
   
    public Object decode( Fieldable field );
    
    public Query searchQuery( QueryExpression exp );
    
}
