/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.alkis.importer.nas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.InputStream;
import java.io.Serializable;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.UploadItem;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.feature.recordstore.RDataStore;
import org.polymap.core.data.feature.recordstore.catalog.RDataStoreFactory;
import org.polymap.core.data.feature.recordstore.catalog.RServiceExtension;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NasImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NasImportOperation.class );
    
    private List<UploadItem>        items;


    protected NasImportOperation( List<UploadItem> items ) {
        super( "NAS/GML-Daten importieren" );
        this.items = items;
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        monitor.beginTask( getLabel(), IProgressMonitor.UNKNOWN );
        InputStream in = null;
        Transaction tx = new DefaultTransaction( getLabel() );
        try {
            //SubMonitor sub = new SubMonitor( monitor, 10 );
            UploadItem xsd = null;
            UploadItem gml = null;
            for (UploadItem item : items) {
                log.info( "item: " + item );
                if (item.getFileName().endsWith( "xsd" )) {
                    xsd = item;
                }
                else if (item.getFileName().endsWith( "gml" )) {
                    gml = item;
                }
            }
            // create the GML parser 
            // http://docs.codehaus.org/pages/viewpage.action?title=GML+XML+Support&spaceKey=GEOTDOC
            String namespace = "http://ogr.maptools.org/";
            String schemaLocation = getClass().getClassLoader().getResource( "postnas.xsd" ).toString();
             
            Configuration configuration = new ApplicationSchemaConfiguration( namespace, schemaLocation );
            //configuration.getProperties().add( Parser.Properties.IGNORE_SCHEMA_LOCATION );
            //configuration.getProperties().add( Parser.Properties.PARSE_UNKNOWN_ELEMENTS);
            
            RDataStore store = createStore( "Alkis" );
            Map<String,FeatureStore> schemas = new HashMap( 256 );
            
            in = gml.getFileInputStream();
            StreamingParser parser = new StreamingParser( configuration, in, Feature.class );
            Feature feature = null;
            int count = 0;
            while ((feature = (Feature)parser.parse()) != null) {
                //log.info( "    Feature: " + feature );
                
                // check/create schema
                FeatureType schema = feature.getType();
                FeatureStore fs = schemas.get( schema.getName().getLocalPart() );
                if (fs == null) {
                    store.createSchema( schema );
                    fs = (FeatureStore)store.getFeatureSource( schema.getName() );
                    fs.setTransaction( tx );
                    schemas.put( schema.getName().getLocalPart(), fs );
                    log.info( "    Schema created: " + schema.getName().getLocalPart() );
                }
                // create feature
                DefaultFeatureCollection fc = new DefaultFeatureCollection( null, (SimpleFeatureType)schema );
                fc.add( (SimpleFeature)feature );
                fs.addFeatures( fc );
                
                if (++count % 100 == 0) {
                    monitor.worked( 100 );
                    monitor.subTask( "Objekte verarbeitet: " + count );
                }
            }
            tx.commit();
        }
        catch (Exception e) {
            tx.rollback();
            throw e;
        }
        finally {
            tx.close();
            IOUtils.closeQuietly( in );
        }

        return Status.OK_STATUS;
    }

    
    protected RDataStore createStore( String dbName ) throws Exception {
        Map<String,Serializable> params = new HashMap();
        params.put( RDataStoreFactory.DBTYPE.key, (Serializable)RDataStoreFactory.DBTYPE.sample );
        params.put( RDataStoreFactory.DATABASE.key, dbName );

        RDataStoreFactory factory = RServiceExtension.factory();
        RDataStore ds = factory.createNewDataStore( params );
        try {
            List<Name> typeNames = ds.getNames();
            log.info( "RDataStore: " + typeNames );
        }
        catch( Exception e) {
            throw new RuntimeException( e );
        }

//        RServiceImpl service = (RServiceImpl)new RServiceExtension().createService( null, params );
//        CatalogPluginSession.instance().getLocalCatalog().add( service );
        return ds;
    }
    
}
