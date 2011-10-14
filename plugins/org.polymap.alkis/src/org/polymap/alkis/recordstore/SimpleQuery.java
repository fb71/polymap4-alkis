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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple query that every store implementation should handle. SimpleQuery
 * supports EQUAL and wildcard MATCH expressions. All expressions are joined
 * with logical AND. If the property value is a list then CONTAINS semantic
 * is assumed. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class SimpleQuery
        extends RecordQuery {

    private static Log log = LogFactory.getLog( SimpleQuery.class );

    public static final char            DEFAULT_ANY_WILDCARD = '*';
    
    public static final char            DEFAULT_ONE_WILDCARD = '?';
    
    private List<QueryExpression>       expressions = new ArrayList();
    
    public char                         anyWildcard = DEFAULT_ANY_WILDCARD; 
    
    public char                         oneWildcard = DEFAULT_ONE_WILDCARD;


    /**
     * Creates a new query with default wildcards and max results defaults to
     * {@link #DEFAULT_MAX_RESULTS}.
     */
    public SimpleQuery() {
    }
    
    
    /**
     * Creates a new query with the given wildcards and max results defaults to
     * {@link #DEFAULT_MAX_RESULTS}.
     */
    public SimpleQuery( char anyWildcard, char oneWildcard ) {
        this.anyWildcard = anyWildcard;
        this.oneWildcard = oneWildcard;
    }

    
    public SimpleQuery eq( String key, Object value ) {
        expressions.add( new QueryExpression.Equal( key, value ) );
        return this;
    }
    
    
    public SimpleQuery match( String key, Object value ) {
        assert value instanceof String : "Only String expressions are allowed for MATCHES predicate.";
        expressions.add( new QueryExpression.Match( key, value ) );
        return this;
    }

    
    public Collection<QueryExpression> expressions() {
        return expressions;
    }
    
}
