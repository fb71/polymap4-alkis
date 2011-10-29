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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple query that every store implementation should handle. SimpleQuery supports
 * EQUAL and wildcard MATCH expressions. All expressions are joined with logical AND.
 * If the property value is a list then CONTAINS semantic is assumed.
 * <p/>
 * The {@link #template()} method together with RecordModel allows type-safe query by
 * example.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public final class SimpleQuery
        extends RecordQuery
        implements IRecordState {

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


    /*
     * Changing return type.
     */
    public SimpleQuery setFirstResult( int firstResult ) {
        return (SimpleQuery)super.setFirstResult( firstResult );
    }

    /*
     * Changing return type.
     */
    public SimpleQuery setMaxResults( int maxResults ) {
        return (SimpleQuery)super.setMaxResults( maxResults );
    }

    
    // template *******************************************
    
    /**
     * Creates a new query template. This can be use together with RecordModel for
     * type-safe query by example queries.
     */
    IRecordState template() {
        return this;
    }
    

//    /**
//     * 
//     */
//    class Template
//            implements IRecordState {

        public <T> SimpleQuery put( String key, T value ) {
            SimpleQuery.this.eq( key, value );
            return this;
        }

        public SimpleQuery add( String key, Object value ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public <T> T get( String key ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public <T> List<T> getList( String key ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public Object id() {
            throw new UnsupportedOperationException( "Method no supported for query template.");
        }

        public SimpleQuery remove( String key ) {
            throw new UnsupportedOperationException( "Method no supported for query template.");
        }

        public Iterator<Entry<String, Object>> iterator() {
            throw new UnsupportedOperationException( "Method no supported for query template.");
        }
        
//    }
    
}
