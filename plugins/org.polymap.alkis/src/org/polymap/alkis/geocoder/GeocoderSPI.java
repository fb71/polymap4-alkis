/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and individual contributors as
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

/**
 * The geocoder SPI. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public interface GeocoderSPI {
    
    /**
     *
     * @throws Exception
     */
    public List<Flurstueck> find( List<Flurstueck> flurstuecke )
    throws Exception;
    
}
