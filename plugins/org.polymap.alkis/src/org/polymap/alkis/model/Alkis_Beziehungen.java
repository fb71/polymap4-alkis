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
package org.polymap.alkis.model;

import org.polymap.model2.Entity;
import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

/**
 * Assoziationen zwischen den Entities. Wird vom OGR/PostNAS-Import angelegt.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("alkis_beziehungen")
public class Alkis_Beziehungen
        extends Entity {

    public static Alkis_Beziehungen TYPE;
    
    @NameInStore("beziehung_von")
    public Property<String>     von;
    
    @NameInStore("beziehung_zu")
    public Property<String>     zu;
    
    @NameInStore("beziehungsart")
    public Property<String>     art;
    
}
