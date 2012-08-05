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

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Property;

/**
 * Amtliche Verschlüsselung der Gemarkung.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AX_Gemarkung_Schluessel
        extends Composite {

    /**
     * 'Land' enthält den Schlüssel für das Bundesland.
     */
    public Property<String>             land;

    /**
     * 'Gemarkungsnummer' enthält die von der Katasterbehörde zur eindeutigen
     * Bezeichnung der Gemarkung vergebene Nummer innerhalb eines Bundeslandes.
     */
    public Property<String>             gemarkungsnummer;
    
}
