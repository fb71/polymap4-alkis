/* 
 * polymap.org
 * Copyright (C) 2012-2015, Falko Bräutigam. All rights reserved.
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

import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

/**
 * [F] "Gemarkung" ist ein Katasterbezirk, der eine zusammenhängende Gruppe von
 * Flurstücken umfasst. Er kann von Gemarkungsteilen/Fluren unterteilt werden.
 *
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ax_gemarkung")
public class AX_Gemarkung
        extends AA_NREO {

    /**
     * 'Land' enthält den Schlüssel für das Bundesland.
     */
    public Property<String>                     land;

    /**
     * 'Gemarkungsnummer' enthält die von der Katasterbehörde zur eindeutigen
     * Bezeichnung der Gemarkung vergebene Nummer innerhalb eines Bundeslandes.
     */
    public Property<String>                     gemarkungsnummer;

    /**
     * Name der Gemarkung. 
     */
    public Property<String>                     bezeichnung;
    
}
