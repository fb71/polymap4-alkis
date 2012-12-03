/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.alkis.internal.flurliste;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.alkis.geocoder.Flurstueck;
import org.polymap.alkis.geocoder.GeocoderSPI;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurlisteGeocoder
        implements GeocoderSPI {

    private static Log log = LogFactory.getLog( FlurlisteGeocoder.class );


    public List<Flurstueck> find( List<Flurstueck> flurstuecke )
    throws Exception {
        List<ILayer> layers = findLayers();
        if (layers.isEmpty()) {
            throw new IllegalStateException( "Um nach Zähler/Nenner/Gemarkung zu Geocodieren, muss eine Ebene mit Namen 'flurliste' existieren.");
        }
        
        // create FeatureSource
        List<FeatureSource> fss = new ArrayList();
        for (ILayer layer : layers) {
            fss.add( PipelineFeatureSource.forLayer( layer, false ) );
        }
        
        // filter
        FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
        
        // find flurstuecke
        for (Flurstueck flurstueck : flurstuecke) {
            Filter filter = ff.and( Arrays.asList( new Filter[] {
                    ff.equals( ff.property( "GKZ" ), ff.literal( flurstueck.getGemarkung() ) ),
                    ff.equals( ff.property( "ZAEHLER" ), ff.literal( flurstueck.getZaehler() ) ),
                    ff.equals( ff.property( "NENNER" ), ff.literal( flurstueck.getNenner() ) ) } ) );
            
            for (FeatureSource fs : fss) {
                // XXX org.polymap.core.data.feature.lucene.LuceneFeatureCacheProcessor needs this
                FeatureType schema = fs.getSchema();
                
                // check properties
                if (schema.getDescriptor( "GKZ" ) == null) {
                    throw new IllegalStateException( "Flurliste enthält kein Feld 'gemarkung'. Ebene: " + schema.getName() );
                }
                if (schema.getDescriptor( "ZAEHLER" ) == null) {
                    throw new IllegalStateException( "Flurliste enthält kein Feld 'zaehler'. Ebene: " + schema.getName() );
                }
                if (schema.getDescriptor( "NENNER" ) == null) {
                    throw new IllegalStateException( "Flurliste enthält kein Feld 'nenner'. Ebene: " + schema.getName() );
                }
                
                FeatureCollection fc = fs.getFeatures( filter );
                if (!fc.isEmpty()) {
                    Feature feature = (Feature)fc.iterator().next();
                    log.info( "Found: " + feature );
                    Geometry geom = (Geometry)feature.getDefaultGeometryProperty().getValue();
                    log.info( "    geometry: " + geom );
                    
                    flurstueck.setGeometry( geom );
                    log.info( "    flurstueck: " + flurstueck );
                }
            }
        }
        return flurstuecke;
    }

    
    protected List<ILayer> findLayers() {
        List<ILayer> layers = ProjectRepository.instance().visit( new LayerVisitor<List<ILayer>>() {
            public void visit( ILayer l ) {
                if (l.getLabel().equals( "flurliste" )) {
                    if (result == null) {
                        result = new ArrayList();
                    }
                    result.add( l );
                }
            }
        });

        if (layers == null) {
            throw new RuntimeException( "Es wurde keine Ebene mit Namen \"Flurliste\" gefunden. " );
        }
        return layers;
    }

}
