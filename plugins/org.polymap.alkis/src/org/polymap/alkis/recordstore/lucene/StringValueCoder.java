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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import org.polymap.alkis.recordstore.QueryExpression;
import org.polymap.alkis.recordstore.QueryExpression.Equal;
import org.polymap.alkis.recordstore.QueryExpression.Match;

/**
 * Interprets each and every field as a String {@link Field}. This *must* be
 * last consulted by {@link ValueCoders}. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class StringValueCoder
        implements LuceneValueCoder {

    public static final char        MAGIC = 'S';
    
    
    public Fieldable encode( String key, Object value, boolean indexed ) {
        if (value instanceof String) { 
            return new Field( key, (String)value, Store.YES, indexed ? Index.NOT_ANALYZED : Index.NO );
        }
        else {
            return null;
        }
    }
    

    public Object decode( Fieldable field ) {
        return field.stringValue();
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            if (equalExp.value instanceof String) {
                return new TermQuery( new Term( equalExp.key, (String)equalExp.value) );
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
            Match matchExp = (Match)exp;
            if (matchExp.value instanceof String) {
                String value =(String)matchExp.value;
                
                // FIXME properly substitute wildcard chars
                if (value.endsWith( "*" )
                        && StringUtils.countMatches( value, "*" ) == 1
                        && StringUtils.countMatches( value, "?" ) == 0) {
                    return new PrefixQuery( new Term( matchExp.key, value.substring( 0, value.length()-1 ) ) );
                }
                else {
                    return new WildcardQuery( new Term( matchExp.key, value ) );
                }
            }
        }
        return null;
    }
    
}
