/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.alkis.geocoder;

import java.util.List;

import org.polymap.alkis.internal.flurliste.FlurlisteGeocoder;

/**
 * The geocoding API. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class Geocoder {

    private static Geocoder     instance;
    
    
    public static Geocoder instance() {
        if (instance == null) {
            instance = new Geocoder();
        }
        return instance;
    }

    
    // instance *******************************************
    
    // XXX define extension point 
    private GeocoderSPI         provider = new FlurlisteGeocoder();
    
    
    private Geocoder() {
        super();
    }


    public List<Flurstueck> find( List<Flurstueck> flurstuecke )
    throws Exception {
        return provider.find( flurstuecke );
    }

}
