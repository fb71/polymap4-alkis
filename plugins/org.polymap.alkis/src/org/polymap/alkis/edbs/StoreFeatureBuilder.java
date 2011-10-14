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
import java.util.List;
import java.util.Map;

import java.io.IOException;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import edu.emory.mathcs.backport.java.util.Arrays;

import org.polymap.alkis.edbs.Objektdaten.LinieRecord;
import org.polymap.alkis.edbs.Objektdaten.ObjektRecord;
import org.polymap.alkis.recordstore.IRecordState;
import org.polymap.alkis.recordstore.RecordQuery;
import org.polymap.alkis.recordstore.SimpleQuery;
import org.polymap.alkis.recordstore.IRecordStore.ResultSet;
import org.polymap.alkis.recordstore.IRecordStore.Updater;
import org.polymap.alkis.recordstore.lucene.LuceneRecordStore;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StoreFeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( StoreFeatureBuilder.class );

    public static final GeometryFactory gf = new GeometryFactory();

    private LuceneRecordStore           store;
    
    private Updater                     updater;
    
    
    public StoreFeatureBuilder() throws IOException {
        //store = new LuceneRecordStore( new File( "/tmp", "LuceneRecordStore" ), true );
        store = new LuceneRecordStore();
        updater = store.prepareUpdate();
    }

    
    public void consume( EdbsRecord record ) 
    throws Exception {
        IRecordState storeRecord = store.newRecord();
        // copy state
        for (Map.Entry<String,Object> entry : record.state()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                for (Object listValue : (List)value) {
                    storeRecord.add( entry.getKey(), listValue );
                }
            }
            else {
                storeRecord.put( entry.getKey(), value );
            }
        }
        // store
        updater.store( storeRecord );
    }


    public void endOfRecords()
    throws Exception {
        log.info( "committing..." );
        updater.apply();
        
        RecordQuery query = new SimpleQuery()
                .eq( ObjektRecord.TYPE.typeId.name(), ObjektRecord.ID  )
                .setMaxResults( 1000000 );
        ResultSet objekte = store.find( query );

        query = new SimpleQuery()
                .eq( LinieRecord.TYPE.typeId.name(), LinieRecord.ID  )
                .setMaxResults( 1000000 );
        ResultSet linien = store.find( query );

        log.info( "Objekte: " + objekte.count() + ", Linien: " + linien.count() );
        
        // find linien
        for (IRecordState recordState : objekte) {
            ObjektRecord objekt = new Objektdaten.ObjektRecord( recordState );

            query = new SimpleQuery()
                    .eq( LinieRecord.TYPE.typeId.name(), LinieRecord.ID  )
                    .eq( LinieRecord.TYPE.objektnummern1.name(), objekt.objektnummer.get() )
                    .setMaxResults( 100 );
            ResultSet objektLinien = store.find( query );
            
            if (objektLinien.count() > 0) {
                log.info( "Linien für Objekt: " + objekt.objektnummer.get() + ": " + objektLinien.count() );
            }
        }
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
