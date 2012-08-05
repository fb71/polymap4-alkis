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
package org.polymap.alkis.model;

import javax.annotation.Nullable;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Property;

/**
 * 'AX_Flurstücksnummer' ist ein Datentyp, der alle Eigenschaften für den Aufbau der
 * Attributart 'Flurstücksnummer' enthält.
 * 
 * @version ALKIS-OK 6.0 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AX_Flurstuecksnummer
        extends Composite {

    /**
     * Dieses Attribut enthält den Zähler der Flurstücknummer ohne führende Nullen.
     * Diese sind gebenefalls bei der Erzeugung des Flurstückskennzeichens zu
     * ergänzen.
     */
    public Property<String>        zaehler;

    /**
     * Dieses Attribut enthält den Nenner der Flurstücknummer ohne führende Nullen.
     * Diese sind gebenefalls bei der Erzeugung des Flurstückskennzeichens zu ergän-
     * zen.
     */
    @Nullable
    public Property<String>        nenner;
}
