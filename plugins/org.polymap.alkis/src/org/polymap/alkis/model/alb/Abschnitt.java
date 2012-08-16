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
@NameInStore( "ALBANUA" )
public class Abschnitt
        extends Entity {

    private static Log log = LogFactory.getLog( Abschnitt.class );

    public static final FilterFactory   ff = ALBRepository.ff;

    @NameInStore("ALBANUA_FLAECHE")
    public Property<Float>              flaeche;

//    @Queryable
//    @NameInStore("ALBANUA_NUTZUNG")
//    public Property<String>             nutzung;

    /**
     * n:1 Association: {@link Flurstueck}
     */
    @Queryable
    @NameInStore("ALBANUA_IDALBFLU")
    public Property<String>             flurstueckId;    

    /**
     * n:1 Association: {@link Nutzungsart}
     */
    @Queryable
    @NameInStore("ALBANUA_IDALBNUART")
    public Property<String>             nutzungsartId;    

    
    /**
     * 
     */
    public Nutzungsart nutzungsart() {
        try {
            final FeatureStoreUnitOfWork suow = (FeatureStoreUnitOfWork)context.getStoreUnitOfWork();
            FeatureStore fs = suow.featureSource( Nutzungsart.class );
            
            Filter filter = ff.equals( ff.property( "ALBNUART_ID" ), ff.literal( nutzungsartId.get() ) );
            FeatureCollection features = fs.getFeatures( filter );
            
            final List<Nutzungsart> result = new ArrayList();
            features.accepts( new FeatureVisitor() {
                public void visit( Feature feature ) {
                    result.add( context.getUnitOfWork().entityForState( Nutzungsart.class, feature ) );
                }
            }, null );
            if (result.size() > 1) {
                log.warn( "Nutzungsarten: " + result.size() );
            }
            return result.isEmpty() ? null : result.get( 0 );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
}
