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
package org.polymap.alkis.importer.edbs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.importer.edbs.Objektdaten.LinieRecord;
import org.polymap.alkis.importer.edbs.Objektdaten.ObjektRecord;

/**
 * Builds {@link Feature} instances via the great JTS tools {@link LineMerger} and
 * {@link Polygonizer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JTSFeatureBuilder
        implements IEdbsConsumer {

    private static Log log = LogFactory.getLog( JTSFeatureBuilder.class );

    public static final GeometryFactory gf = new GeometryFactory();
    
    /** objektnummer+teilnummer -> objekt */
    private Map<String,ObjektRecord>    objekte = new HashMap( 4*4096 );
    
    private Map<String,List<LineString>> lineSegments = new HashMap( 4*4096 );

    private ReportLog                   report;
    
    PolygonBuilder                      polygonBuilder;

    LineBuilder                         lineBuilder;

    PointBuilder                        pointBuilder;

    
    public JTSFeatureBuilder( ReportLog report ) {
        this.report = report;
    }


    public void consume( EdbsRecord record ) {
        // objekt
        if (record instanceof ObjektRecord) {
            ObjektRecord objekt = (ObjektRecord)record;
            List<String> teilnummern = objekt.teilnummer.getList();
//            if (teilnummern.size() > 1) {
//                log.warn( "Objekt-Teilnummer > 1: " + teilnummern );
//            }
            objekte.put( objekt.objektnummer.get(), objekt );
        }

        // linie
        else if (record instanceof LinieRecord) {
            LinieRecord linie = (LinieRecord)record;

            List<String> o1 = linie.state().getList( LinieRecord.TYPE.objektnummern1.name() );
            List<String> o2 = linie.state().getList( LinieRecord.TYPE.objektnummern2.name() );

            for (int i=0; i<o1.size(); i++) {
                // objektnummer1
                String objektnummer = o1.get( i );
                if (!objektnummer.startsWith( " " )) {
                    addLineSegment( objektnummer, linie );
                }
                // objektnummer2
                objektnummer = o2.get( i );
                if (!objektnummer.startsWith( " " )) {
                    addLineSegment( objektnummer, linie );
                }
            }
        }
    }


    private void addLineSegment( String objektnummer, LinieRecord linie ) {
        Coordinate anfang = linie.anfang.get();
        List<Coordinate> enden = linie.state().getList( LinieRecord.TYPE.enden.name() );

        Coordinate ende = enden.get( 0 );
        if (enden.size() > 1) {
            report.warn( "Linie mit mehrere Endpunkten: " + enden.size() );
        }

        LineString segment = null;
        List<Coordinate> lapa = linie.lapa.getList();
        if (lapa.isEmpty()) {
            segment = gf.createLineString( new Coordinate[] {anfang, ende} );
        }
        else {
            List<Coordinate> points = new ArrayList( lapa.size() + 2 );
            points.add( anfang );
            points.addAll( lapa );
            points.add( ende );
            segment = gf.createLineString( points.toArray( new Coordinate[ points.size() ] ) );
        }

        List<LineString> segments = lineSegments.get( objektnummer );
        
        if (segments == null) {
            log.debug( "new segment list for: " + objektnummer ); 
            segments = new ArrayList( 16 );
            segments.add( segment );
            lineSegments.put( objektnummer, segments );
        }
        else {
            log.debug( "adding to MultiLineString: " + objektnummer ); 
            segments.add( segment );
        }
    }

    
    public void endOfRecords() {
        report.info( "Objekte: " + objekte.size() + ", Linien(haufen): " + lineSegments.size() );

        // builder
        polygonBuilder = new PolygonBuilder();
        lineBuilder = new LineBuilder();
        pointBuilder = new PointBuilder();

        for (ObjektRecord objekt : objekte.values()) {

            String objekttyp = objekt.objekttyp.get();
            if ("F".equals( objekttyp )) {
                polygonBuilder.add( objekt );
            }
            else if ("L".equals( objekttyp )) {
                lineBuilder.add( objekt );
            }
            else if ("P".equals( objekttyp )) {
                pointBuilder.add( objekt );
            }
            else {
                report.warn( "Objekttyp nicht behandelt: " + objekt.objekttyp.get() );
            }
        }
        
//        try {
//            new ShapefileWriter( new File( "/tmp/alk-flaechen.shp" ) ).write( polygonBuilder.fc, false );
//            new ShapefileWriter( new File( "/tmp/alk-linien.shp" ) ).write( lineBuilder.fc, false );
//            new ShapefileWriter( new File( "/tmp/alk-punkte.shp" ) ).write( pointBuilder.fc, false );
//        }
//        catch (IOException e) {
//            log.warn( "", e );
//        }
    }

    
    /*
     * 
     */
    class PolygonBuilder {
        
        SimpleFeatureType       schema;
        
        SimpleFeatureBuilder    fb;

        FeatureCollection       fc;
        
        
        public PolygonBuilder() {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName( "ALK_Flaechen" );
            builder.add( "objnum", String.class );
            builder.add( "folie", Integer.class );
            builder.add( "objart", Integer.class );
            builder.add( "infoart", Integer.class );
            builder.add( "info", String.class );
            builder.add( "geom", MultiPolygon.class );
            schema = builder.buildFeatureType();
            
            fb = new SimpleFeatureBuilder( schema );
            fc = FeatureCollections.newCollection();
        }


        void add( ObjektRecord objekt ) {
            List<LineString> segments = lineSegments.get( objekt.objektnummer.get() );
            if( segments == null) {
                throw new IllegalStateException( "Keine Liniensegment für Objekt: " + objekt.objektnummer.get() );
            }
            log.debug( "segments: " + segments.size() );

            Polygonizer merger = new Polygonizer();
            merger.add( segments );

            Collection polygons = merger.getPolygons();
            MultiPolygon geom = gf.createMultiPolygon( (Polygon[])polygons.toArray( new Polygon[ polygons.size() ] ) );
            
            fb.set( "geom", geom );
            fb.set( "objnum", objekt.objektnummer.get() );
            fb.set( "folie", objekt.folie.get() );
            fb.set( "objart", objekt.objektArt.get() );
            
            List<Integer> infoArten = objekt.infoArt.getList();
            List<String> info = objekt.info.getList();
            if (!info.isEmpty()) {
                fb.set( "infoart", infoArten.get( 0 ) );
                fb.set( "info", info.get( 0 ) );
            }
//            if (info.size() > 1) {
//                report.println( "WARN: Mehr als ein Eintrag für 'besondereInfos': " + info.size() );
//                report.println( ">> " + info );
//            }
            
            fc.add( fb.buildFeature( objekt.objektnummer.get() ) );
        }
        
    }

    
    /*
     * 
     */
    class LineBuilder {
        
        SimpleFeatureType       schema;
        
        SimpleFeatureBuilder    fb;

        FeatureCollection       fc;
        
        
        public LineBuilder() {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName( "ALK_Linien" );
            builder.add( "objnum", String.class );
            builder.add( "folie", Integer.class );
            builder.add( "objart", Integer.class );
            builder.add( "geom", MultiLineString.class );
            schema = builder.buildFeatureType();
            
            fb = new SimpleFeatureBuilder( schema );
            fc = FeatureCollections.newCollection();
        }


        void add( ObjektRecord objekt ) {
            List<LineString> segments = lineSegments.get( objekt.objektnummer.get() );
            if( segments == null) {
                throw new IllegalStateException( "Keine Liniensegment für Objekt: " + objekt.objektnummer.get() );
            }
            log.debug( "segments: " + segments.size() );

            LineMerger merger = new LineMerger();
            merger.add( segments );

            Collection merged = merger.getMergedLineStrings();
            MultiLineString geom = gf.createMultiLineString( (LineString[])merged.toArray( new LineString[ merged.size() ] ) );

            fb.set( "geom", geom );
            fb.set( "objnum", objekt.objektnummer.get() );
            fb.set( "folie", objekt.folie.get() );
            fb.set( "objart", objekt.objektArt.get() );
            
            fc.add( fb.buildFeature( objekt.objektnummer.get() ) );
        }
        
    }

    
    /*
     * 
     */
    class PointBuilder {
        
        SimpleFeatureType       schema;
        
        SimpleFeatureBuilder    fb;

        FeatureCollection       fc;
        
        
        public PointBuilder() {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName( "ALK_Punkte" );
            builder.add( "objnum", String.class );
            builder.add( "folie", Integer.class );
            builder.add( "objart", Integer.class );
            builder.add( "geom", Point.class );
            schema = builder.buildFeatureType();
            
            fb = new SimpleFeatureBuilder( schema );
            fc = FeatureCollections.newCollection();
        }


        void add( ObjektRecord objekt ) {
            List<Coordinate> punkte = objekt.punkte.getList();
            
            for (Coordinate punkt : punkte) {
                Point geom = gf.createPoint( punkt );
                fb.set( "geom", geom );
                fb.set( "objnum", objekt.objektnummer.get() );
                fb.set( "folie", objekt.folie.get() );
                fb.set( "objart", objekt.objektArt.get() );

                fc.add( fb.buildFeature( objekt.objektnummer.get() ) );
            }
        }
        
    }
    
}
