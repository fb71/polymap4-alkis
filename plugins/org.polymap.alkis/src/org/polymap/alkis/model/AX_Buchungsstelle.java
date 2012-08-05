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

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;

/**
 * [E] 'Buchungsstelle' ist die unter einer laufenden Nummer im Verzeichnis des
 * Buchungsblattes eingetragene Buchung.
 * <p/>
 * <h4>Bildungsregeln:</h4> Die Attributarten 'Buchungsart' und 'Laufende Nummer'
 * sind objektbildend. Die Buchungsarten mit Wertearten 1101, 1102, 1401 bis 1403,
 * 2201 bis 2205 und 2401 bis 2404 können nur auf einem Fiktiven Blatt vorkommen. Die
 * Attributart 'Anteil' ist dann immer zu belegen.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AX_Buchungsstelle
        extends Entity {

//    public Property<AX_Buchungsart_Buchungsstelle>  buchungsart;

    /**
     * 'Laufende Nummer' ist die eindeutige Nummer der Buchungsstelle auf dem
     * Buchungsblatt.
     */
    public Property<String>                         laufendeNummer;

}
