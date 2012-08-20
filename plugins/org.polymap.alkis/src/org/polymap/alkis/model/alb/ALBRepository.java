/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.alkis.model.alb;

import java.util.List;
import java.util.Properties;

import java.io.IOException;
import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.EntityRepositoryImpl;
import org.polymap.core.model2.runtime.EntityRepositoryConfiguration;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.CacheManager;

import org.polymap.alkis.importer.fs.ImportConfigFile;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ALBRepository
        extends EntityRepositoryImpl {

    private static Log log = LogFactory.getLog( ALBRepository.class );
    
    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );
    
    private static ALBRepository            instance;
    
    /**
     * 
     */
    public static ALBRepository instance() {
        if (instance == null) {
            try {
                // find config file
                Properties config = ImportConfigFile.configuration();
                ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                
                // find service to import into
                IService service = null;

                String id = config.getProperty( ImportConfigFile.PROP_SERVICE_URL );
                URL url  = new URL( null, id, CorePlugin.RELAXED_HANDLER );
                List<IResolve> canditates = catalog.find( url, new NullProgressMonitor() );
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
                DataAccess ds = service.resolve( DataStore.class, new NullProgressMonitor() );
                if (ds == null) {
                    throw new IOException( "Kein DataStore für Service: " + service );
                }
                
                // create repo
                EntityRepositoryConfiguration repoConfig = newConfiguration()
                        .setEntities( new Class[] {
                                Flurstueck.class, 
                                Abschnitt.class, 
                                Gemarkung.class, 
                                Lagehinweis2.class,
                                Nutzungsart.class} )
                        .setStore( new FeatureStoreAdapter( ds ) );
                instance = new ALBRepository( repoConfig );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return instance;
    };

        
    // instance *******************************************
    
    public DataAccess                   ds;
    
    private FeatureStoreAdapter         store;
    
    private Cache<String,Nutzungsart>   nutzungsarten = CacheManager.instance().newCache( CacheConfig.DEFAULT.initSize( 1024 ) );
    
    private Cache<String,Gemarkung>     gemarkungen = CacheManager.instance().newCache( CacheConfig.DEFAULT.initSize( 1024 ) );
    
    private UnitOfWork                  cacheUow;
    

    public ALBRepository( EntityRepositoryConfiguration config ) throws IOException {
        super( config );

        store = (FeatureStoreAdapter)config.getStore();
        ds = store.getStore();
        cacheUow = newUnitOfWork();
    }


    public FeatureType getSchema( Class<? extends Entity> entityClass ) {
        return store.simpleFeatureType( entityClass );
    }


    public void close() {
        super.close();
        nutzungsarten.clear();
        gemarkungen.clear();
        cacheUow.close();
    }


    public Nutzungsart nutzungsart( String schluessel ) {
        assert schluessel != null;
        return nutzungsarten.get( schluessel, new CacheLoader<String,Nutzungsart,RuntimeException>() {
            
//            public Map<String, Nutzungsart> get() {
//                UnitOfWork uow = newUnitOfWork();
//                Map<String,Nutzungsart> result = new HashMap( 1024 );
//                for (Nutzungsart entity : uow.find( Nutzungsart.class )) {
//                    result.put( entity.id.get(), entity );
//                }
//                uow.close();
//                return result;
//            }

            public Nutzungsart load( String key ) throws RuntimeException {
                FeatureIterator it = null;
                try {
                    log.info( "Loading Nutzungsart: " + key );
                    FeatureStore fs = (FeatureStore)ds.getFeatureSource( new NameImpl( Nutzungsart.TABLE_NAME ) );

                    Filter filter = ff.equals( ff.property( "ALBNUART_ID" ), ff.literal( key ) );
                    FeatureCollection features = fs.getFeatures( filter );

                    it = features.features();
                    return it.hasNext() ? cacheUow.entityForState( Nutzungsart.class, it.next() ) : null;
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
                finally {
                    if (it != null) { it.close(); }
                }
            }

            public int size() throws RuntimeException {
                return 1024;
            }
        });
    }


    public Gemarkung gemarkung( String nummer ) {
        assert nummer != null;
        return gemarkungen.get( nummer, new CacheLoader<String,Gemarkung,RuntimeException>() {
            
            public Gemarkung load( String key ) throws RuntimeException {
                FeatureIterator it = null;
                try {
                    log.info( "Loading Gemarkung: " + key );
                    FeatureStore fs = (FeatureStore)ds.getFeatureSource( new NameImpl( Gemarkung.TABLE_NAME ) );

                    Filter filter = ff.equals( ff.property( "ALBGEM_NR" ), ff.literal( key ) );
                    FeatureCollection features = fs.getFeatures( filter );

                    it = features.features();
                    return it.hasNext() ? cacheUow.entityForState( Gemarkung.class, it.next() ) : null;
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
                finally {
                    if (it != null) { it.close(); }
                }
            }

            public int size() throws RuntimeException {
                return 1024;
            }
        });
    }

}
