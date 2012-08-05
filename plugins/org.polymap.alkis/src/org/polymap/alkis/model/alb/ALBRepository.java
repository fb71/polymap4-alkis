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
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.model2.runtime.EntityRepositoryConfiguration;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;
import org.polymap.alkis.importer.fs.ImportConfigFile;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ALBRepository
        extends EntityRepository {

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
                                Lagehinweis2.class} )
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
    
    private FeatureStoreAdapter  store;
    
    public DataAccess            ds;
    

    public ALBRepository( EntityRepositoryConfiguration config ) throws IOException {
        super( config );

        store = (FeatureStoreAdapter)config.getStore();
        ds = store.getStore();
    }

}
