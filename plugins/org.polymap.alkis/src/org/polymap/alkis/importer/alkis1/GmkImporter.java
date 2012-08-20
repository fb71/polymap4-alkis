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

import java.util.ArrayList;
import java.util.List;

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
import org.polymap.alkis.model.alb.Gemarkung;

/**
 * 
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 *         <li>16.09.2008: created</li>
 *         <li>20.08.2012: adopted to org.polymap.alkis module</li>
 */
public class GmkImporter
        extends Job {
    
    private static Log log = LogFactory.getLog( GmkImporter.class );
    
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
    public GmkImporter( InputStream in, ReportLog report, ALBRepository repo )
    throws IOException {
        super( "Gemarkungen" );
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
        log.info( "************" );
        Timer timer = new Timer();
        uow = repo.newUnitOfWork();

        List exceptions = new ArrayList();
        LineNumberReader reader = new LineNumberReader( new InputStreamReader( in0, "ISO-8859-1" ) );
        int count = 0;
        for (String line=reader.readLine(); line!=null; line=reader.readLine()) {
            try {
                log.debug( ":: " + line );
                StrTokenizer tkn = new StrTokenizer( line, ";" );
                String gemeindeNr = tkn.nextToken();
                String gemeinde = tkn.nextToken();
                String gemarkungNr = tkn.nextToken();
                String gemarkung = tkn.nextToken();
                log.debug( "--Gemarkung: " + gemarkungNr + ":" + gemarkung );

                Gemarkung entity = uow.createEntity( Gemarkung.class, null, null );
                String id = String.valueOf( count++ );
                entity.gemeinde.set( gemeinde );
                entity.gemarkung.set( gemarkung );
                entity.nummer.set( gemarkungNr );
                
//                // check flurstueck
//                Criteria criteria = ((HibernateConversation)conversation).createCriteria( flurstueckMetaData );
//                criteria.add( Restrictions.eq( "gemarkungNr", gemarkungNr ) );
//                criteria.setProjection( Projections.rowCount() );
//                int rowCount = (Integer)criteria.list().get( 0 );
//                log.info( "--Gemarkung: " + gemarkungNr + ":" + gemarkung + " -- rowCount: " + rowCount + (rowCount == 0 ? " skipping." : "adding...") );
//                if (rowCount > 0) {
//                    conversation.saveEntity( entity );
//                }
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
