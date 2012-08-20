/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.alkis.model.alb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.io.IOException;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Immutable;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.Queryable;
import org.polymap.core.model2.store.feature.FeatureStoreUnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Immutable
@NameInStore(Flurstueck.TABLE_NAME)
public class Flurstueck
        extends Entity {

    private static Log log = LogFactory.getLog( Flurstueck.class );

    public static final FilterFactory   ff = ALBRepository.ff;
    
    public static final String          TABLE_NAME = "ALBFLU";

    public Property<MultiPolygon>       geom;
    
    /**
     * 
     */
    @Queryable
    @NameInStore("ALBFLU_ID")
    public Property<String>             id;

    @NameInStore("ALBFLU_SCHLUESSEL")
    public Property<String>             flur;
    
    @NameInStore("ALBFLU_RW")
    public Property<Integer>            rw;
    
    @NameInStore("ALBFLU_HW")
    public Property<Integer>            hw;
    
    /** Zähler des Flurstücks. */
    @Queryable
    @NameInStore("ALBFLU_NENNER")
    public Property<String>             nenner;
    
    @Queryable 
    @NameInStore("ALBFLU_ZAEHLER")
    public Property<String>             zaehler;
    
    @Queryable
    @NameInStore("ALBFLU_HAUSNR")
    public Property<Integer>            hnr;
        
    @Queryable
    @NameInStore("ALBFLU_STRANAME")
    public Property<String>             lagehinweis;
    
    @Queryable
    @NameInStore("ALBFLU_ZUSATZ")
    public Property<String>             hnrZusatz;
    
    @Queryable
    @NameInStore("ALBFLU_GEMANAME")
    public Property<String>             gemarkungName;
    
    @Queryable
    @NameInStore("ALBFLU_GEMARKUNG")
    public Property<String>             gemarkungNr;
    
    @NameInStore("ALBFLU_FLAECHE")
    public Property<Float>              flaeche;
    
    @Queryable
    @NameInStore("ALBFLU_STATUS")
    public Property<String>             status;
    
//    @NameInStore("ALBFLU_GISKEY")
//    public Property<String>             gisKey;


    /**
     * 
     */
    public Collection<Lagehinweis2> lagehinweise() throws IOException {
        final FeatureStoreUnitOfWork suow = (FeatureStoreUnitOfWork)context.getStoreUnitOfWork();
        FeatureStore fs = suow.featureSource( Lagehinweis2.class );
        
        Filter filter = ff.equals( ff.property( "ALBHINF_IDALBFLU" ), ff.literal( id.get() ) );
        log.debug( "Filter: " + filter );
        FeatureCollection features = fs.getFeatures( filter );
        
        final List<Lagehinweis2> result = new ArrayList();
        features.accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                result.add( context.getUnitOfWork().entityForState( Lagehinweis2.class, feature ) );
            }
        }, null );
        return result;
    }

    
    /**
     * 
     */
    public Collection<Abschnitt> abschnitte() throws IOException {
        final FeatureStoreUnitOfWork suow = (FeatureStoreUnitOfWork)context.getStoreUnitOfWork();
        FeatureStore fs = suow.featureSource( Abschnitt.class );
        
        Filter filter = ff.equals( ff.property( "ALBANUA_IDALBFLU" ), ff.literal( id.get() ) );
        log.debug( "Filter: " + filter );
        FeatureCollection features = fs.getFeatures( filter );
        
        final List<Abschnitt> result = new ArrayList();
        features.accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                result.add( context.getUnitOfWork().entityForState( Abschnitt.class, feature ) );
            }
        }, null );
        return result;
    }

    
    /**
     * 
     */
    public Gemarkung gemarkung() {
        String key = gemarkungNr.get();
        return ((ALBRepository)context.getRepository()).gemarkung( key );
    }
    
}
