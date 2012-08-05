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
package org.polymap.alkis.importer;

import java.util.HashMap;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper that creates and writes shapefiles. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapefileWriter {

    private static Log log = LogFactory.getLog( ShapefileWriter.class );

    private File        file;
    
    
    public ShapefileWriter( File file ) {
        this.file = file;
    }


    public void write( FeatureCollection<SimpleFeatureType,SimpleFeature> src, boolean append )
    throws IOException {
        SimpleFeatureType shapeSchema = src.getSchema();
        
        ShapefileDataStoreFactory shapeFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put( "url", file.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = append
                ? (ShapefileDataStore)shapeFactory.createDataStore( params )
                : (ShapefileDataStore)shapeFactory.createNewDataStore( params );
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
    }

}
