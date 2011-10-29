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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.geotools.data.FeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsImporter {

    private static Log log = LogFactory.getLog( EdbsImporter.class );

    private FeatureStore<SimpleFeatureType, SimpleFeature> polygonFs;

    private FeatureStore<SimpleFeatureType, SimpleFeature> lineFs;

    private FeatureStore<SimpleFeatureType, SimpleFeature> pointFs;
    
    private List<File>                                     files;
    
    public EdbsImporter( List<File> files,
            FeatureStore<SimpleFeatureType,SimpleFeature> polygonFs,
            FeatureStore<SimpleFeatureType,SimpleFeature> lineFs,
            FeatureStore<SimpleFeatureType,SimpleFeature> pointFs ) {
        this.files = files;
        this.polygonFs = polygonFs;
        this.lineFs = lineFs;
        this.pointFs = pointFs;
    }
    
    
    public void start() 
    throws Exception {
        JTSFeatureBuilder builder = new JTSFeatureBuilder();
        
        for (File f : files) {
            processFile( f, builder, true );
        }
        
        builder.endOfRecords();
        
        try {
            new ShapefileWriter( new File( "/tmp/alk-flaechen.shp" ) )
                    .write( builder.polygonBuilder.fc, false );
            new ShapefileWriter( new File( "/tmp/alk-linien.shp" ) )
                    .write( builder.lineBuilder.fc, false );
            new ShapefileWriter( new File( "/tmp/alk-punkte.shp" ) )
                    .write( builder.pointBuilder.fc, false );
        }
        catch (IOException e) {
            log.warn( "", e );
        }
    }
    
    
    protected void processFile( File f, IEdbsConsumer consumer, boolean createReport ) 
    throws IOException {
        
        EdbsReader reader = new EdbsReader( new LineNumberReader( new FileReader( f ) ) );
        RecordTokenizer satz = null;
        int count = 0, errorCount = 0;
        Timer timer = new Timer();

        while ((satz = reader.next()) != null) {
            try {
                count++;
                List<EdbsRecord> records = reader.parse( satz );
                if (records.isEmpty()) {
                    throw new EdbsParseException( "Unbekannter Satztyp: " + satz );
                }
                
                for (EdbsRecord record : records) {
                    consumer.consume( record );       
                }
            }
            catch (Exception e) {
                log.warn( "" + e.getMessage(), e );
                errorCount++;
            }
        }
        System.out.println( "Fehler: " + errorCount + ", Zeit: " + timer.elapsedTime() + "ms" );
    }
    
    
    /*
     * 
     */
    public static void main( String[] args )
    throws Exception {

        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.alkis.recordstore", "debug" );

        List<File> files = new ArrayList();
        files.add( new File( "/home/falko/Data/ALK_Testgemeinden/edbs.Test-IT-ALK.dbout.1.001" ) );
        files.add( new File( "/home/falko/Data/ALK_Testgemeinden/edbs.Test-IT-ALK.dbout.1.002" ) );
        files.add( new File( "/home/falko/Data/ALK_Testgemeinden/edbs.Test-IT-ALK.dbout.1.003" ) );
        files.add( new File( "/home/falko/Data/ALK_Testgemeinden/edbs.Test-IT-ALK.dbout.1.004" ) );
        //files.add( new File( "/home/falko/workspace-biotop/polymap3-alkis/plugins/org.polymap.alkis/doc/edbs.ALK_Muster_EDBS_BSPE.dbout.1.001" ) );
        
        EdbsImporter importer = new EdbsImporter( files, null, null, null );
        importer.start();
    }

}
