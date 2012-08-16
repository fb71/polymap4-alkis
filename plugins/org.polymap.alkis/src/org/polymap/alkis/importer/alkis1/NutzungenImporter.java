/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.alkis.importer.alkis1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.opengis.filter.FilterFactory;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.runtime.Timer;

import org.polymap.alkis.importer.ReportLog;
import org.polymap.alkis.model.alb.ALBRepository;
import org.polymap.alkis.model.alb.Nutzungsart;

/**
 * 
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 *         <li>16.09.2008: created</li>
 *         <li>16.08.2012: adopted to org.polymap.alkis module</li>
 */
public class NutzungenImporter
        extends Job {

    private static Log log = LogFactory.getLog( Alkis1Importer.class );

    public static final FilterFactory   ff = Alkis1Importer.ff;
    
    private ALBRepository           repo;
    
    private UnitOfWork              uow;

    private ReportLog               report;

    private InputStream             in;


    /**
     * 
     * @param in Stream input of the importer: CSV data
     * @param report
     * @param repo The repo to use, or null if {@link ALBRepository#instance()} is to
     *        be used.
     * @throws IOException
     */
    public NutzungenImporter( InputStream in, ReportLog report, ALBRepository repo )
    throws IOException {
        super( "Nutzungen" );
        setPriority( LONG );
        setSystem( true );

        this.report = report;
        this.in = in;
        
        try {
            this.repo = repo != null ? repo : ALBRepository.instance();
        }
        catch (Exception e) {
            report.error( "Fehler beim Öffnen der Import-Datenquelle.", e );
            throw new IOException( e );
        }
    }

    /**
     * Import data.
     */
    protected IStatus run( IProgressMonitor monitor ) {
        try {
            parseFile( in );
            return Status.OK_STATUS;
        }
        catch (IOException e) {
            report.error( "Fehler beim Verarbeiten des ZIP-Files: " + e, e );
            return Status.CANCEL_STATUS;
        }
    }

    
    public void run() {
        run( new NullProgressMonitor() );
    }

    
    /**
     * Parse the next entry from the given zip stream and apply the given
     * parser.
     */
    protected void parseFile( InputStream in0 )
    throws IOException {
        Timer timer = new Timer();
        uow = repo.newUnitOfWork();

        int count = 0;
        LineNumberReader reader = new LineNumberReader( new InputStreamReader( in0, "ISO-8859-1" ) );
        for (String line=reader.readLine(); line!=null; line=reader.readLine()) {
            try {
                log.debug( ":: " + line );
                StrTokenizer tkn = new StrTokenizer( line, ";" ).setIgnoredChar( '"' );
                String nutzung = tkn.nextToken();
                String nr = tkn.nextToken();
                log.info( "--Nutzung: " + nr + ":" + nutzung );

                Nutzungsart nutzungsart = uow.createEntity( Nutzungsart.class, null, null );
                nutzungsart.id.set( nr );
                nutzungsart.nutzung.set( nutzung );
            }
            catch (Exception e) {
                log.warn( "Fehler beim Import: " + e.toString(), e );
                report.error( "Fehler bei Zeile: " + count + " -> " + line );
            }
        }
        report.info( "Zeilen verarbeitet: " + count + ". Zeit: " + timer.elapsedTime() + "ms." );
        log.info( "    " + count + " lines parsed. (" + timer.elapsedTime() + "ms)" );
        log.info( "    committing..." );
        uow.commit();
        uow.close();
        log.info( "    done." );
    }

}

