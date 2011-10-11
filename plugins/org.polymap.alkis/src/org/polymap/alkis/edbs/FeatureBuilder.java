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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Baut {@link Feature} objekte aus den {@link EdbsRecord} Objekten. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( FeatureBuilder.class );

    private Map<String,EdbsRecord>      objekte = new HashMap();
    
    private List<EdbsRecord>            linien = new ArrayList();

    
    public void consume( EdbsRecord record ) {
        if (record.getId() == Objektdaten.ID) {
            String objektnummer = (String)record.get( Objektdaten.Property.OBJEKTNUMMER );
            
            // objekt
            if (objektnummer != null) {
                if (objekte.containsKey( objektnummer )) {
                    log.info( "Objektnummer gefunden: " + objektnummer );
                }
                else {
                    objekte.put( objektnummer, record );
                }                
            }
            
            // linie
            else {
                linien.add( record );
            }
        }
    }


    public void endOfRecords() {
        log.info( "Objekte: " + objekte.size() + ", Linien: " + linien.size() );
    
        // shapeSchema
        SimpleFeatureType shapeSchema = schema();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( shapeSchema );
        FeatureCollection fc = FeatureCollections.newCollection();
        GeometryFactory gf = new GeometryFactory();
        
        // objekte
        for (EdbsRecord record : objekte.values()) {
            // feature
            Point anfang = record.get( Objektdaten.Property.ANFANG );
            List<Point> endeList = record.getList( Objektdaten.Property.ENDE );
            List<Point> punkte = record.getList( Objektdaten.Property.PUNKTE );
            String objektnummer = record.get( Objektdaten.Property.OBJEKTNUMMER );
            
            if (endeList.size() > 1) {
                log.info( "Punkte: " + punkte );
                log.info( "Objektnummer: " + objektnummer );
            }
            
            for (Point ende : endeList) {
                Coordinate[] coords = new Coordinate[ 1 + punkte.size() + 1 ];
                int i = 0;
                coords[i++] = anfang.getCoordinate();
                for (Point p : punkte) {
                    coords[i++] = p.getCoordinate();
                }
                coords[i++] = ende.getCoordinate();
                
                LineString geom = gf.createLineString( coords );
                fb.set( "geom", geom );
                fc.add( fb.buildFeature( null ) );
            }
        }
        
        // linien
        for (EdbsRecord record : linien) {
            // feature
            Point anfang = record.get( Objektdaten.Property.ANFANG );
            List<Point> endeList = record.getList( Objektdaten.Property.ENDE );
            
            for (Point ende : endeList) {
                LineString geom = gf.createLineString( new Coordinate[] { anfang.getCoordinate(), ende.getCoordinate() } );
                fb.set( "geom", geom );
                fc.add( fb.buildFeature( null ) );

                //
//                List objektnummer1 = record.getList( Objektdaten.Property.OBJEKTNUMMER1 );
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
            writeShapefile( fc );
        }
        catch (IOException e) {
            log.warn( "", e );
        }
    }
    
    
    protected SimpleFeatureType schema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "edbs" );
        builder.add( "objnum", String.class );
        builder.add( "geom", LineString.class );
        return builder.buildFeatureType();
    }
    
    
    protected File writeShapefile( FeatureCollection<SimpleFeatureType,SimpleFeature> src )
    throws IOException {
        File newFile = new File( "/tmp/edbs.shp" );
        SimpleFeatureType shapeSchema = src.getSchema();
        
        ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put( "url", newFile.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = (ShapefileDataStore)shapeFactory.createNewDataStore( params );
        shapeDs.createSchema( shapeSchema );

        //shapeDs.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        //shapeDs.setStringCharset( )
        
        // write shapefile
        Transaction tx = new DefaultTransaction( "create" );

        String typeName = shapeDs.getTypeNames()[0];
        FeatureStore<SimpleFeatureType,SimpleFeature> shapeFs 
                = (FeatureStore<SimpleFeatureType, SimpleFeature>)shapeDs.getFeatureSource( typeName );

        shapeFs.setTransaction( tx );
        try {
            shapeFs.addFeatures( src );
            tx.commit();
        } 
        catch (IOException e) {
            tx.rollback();
            throw e;
        } 
        finally {
            tx.close();
        }
        
        return newFile;
    }

}
