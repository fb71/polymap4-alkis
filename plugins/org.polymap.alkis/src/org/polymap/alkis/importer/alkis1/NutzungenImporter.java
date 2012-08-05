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
 */
public class NutzungenImporter
        extends Job {

    static final org.apache.commons.logging.Log log = 
            org.apache.commons.logging.LogFactory.getLog( Alkis1Importer.class );

    private FsAlbProvider           provider;
    
    private TDMetaData              metaData;
    

    public NutzungenImporter( FsAlbProvider provider, InputStream in )
            throws Exception {
        super( "GmkImporter" );
        this.provider = provider;

        // database schema
        if (!provider.isActive()) {
            provider.login();
        }
        this.metaData = provider.metaDataOf( "polymap2.fs_alb.hibernate.Nutzungsart" );

        provider.createDatabaseSchema( true, true );
        
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
                String nutzung = tkn.nextToken();
                String nr = tkn.nextToken();
                log.info( "--Nutzung: " + nr + ":" + nutzung );

                Object entity = conversation.createEntity( metaData );
                metaData.getIdField().set( entity, nr );
                metaData.getField( "nutzungsart" ).set( entity, nutzung );
                
                conversation.saveEntity( entity );
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

