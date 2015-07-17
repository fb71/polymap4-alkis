/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.model.fulltext;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.RejectedExecutionHandlers;
import org.polymap.core.runtime.RejectedExecutionHandlers.Blocking;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;
import org.polymap.rhei.fulltext.update.UpdateableFulltextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFulltextIndex.Updater;

import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AlkisRepository;
import org.polymap.model2.query.ResultSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurstueckUpdater
        extends Configurable {

    private static Log log = LogFactory.getLog( FlurstueckUpdater.class );

    private FeatureTransformer[]                transformers = { new FlurstueckTransformer() };

    private AlkisRepository                     repo;
    
    private UpdateableFulltextIndex             index;

    @DefaultInt( 0 )
    public Config<FlurstueckUpdater,Integer>    first;
    
    @DefaultInt( Integer.MAX_VALUE )
    public Config<FlurstueckUpdater,Integer>    max;

    private List<Throwable>                     exceptions = new CopyOnWriteArrayList();
    

    public FlurstueckUpdater( AlkisRepository repo, UpdateableFulltextIndex index ) {
        this.repo = repo;
        this.index = index;
    }


    protected JSONObject transform( AX_Flurstueck feature ) {
        Object transformed = feature;
        for (FeatureTransformer transformer : transformers) {
            transformed = transformer.apply( transformed );
        }
        assert ((JSONObject)transformed).opt( FulltextIndex.FIELD_ID ) != null;
        return (JSONObject)transformed;
    }


    public void run() throws Exception {
        log.info( "first: " + first.get() );
        log.info( "max: " + max.get() );
        
        int chunkSize = Math.min( 10000, max.get() );
        for (int i=0; i<max.get(); i+=chunkSize) {
            if (processChunk( first.get()+i, chunkSize ) < chunkSize) {
                break;
            }
        }
    }
    
    
    protected int processChunk( int chunkFirst, int chunkMax ) throws InterruptedException {
        Timer t = new Timer();
        
        //int numThreads = 4;  //Runtime.getRuntime().availableProcessors() * 2;
        int numThreads = Integer.parseInt( System.getProperty( "org.polymap.alkis.threads", "8" ) );
        log.info( "threads: " + numThreads );
        int sleepMillis = Integer.parseInt( System.getProperty( "org.polymap.alkis.sleepMillis", "100" ) );
        log.info( "sleepMillis: " + sleepMillis );
        Blocking rejected = new RejectedExecutionHandlers.Blocking().sleepMillis.put( sleepMillis );
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor( numThreads, numThreads,
                10L, TimeUnit.SECONDS, new ArrayBlockingQueue( numThreads ), rejected );
        
        try (
                Updater updater = index.prepareUpdate();
                ResultSet<AX_Flurstueck> rs = repo.newUnitOfWork()
                        .query( AX_Flurstueck.class ).firstResult( chunkFirst ).maxResults( chunkMax ).execute()
            ) {
            int count = 0;
            for (AX_Flurstueck fst : rs) {
                count ++;
                executor.execute( () -> {
                    try {
                        log.info( Thread.currentThread().getName() );
                        JSONObject transformed = transform( fst );
                        log.info( "transformed: " + transformed.toString( 4 ) );
                        updater.store( transformed, false );                    
                    }
                    catch (Throwable e) {
                        log.info( "", e );
                        exceptions.add( e );
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination( 1, TimeUnit.MINUTES );
            log.info( "Zeit für Transformation: " + t.elapsedTime() + "ms (" + chunkFirst + "/" + chunkMax + ")" );
            
            t.start();
            updater.apply();
            log.info( "Zeit für Index: " + t.elapsedTime() + "ms" );
            return count;
        }
    }
    
    
    /**
     * Simple access test.
     */
    public static void main( String[] args ) throws Exception {
        AlkisRepository.instance.get().updateFulltext( 100 );
    }
    
}
