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

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.polymap.alkis.edbs.Objektdaten.LinieRecord;
import org.polymap.alkis.edbs.Objektdaten.ObjektRecord;

/**
 * Test: baut {@link Feature}s aus den EDBS-Records. Jedes Objekt und jede Linie
 * wird dabei zu einer Geometrie. Diese werden nicht zusammengefasst.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PlainFeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( PlainFeatureBuilder.class );

    private List<ObjektRecord>      objekte = new ArrayList( 10000 );
    
    private List<LinieRecord>       linien = new ArrayList( 10000 );

    
    public void consume( EdbsRecord record ) {
        if (record instanceof Objektdaten.ObjektRecord) {
            objekte.add( (ObjektRecord)record );
        }
        else if (record instanceof Objektdaten.LinieRecord) {
            linien.add( (LinieRecord)record );
        }
    }


    public void endOfRecords() {
        log.info( "Objekte: " + objekte.size() + ", Linien: " + linien.size() );
    
        // shapeSchema
        SimpleFeatureType shapeSchema = schema();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( shapeSchema );
        FeatureCollection fc = FeatureCollections.newCollection();
        GeometryFactory gf = new GeometryFactory();
        
//        // objekte
//        for (ObjektRecord objekt : objekte) {
//            //
//            List<Point> endeList = record.getList( Objektdaten.Property.ENDE );
//            List<Point> punkte = record.getList( Objektdaten.Property.PUNKTE );
//            String objektnummer = record.get( Objektdaten.Property.OBJEKTNUMMER );
//            
//            if (punkte.size() > 1) {
//                log.info( "Punkte: " + punkte );
//                log.info( "Objektnummer: " + objektnummer );
//            }
//            
//            // features
//            for (Point ende : endeList) {
//                Coordinate[] coords = new Coordinate[ 1 + punkte.size() + 1 ];
//                int i = 0;
//                coords[i++] = objekt.anfang.get().getCoordinate();
//                for (Point p : punkte) {
//                    coords[i++] = p.getCoordinate();
//                }
//                coords[i++] = ende.getCoordinate();
//                
//                LineString geom = gf.createLineString( coords );
//                fb.set( "geom", geom );
//                fb.set( "objnum", objektnummer );
//                fb.set( "objart", record.get( Objektdaten.Property.OBJEKTART ) );
//                fb.set( "folie", record.get( Objektdaten.Property.FOLIE ) );
//                fc.add( fb.buildFeature( null ) );
//            }
//        }
        
        // linien
        for (LinieRecord linie : linien) {
            // feature
            Coordinate anfang = linie.anfang.get();
            List<Coordinate> endeList = linie.enden.getList();
            
//            if (!linie.getList( Objektdaten.Property.PUNKTE ).isEmpty()) {
//                throw new IllegalStateException( "Punkte in Linie" );
//            }
//            printProperties( record );
            
            for (Coordinate ende : endeList) {
                LineString geom = gf.createLineString( new Coordinate[] { anfang, ende } );
                fb.set( "geom", geom );
                fc.add( fb.buildFeature( null ) );

                //
//                List objektnummer1 = record.getList( Objektdaten.Property.LINIE_OBJEKTNUMMER1 );
//                List objektnummer2 = record.getList( Objektdaten.Property.OBJEKTNUMMER2 );
//
//                for (int i=0; i<objektnummer1.size(); i++) {
//                    String o1 = (String)objektnummer1.get( i );
//                    String o2 = (String)objektnummer2.get( i );
//
//                    if (objekte.get( o1 ) != null) {
//                        log.warn( "gefunden: " + o1 );
//                    }
//                    if (!o2.equals( o1 ) && objekte.get( o2 ) != null) {
//                        log.warn( "gefunden: " + o2 );
//                    }
//                }
            }
        }
        
        try {
            new ShapefileWriter( new File( "/tmp/edbs.shp" ) ).write( fc, false );
        }
        catch (IOException e) {
            log.warn( "", e );
        }
    }
    
    
    protected SimpleFeatureType schema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "edbs" );
        builder.add( "objnum", String.class );
        builder.add( "folie", Integer.class );
        builder.add( "objart", Integer.class );
        builder.add( "geom", LineString.class );
        return builder.buildFeatureType();
    }
    
}
