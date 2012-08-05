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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.apache.commons.lang.text.StrTokenizer;

/**
 * 
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 *         <li>16.09.2008: created</li>
 * @version $Revision: 1.1.2.1 $
 */
public class GmkImporter
        extends Job {

    static final org.apache.commons.logging.Log log = 
            org.apache.commons.logging.LogFactory.getLog( Alkis1Importer.class );

    private FsAlbProvider           provider;
    
    private TDMetaData              metaData, flurstueckMetaData;
    

    public GmkImporter( FsAlbProvider provider, InputStream in )
            throws Exception {
        super( "GmkImporter" );
        this.provider = provider;

        // database schema
        if (!provider.isActive()) {
            provider.login();
        }
        this.metaData = provider.metaDataOf( "polymap2.fs_alb.hibernate.Gemarkung" );
        this.flurstueckMetaData = provider.metaDataOf( "polymap2.fs_alb.hibernate.Flurstueck" );

        // import data
        log.info( "Importing into: " + provider.getProviderId() );
        parseFile( new BufferedInputStream( in ) );
    }


    /**
     * Parse the next entry from the given zip stream and apply the given
     * parser.
     */
    protected Exception[] parseFile( InputStream in0 )
            throws IOException {
        log.info( "************" );
        TDConversation conversation = provider.newConversation();

        List exceptions = new ArrayList();
        LineNumberReader in = new LineNumberReader( new InputStreamReader( in0, "ISO-8859-1" ) );
        int count = 0;
        for (String line=in.readLine(); line!=null; line=in.readLine()) {
            try {
                log.debug( ":: " + line );
                StrTokenizer tkn = new StrTokenizer( line, ";" );
                String gemeindeNr = tkn.nextToken();
                String gemeinde = tkn.nextToken();
                String gemarkungNr = tkn.nextToken();
                String gemarkung = tkn.nextToken();
                log.debug( "--Gemarkung: " + gemarkungNr + ":" + gemarkung );

                Object entity = conversation.createEntity( metaData );
                String id = String.valueOf( count++ );
                metaData.getIdField().set( entity, id );
                metaData.getField( "gemeinde" ).set( entity, gemeinde );
                metaData.getField( "gemarkung" ).set( entity, gemarkung );
                metaData.getField( "nr" ).set( entity, gemarkungNr );
                
                // check flurstueck
                Criteria criteria = ((HibernateConversation)conversation).createCriteria( flurstueckMetaData );
                criteria.add( Restrictions.eq( "gemarkungNr", gemarkungNr ) );
                criteria.setProjection( Projections.rowCount() );
                int rowCount = (Integer)criteria.list().get( 0 );
                log.info( "--Gemarkung: " + gemarkungNr + ":" + gemarkung + " -- rowCount: " + rowCount + (rowCount == 0 ? " skipping." : "adding...") );
                if (rowCount > 0) {
                    conversation.saveEntity( entity );
                }
            }
            catch (Exception e) {
                log.warn( "Fehler beim Import: " + e.toString(), e );
                throw new RuntimeException( e.toString(), e );
                //exceptions.add( e );
            }
        }
        conversation.flush();
        // conversation.close();

        return (Exception[])exceptions.toArray( new Exception[exceptions.size()] );
    }


    public int run()
            throws Exception {
        throw new RuntimeException( "not yet implemented." );
    }

}
