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

import java.util.Collections;

import java.io.IOException;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.FeatureStoreUnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore(Flurstueck.TABLE_NAME)
public class Flurstueck
        extends Entity {

    public static final String          TABLE_NAME = "ALBFLU";

    public Property<MultiPolygon>       geom;

    @NameInStore("ALBFLU_SCHLUESSEL")
    public Property<String>             schluessel;
    
    @NameInStore("ALBFLU_RW")
    public Property<Integer>            rw;
    
    @NameInStore("ALBFLU_HW")
    public Property<Integer>            hw;
    
    @NameInStore("ALBFLU_NENNER")
    public Property<String>             nenner;
    
    @NameInStore("ALBFLU_ZAEHLER")
    public Property<String>             zaehler;
    
    @NameInStore("ALBFLU_HAUSNR")
    public Property<Integer>            hnr;
        
    @NameInStore("ALBFLU_STRANAME")
    public Property<String>             lagehinweis;
    
    @NameInStore("ALBFLU_ZUSATZ")
    public Property<String>             hnrZusatz;
    
    @NameInStore("ALBFLU_GEMANAME")
    public Property<String>             gemarkungName;
    
    @NameInStore("ALBFLU_GEMARKUNG")
    public Property<String>             gemarkungNr;
    
    @NameInStore("ALBFLU_FLAECHE")
    public Property<Float>              flaeche;
    
    @NameInStore("ALBFLU_STATUS")
    public Property<String>             status;
    
    @NameInStore("ALBFLU_GISKEY")
    public Property<String>             gisKey;

    /**
     * 1:1 Association: {@link Gemarkung}
     */
    @NameInStore("ALBFLU_IDALBGEMA")
    public Property<String>             gemarkungId;    

    
    public Gemarkung gemarkung() throws IOException {
        FilterFactory ff = ALBRepository.ff;
        
        FeatureStoreUnitOfWork uow = (FeatureStoreUnitOfWork)context.unitOfWork();
        FeatureStore gemarkungFs = uow.featureSource( Gemarkung.class );
        
        FeatureCollection features = gemarkungFs.getFeatures( ff.id( 
                Collections.singleton( ff.featureId( gemarkungId.get() ) ) ) );
        
        FeatureIterator it = features.features();
        try {
            Feature feature = it.hasNext() ? it.next() : null;
            return uow.entityForState( Gemarkung.class, feature );
        }
        finally {
            it.close();
        }
    }
    
}
