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

import java.util.List;
import java.util.Map;

import java.io.IOException;

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.polymap.core.runtime.Timer;

import org.polymap.alkis.edbs.Objektdaten.LinieRecord;
import org.polymap.alkis.edbs.Objektdaten.ObjektRecord;
import org.polymap.alkis.recordstore.IRecordFieldSelector;
import org.polymap.alkis.recordstore.IRecordState;
import org.polymap.alkis.recordstore.IRecordStore;
import org.polymap.alkis.recordstore.RecordQuery;
import org.polymap.alkis.recordstore.SimpleQuery;
import org.polymap.alkis.recordstore.IRecordStore.ResultSet;
import org.polymap.alkis.recordstore.IRecordStore.Updater;
import org.polymap.alkis.recordstore.lucene.LuceneRecordStore;

/**
 * Builds {@link Feature}s from the EDBS records. Holding and searching the
 * records uses a {@link LuceneRecordStore}. 
 * <p/>
 * This is work in progress and not yet done. The use of a {@link IRecordStore}
 * would allow to process a working that is bigger than the available memory.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StoreFeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( StoreFeatureBuilder.class );

    public static final GeometryFactory gf = new GeometryFactory();

    private LuceneRecordStore           store;
    
    private Updater                     updater;
    
    private Timer                       timer = new Timer();
    
    
    public StoreFeatureBuilder() throws IOException {
        //store = new LuceneRecordStore( new File( "/tmp", "LuceneRecordStore" ), true );
        store = new LuceneRecordStore();
        
        store.getValueCoders().addValueCoder( new CoordinateValueCoder() );
        
        store.setIndexFieldSelector( new IRecordFieldSelector() {
            public boolean accept( String key ) {
                return key.equals( ObjektRecord.TYPE.typeId.name() )
                        || key.equals( ObjektRecord.TYPE.anfang.name() )
                        || key.equals( ObjektRecord.TYPE.objekttyp.name() )
                        || key.equals( ObjektRecord.TYPE.objektnummer.name() );
            }
        });
        
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
        log.info( "create: " + timer.elapsedTime() + "ms, committing..." );
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

//            query = new SimpleQuery()
//                    .eq( LinieRecord.TYPE.typeId.name(), LinieRecord.ID  )
//                    .eq( LinieRecord.TYPE.anfang.name(), objekt.anfang.get() )
//                    .setMaxResults( 10 );
//            ResultSet objektLinien = store.find( query );
            
//            if (objektLinien.count() > 0) {
//                log.info( "Linien für Objekt: " 
//                        + objekt.objektnummer.get() 
//                        + ", objTyp: " + objekt.objekttyp.get() 
//                        + ", objArt: " + objekt.objektArt.get() 
//                        + ", count: " + objektLinien.count() );
//            }
            
            throw new RuntimeException( "FIXME: new LineStringBuilder( objekt ).createLineString();" );
        }
    }
    
    
//    /*
//     * 
//     */
//    class LineStringBuilder {
//
//        private ObjektRecord        objekt;
//        
//        
//        public LineStringBuilder( ObjektRecord objekt ) {
//            this.objekt = objekt;
//        }
//
//        
//        public LineString createLineString() 
//        throws Exception {
//            List<Coordinate> coords = new ArrayList();
//            
//            Coordinate ende = objekt.anfang.get();
//            coords.add( ende );
//            
//            do {
//                SimpleQuery query = new SimpleQuery()
//                        .eq( LinieRecord.TYPE.typeId.name(), LinieRecord.ID  )
//                        .eq( LinieRecord.TYPE.anfang.name(), ende )
//                        .setMaxResults( 10 );
//                ResultSet linien = store.find( query );
//                ende = null;
//                
//                LinieRecord linie = null;
//                String objektnummer = objekt.objektnummer.get();
//                
//                for (IRecordState state : linien) {
//                    //LinieRecord linie = new LinieRecord( state );
//                    List<String> o1 = state.getList( LinieRecord.TYPE.objektnummern1.name() );
//                    List<Coordinate> enden = linie.state().getList( LinieRecord.TYPE.enden.name() );
//
//                    for (int i=0; i<o1.size(); i++) {
//                        if (o1.get( i ).equals( objektnummer ) ) {
//                            ende = enden.get( i );
//                            coords.add( ende );
//                            
//                        }
//                    }
//                    if (o1.contains( objektnummer )) {
//                        linie = new LinieRecord( state );
//                        break;
//                    }
//                    List<String> o2 = state.getList( LinieRecord.TYPE.objektnummern2.name() );
//                    if (o2.contains( objektnummer )) {
//                        linie = new LinieRecord( state );
//                        break;
//                    }
//                }
//                if (linie != null) {
//                    ende = enden.get( 0 );
//                    coords.add( ende );
//                }
//            } 
//            while (ende != null);
//            
//            if (coords.size() > 1) {
//                log.info( "LinieString: " + coords );
//            }
//            return null;
//        }
        
        
//        public String toString() {
//            StringBuilder result = new StringBuilder();
//            for (Coordinate[] segment : segments) {
//                result.append( Arrays.asList( segment ).toString() );
//                result.append( " - " );
//            }
//            return result.toString();
//        }
    
}
