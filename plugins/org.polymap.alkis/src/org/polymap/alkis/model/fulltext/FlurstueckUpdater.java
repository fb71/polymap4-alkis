/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.RejectedExecutionHandlers;
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
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class FlurstueckUpdater
        extends Configurable {

    private static Log log = LogFactory.getLog( FlurstueckUpdater.class );

    private FeatureTransformer[]                transformers = { new FlurstueckTransformer() };

    private AlkisRepository                     repo;
    
    private UpdateableFulltextIndex             index;

    @DefaultInt( 0 )
    private Config<FlurstueckUpdater,Integer>   first;
    
    @DefaultInt( 1000 )
    private Config<FlurstueckUpdater,Integer>   max;


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
        log.info( "Transformed: " + ((JSONObject)transformed).toString( 4 ) );
        return (JSONObject)transformed;
    }


    public void run() throws Exception {
        log.info( "first: " + first.get() );
        log.info( "max: " + max.get() );
        Timer t = new Timer();
        
        int numThreads = 6;  //Runtime.getRuntime().availableProcessors() * 2;
        ThreadPoolExecutor executor = new ThreadPoolExecutor( numThreads, numThreads,
                10L, TimeUnit.SECONDS, new SynchronousQueue(), new RejectedExecutionHandlers.Blocking() );
        
        List<Throwable> exceptions = new CopyOnWriteArrayList();
        
        try (
                Updater updater = index.prepareUpdate();
                ResultSet<AX_Flurstueck> rs = repo.newUnitOfWork().query( AX_Flurstueck.class ).firstResult( first.get() ).maxResults( max.get() ).execute()
            ) {
            rs.forEach( fst -> {
                executor.execute( () -> {
                    try {
                        log.info( Thread.currentThread().getName() );
                        JSONObject transformed = transform( fst );
                        //log.info( "transformed: " + transformed );
                        //updater.store( transformed, false );                    
                    }
                    catch (Throwable e) {
                        log.info( "", e );
                        exceptions.add( e );
                    }
                });
            });
            executor.shutdown();
            executor.awaitTermination( 1, TimeUnit.MINUTES );
            log.info( "Zeit f�r Transformation: " + t.elapsedTime() + "ms" );
            
            t.start();
            updater.apply();
            log.info( "Zeit f�r Index: " + t.elapsedTime() + "ms" );
        }
    }
    
}