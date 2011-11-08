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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.vividsolutions.jts.geom.Coordinate;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;
import org.polymap.core.runtime.recordstore.lucene.LuceneValueCoder;
import org.polymap.core.runtime.recordstore.lucene.NumericValueCoder;


/**
 * En/Decode {@link Coordinate} values using {@link NumericField} build-in support of
 * Lucene.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class CoordinateValueCoder
        implements LuceneValueCoder {

    private NumericValueCoder       numeric = new NumericValueCoder();
    
    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Coordinate) {
            Coordinate coord = (Coordinate)value;
            numeric.encode( doc, key + "_x", coord.x, indexed );
            numeric.encode( doc, key + "_y", coord.y, indexed );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        if (doc.getFieldable( key + "_x" ) != null) {
            Double x = (Double)numeric.decode( doc, key + "_x" );
            Double y = (Double)numeric.decode( doc, key + "_y" );
            return new Coordinate( x, y );
        }
        else {
            return null;
        }
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            
            if (equalExp.value instanceof Coordinate) {
                String key = equalExp.key;
                Coordinate coord = (Coordinate)equalExp.value;
                
                BooleanQuery result = new BooleanQuery();
                
                QueryExpression.Equal xQuery = new Equal( key + "_x", coord.x );
                result.add( numeric.searchQuery( xQuery ), BooleanClause.Occur.MUST );
                
                QueryExpression.Equal yQuery = new Equal( key + "_y", coord.y );
                result.add( numeric.searchQuery( xQuery ), BooleanClause.Occur.MUST );
                
                return result;
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
//            Match matchExp = (Match)exp;
//
//            if (matchExp.value instanceof Number) {
//                throw new UnsupportedOperationException( "MATCHES not supported for Number values." );
//            }
        }
        return null;
    }
    
}
