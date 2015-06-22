/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.model;

import static org.polymap.model2.store.geotools.FeatureStoreUnitOfWork.ff;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opengis.filter.Filter;

import org.apache.commons.lang3.StringUtils;

import org.polymap.core.runtime.CachedLazyInit;

import org.polymap.model2.Entity;
import org.polymap.model2.store.geotools.FilterWrapper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AA_Objekt
        extends Entity {

    /**
     * Identifikator. Spalte in der Datenbank: <code>gml_id</code> (siehe {@link AlkisRepository}}.
     */
    @Override
    public String id() {
        return (String)super.id();
    }


    /**
     * Stellt eine zu-1 Assoziation über das angegebene Fremdschlüssel-Property her.
     */
    public class Association<T extends Entity>
            implements Supplier<T> {

        private Class<T>                targetClass;

        private Supplier<String>        idProp;
        
        public Association( Class<T> targetClass, Supplier<String> idProp ) {
            this.targetClass = targetClass;
            this.idProp = idProp;
        }

        @Override
        public T get() {
            return context.getUnitOfWork().entity( targetClass, idProp.get() );
        }
    }
    
    
    /**
     * Stellt eine 0..1 Assoziation über das angegebene Fremdschlüssel-Property her.
     */
    public class OptionalAssociation<T extends Entity>
            implements Supplier<Optional<T>> {

        private Class<T>                targetClass;

        private Supplier<String>        idProp;
        
        public OptionalAssociation( Class<T> targetClass, Supplier<String> idProp ) {
            this.targetClass = targetClass;
            this.idProp = idProp;
        }

        @Override
        public Optional<T> get() {
            String id = idProp.get();
            return id != null 
                    ? Optional.ofNullable( context.getUnitOfWork().entity( targetClass, id ) )
                    : Optional.empty();
        }
    }
    
    
    /**
     * Stellt eine 0..* Assoziation über die Tabelle {@link Alkis_Beziehungen} her.
     * Das Ergebniss wird nicht zwischengespeichert. Möglicher Cache:
     * {@link CachedLazyInit}.
     */
    public class ManyAssociation<T extends Entity>
            implements Supplier<List<T>> {

        protected Class<T>          targetClass;

        protected Beziehungsart     beziehungsart;

        
        public ManyAssociation( Class<T> targetClass, Beziehungsart beziehungsart ) {
            this.targetClass = targetClass;
            this.beziehungsart = beziehungsart;
        }


        @Override
        public List<T> get() {
            String id = StringUtils.substringAfterLast( id(), "." );
            Filter filter = ff.and( 
                    ff.equals( ff.property( "beziehung_von" ), ff.literal( id ) ),
                    ff.equals( ff.property( "beziehungsart" ), ff.literal( beziehungsart.name() ) ) );

            return context.getUnitOfWork().query( Alkis_Beziehungen.class )
                    .where( new FilterWrapper( filter ) ).execute().stream()
                    .map( bz -> {
                        //System.out.println( "    #### Beziehung: " + bz );
                        return context.getUnitOfWork().entity( targetClass, bz.zu.get() );
                    })
                    .collect( Collectors.toList() );
        }
    }
    

    /**
     * Die inverse Richtung einer {@link ManyAssociation}.
     */
    public class InverseManyAssociation<T extends Entity>
            extends ManyAssociation<T> {

        public InverseManyAssociation( Class<T> targetClass, Beziehungsart beziehungsart ) {
            super( targetClass, beziehungsart );
        }

        @Override
        public List<T> get() {
            String id = StringUtils.substringAfterLast( id(), "." );
            Filter filter = ff.and( 
                    ff.equals( ff.property( "beziehung_zu" ), ff.literal( id ) ),
                    ff.equals( ff.property( "beziehungsart" ), ff.literal( beziehungsart.name() ) ) );

            return context.getUnitOfWork().query( Alkis_Beziehungen.class )
                    .where( new FilterWrapper( filter ) ).execute().stream()
                    .map( bz -> {
                        //System.out.println( "    #### Beziehung: " + bz.von.get() + " -> " + bz.zu.get() );
                        return context.getUnitOfWork().entity( targetClass, bz.von.get() );
                    })
                    .filter( entity -> entity != null && targetClass.isAssignableFrom( entity.getClass() ) )
                    .collect( Collectors.toList() );
        }
    }
    

    /**
     * Mögliche Beziehungsarten für einen {@link ManyAssociation}. 
     */
    protected enum Beziehungsart {
        weistAuf, hat, istBestandteilVon, zeigtAuf
    }

}
