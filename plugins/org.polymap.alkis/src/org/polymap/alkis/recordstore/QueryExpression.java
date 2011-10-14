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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class QueryExpression {

    /**
     * 
     */
    public static class Comparison<T>
            extends QueryExpression {
        
        public String          key;
        
        public T               value;

        public Comparison( String key, T value ) {
            this.key = key;
            this.value = value;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Comparison &&
                    obj.getClass() != obj.getClass()) {
                Comparison rhs = (Comparison)obj;
                return key.equals( rhs.key ) && value.equals( rhs.value );
            }
            return false;
        }
        
    }
    

    /**
     * 
     */
    public static class Equal<T>
            extends Comparison<T> {

        public Equal( String key, T value ) {
            super( key, value );
        }
    }


    /**
     * 
     */
    public static class Match<T>
            extends Comparison<T> {

        public Match( String key, T value ) {
            super( key, value );
        }
    }

}
