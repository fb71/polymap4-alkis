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
import org.polymap.core.model2.Queryable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore(Gemarkung.TABLE_NAME)
public class Gemarkung
        extends Entity {
    
    public static final String      TABLE_NAME = "ALBGEMA";
    
    /**
     * Generierte ID, nicht die Gemarkungsnummer.
     */
    @Queryable
    @NameInStore("ALBGEM_ID")
    public Property<String>             id;

    @Queryable
    @NameInStore("ALBGEM_GEMARKUNG")
    public Property<String>             gemarkung;

    @Queryable
    @NameInStore("ALBGEM_GEMEINDE")
    public Property<String>             gemeinde;
    
    @Queryable
    @NameInStore("ALBGEM_NR")
    public Property<String>             nummer;

    @Queryable
    @NameInStore("ALBGEM_GERICHT")
    public Property<String>             gericht;

    @Queryable
    @NameInStore("ALBGEM_GEMTEIL")
    public Property<String>             gemeindeteil;
    
}
