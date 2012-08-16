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

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.UnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ALBNUART")
public class Nutzungsart
        extends Entity {

    /** Schlüssel der Nutzungsart aus dem Katalog. */
    @NameInStore("ALBNUART_ID")
    public Property<String>         id;
    
    /** Bezeichnung/Text der Nutzungsart aus dem Katalog. */
    @NameInStore("ALBNUART_NUART")
    public Property<String>         nutzung;

    
    /**
     * 
     */
    public static Nutzungsart forId( String id, UnitOfWork uow ) {
        throw new RuntimeException( "not yet implemented" );
//        try {
//            final FeatureStoreUnitOfWork suow = (FeatureStoreUnitOfWork)uow.context.getStoreUnitOfWork();
//            FeatureStore fs = suow.featureSource( Nutzungsart.class );
//            
//            Filter filter = ff.equals( ff.property( "ALBNUART_ID" ), ff.literal( nutzungsartId.get() ) );
//            FeatureCollection features = fs.getFeatures( filter );
//            
//            final List<Nutzungsart> result = new ArrayList();
//            features.accepts( new FeatureVisitor() {
//                public void visit( Feature feature ) {
//                    result.add( context.getUnitOfWork().entityForState( Nutzungsart.class, feature ) );
//                }
//            }, null );
//            if (result.size() > 1) {
//                log.warn( "Nutzungsarten: " + result.size() );
//            }
//            return result.isEmpty() ? null : result.get( 0 );
//        }
//        catch (IOException e) {
//            throw new RuntimeException( e );
//        }
    }

}
