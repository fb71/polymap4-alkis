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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import edu.emory.mathcs.backport.java.util.Arrays;

import org.polymap.alkis.edbs.Objektdaten.LinieRecord;
import org.polymap.alkis.edbs.Objektdaten.ObjektRecord;
import org.polymap.alkis.model.AX_LinienObjekt;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LineFeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( LineFeatureBuilder.class );

    public static final GeometryFactory gf = new GeometryFactory();
    
    private Map<String,AX_LinienObjekt>     objekte = new HashMap();
    
    private List<LinieRecord>               linien = new ArrayList();

    
    public void consume( EdbsRecord record ) {
        // objekt
        if (record instanceof ObjektRecord) {
            Point anfang = record.get( ObjektRecord.Property.ANFANG );
            String objektnummer = record.get( ObjektRecord.Property.OBJEKTNUMMER );
            Integer folie = record.get( ObjektRecord.Property.FOLIE );
            Character objekttyp = record.get( ObjektRecord.Property.OBJEKTTYP );
            Integer objektart = record.get( ObjektRecord.Property.OBJEKTART );

            AX_LinienObjekt objekt = new AX_LinienObjekt();
            objekt.setReferenzPunkt( anfang );
            objekt.setObjektnummer( objektnummer );
            objekt.setFolie( folie );
            
            AX_LinienObjekt old = objekte.put( objekt.getObjektnummer(), objekt );
            if (old != null) {
                throw new IllegalStateException( "Objektnummer existiert bereits: " + objekt.getObjektnummer() );
            }
        }            
        // linie
        if (record instanceof LinieRecord) {
            linien.add( (LinieRecord)record );
        }
    }


    public void endOfRecords() {
        log.info( "Objekte: " + objekte.size() + ", Linien: " + linien.size() );
    
        // Liniensegmente bauen und zuordnen
        Map<String,LineSegmentList> segmente = new HashMap();
        for (LinieRecord record : linien) {
            
            Point anfang = record.get( LinieRecord.Property.ANFANG );
            Point ende = record.get( LinieRecord.Property.ENDE );
            List<Point> punkte = record.getList( LinieRecord.Property.LINIE_LAPA );
            List<String> o1 = record.getList( LinieRecord.Property.LINIE_OBJEKTNUMMER1 );
            List<String> o2 = record.getList( LinieRecord.Property.LINIE_OBJEKTNUMMER2 );
            
            log.info( "Punkte: " + punkte );
            log.info( "Objektnummern: " + o1 );
            
            // linien-koordinaten
            Coordinate[] coords = new Coordinate[ 1 + punkte.size() + 1 ];
            int i = 0;
            coords[i++] = anfang.getCoordinate();
            for (Point p : punkte) {
                coords[i++] = p.getCoordinate();
            }
            coords[i++] = ende.getCoordinate();
            
            // objekte suchen
            for (i=0; i<o1.size(); i++) {
                String objektnummer1 = o1.get( i );
                String objektnummer2 = o2.get( i );
                
                LineSegmentList old = segmente.put( objektnummer1, new LineSegmentList( coords ) );
                if (old != null) {
                    old.add( coords );
                    segmente.put( objektnummer1, old.add( coords ) );
//                    log.info( objektnummer1 + ": Segmente: " + old );
                }
                
                if (!objektnummer2.equals( objektnummer1 )) {
                    old = segmente.put( objektnummer2, new LineSegmentList( coords ) );
                    if (old != null) {
                        old.add( coords );
                        segmente.put( objektnummer2, old.add( coords ) );
//                        log.info( objektnummer2 + ": Segmente: " + old );
                    }

                }
            }
        }
        
        //
        for (Map.Entry<String,LineSegmentList> entry : segmente.entrySet()) {
            log.info( entry.getKey() /*+ " " + entry.getValue().toString()*/ );
            entry.getValue().createLineString();
        }

//                AX_LinienObjekt objekt = objekte.get( objektnummer1 );
//            }
//            
//            LineString geom = gf.createLineString( coords );
//                fb.set( "geom", geom );
//                fc.add( fb.buildFeature( null ) );
//        }
//        
//        try {
//            new ShapefileWriter( new File( "/tmp/edbs-linien.shp" ) ).write( fc );
//        }
//        catch (IOException e) {
//            log.warn( "", e );
//        }
    }
    
    
    /*
     * 
     */
    class LineSegmentList {
        
        List<Coordinate[]>      segments = new ArrayList();
        
        
        public LineSegmentList( Coordinate[] segment ) {
            add( segment );
        }
        
        public LineSegmentList add( Coordinate[] segment ) {
            segments.add( segment );
            return this;
        }
        
        public LineString createLineString() {
            MultiMap pointsMap = new MultiHashMap();
            for (Coordinate[] segment : segments) {
                pointsMap.put( segment[0], segment );
                pointsMap.put( segment[segment.length-1], segment );
            }
            
            for (Object point : pointsMap.keySet()) {
                Collection connected = (Collection)pointsMap.get( point );
                if (connected.size() != 1) {
                    log.info( "    " + point + " => " + connected.size() );
                }
            }
            return null;
        }
        
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Coordinate[] segment : segments) {
                result.append( Arrays.asList( segment ).toString() );
                result.append( " - " );
            }
            return result.toString();
        }
    }
    
    
//    protected SimpleFeatureType schema() {
//        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//        builder.setName( "edbs-linien" );
//        builder.add( "objnum", String.class );
//        builder.add( "geom", LineString.class );
//        return builder.buildFeatureType();
//    }
//    
//    
//    protected List<SimpleFeature> featureFromObjekt( EdbsRecord record, SimpleFeatureBuilder fb ) {
//        
//        Point anfang = record.get( Objektdaten.Property.ANFANG );
//        List<Point> endeList = record.getList( Objektdaten.Property.ENDE );
//        List<Point> punkte = record.getList( Objektdaten.Property.PUNKTE );
//        String objektnummer = record.get( Objektdaten.Property.OBJEKTNUMMER );
//        
//        if (endeList.size() > 1) {
//            log.info( "Objektnummer: " + objektnummer );
//            log.info( "Punkte: " + punkte );
//        }
//        
//        List<SimpleFeature> result = new ArrayList();
//
//        for (Point ende : endeList) {
//            Coordinate[] coords = new Coordinate[ 1 + punkte.size() + 1 ];
//            int i = 0;
//            coords[i++] = anfang.getCoordinate();
//            for (Point p : punkte) {
//                coords[i++] = p.getCoordinate();
//            }
//            coords[i++] = ende.getCoordinate();
//            
//            LineString geom = gf.createLineString( coords );
//            fb.set( "geom", geom );
//            fb.set( "objnum", objektnummer );
//            
//            result.add( fb.buildFeature( null ) );
//        }
//        return result;
//    }
    
}
