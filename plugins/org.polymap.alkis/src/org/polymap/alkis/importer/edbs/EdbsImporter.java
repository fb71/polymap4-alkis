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
import java.util.List;
import java.util.Properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.opengis.feature.simple.SimpleFeatureType;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.core.internal.CorePlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.Timer;

import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.importer.ShapefileWriter;
import org.polymap.alkis.importer.fs.ImportConfigFile;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EdbsImporter
        extends Job
        implements Runnable {

    private static Log log = LogFactory.getLog( EdbsImporter.class );

    private Reader                  in;
    
    private ReportLog               report;

    private DataStore               ds;
    
    
    public EdbsImporter( DataStore ds, Reader in, PrintStream reportOut )
    throws IOException {
        super( "EDBS-Import" );
        setPriority( LONG );
        setSystem( true );
        
        this.ds = ds;
        this.in = in;
        this.report = new ReportLog( reportOut, System.out );
    }
    
    
    public EdbsImporter( Properties conf, Reader in, PrintStream reportOut )
    throws IOException {
        super( "EDBS-Import" );
        setPriority( LONG );
        setSystem( true );
        
        this.in = in;
        this.report = new ReportLog( reportOut, System.out );

        // find service to import into
        try {
            ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

            String id = conf.getProperty( ImportConfigFile.PROP_SERVICE_URL );
            URL url  = new URL( null, id, CorePlugin.RELAXED_HANDLER );
            List<IResolve> canditates = catalog.find( url, new NullProgressMonitor() );
            
            IService service = null;
            for (IResolve resolve : canditates) {
                if (resolve.getStatus() == IResolve.Status.BROKEN) {
                    continue;
                }
                if (resolve instanceof IService) {
                    service = (IService)resolve;
                }
            }
            if (service == null) {
                throw new IOException( "Kein Service im Katalog für URL: " + id );
            }
            // find DataStore from service
            ds = service.resolve( DataStore.class, new NullProgressMonitor() );
            if (ds == null) {
                throw new IOException( "Kein DataStore für Service: " + service );
            }
            
        }
        catch (IOException e) {
            report.error( "Fehler beim Öffnen der Import-Datenquelle.", e );
            throw e;
        }
        catch (Exception e) {
            report.error( "Fehler beim Öffnen der Import-Datenquelle.", e );
            throw new IOException( e );
        }
    }
    
    
    protected IStatus run( IProgressMonitor monitor ) {
        try {
            // process using JTSFeatureBuilder
            JTSFeatureBuilder builder = new JTSFeatureBuilder( report );
            processFile( builder, true );
            builder.endOfRecords();
        
            if (ds != null) {
                // XXX write down lines and points
                
                // create feature type
                SimpleFeatureType schema = builder.polygonBuilder.schema;
                FeatureStore fs = null;
                try {
                    fs = (FeatureStore)ds.getFeatureSource( schema.getName() );
                }
                catch (Exception e) {
                    ds.createSchema( schema );
                    fs = (FeatureStore)ds.getFeatureSource( schema.getName() );
                }

                // write down
                Transaction tx = new DefaultTransaction( "edbs-import" );
                fs.setTransaction( tx );
                try {
                    fs.addFeatures( builder.polygonBuilder.fc );
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
            // shape/test
            else {
                new ShapefileWriter( new File( "/tmp/alk-flaechen.shp" ) )
                        .write( builder.polygonBuilder.fc, false );
                new ShapefileWriter( new File( "/tmp/alk-linien.shp" ) )
                        .write( builder.lineBuilder.fc, false );
                new ShapefileWriter( new File( "/tmp/alk-punkte.shp" ) )
                        .write( builder.pointBuilder.fc, false );
            }
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            report.warn( "ABBRUCH.", e );
            //throw new RuntimeException( e );
            return Status.CANCEL_STATUS;
        }
    }


    public void run() {
        run( new NullProgressMonitor() );
    }
    
    
    protected void processFile( IEdbsConsumer consumer, boolean createReport ) 
    throws IOException {
        
        EdbsReader reader = new EdbsReader( new LineNumberReader( in ), report );
        RecordTokenizer satz = null;
        int count = 0, errorCount = 0;
        Timer timer = new Timer();

        while ((satz = reader.next()) != null) {
            try {
                count++;
                List<? extends EdbsRecord> records = reader.parse( satz );
                if (records.isEmpty()) {
                    report.warn( "Unbekannter Satztyp: " + satz );
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
        
        for (File f : files) {
            EdbsImporter importer = new EdbsImporter( (DataStore)null,
                    new FileReader( f ), System.err );
            importer.run();
        }
    }

}
