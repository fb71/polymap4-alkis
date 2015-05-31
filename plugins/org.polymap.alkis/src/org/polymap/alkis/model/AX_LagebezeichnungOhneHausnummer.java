/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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

import org.polymap.model2.Mixins;
import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

/**
 * [E] 'Lagebezeichnung mit Hausnummer' ist die orts�bliche oder amtlich festgesetzte
 * Benennung der Lage von Flurst�cken und Geb�uden, die eine Lagebezeichnung mit
 * Hausnummer haben. Hinweis zur Ableitung einer punktf�rmigen Geometrie zur
 * Verortung der Hausnummer: Bei einer abweichenden Positionierung von der
 * Standardposition liegt ein Pr�sentationsobjekt (Text) vor aus dem diese abgeleitet
 * werden kann.
 * <p/>
 * <b>Konsistenzbedingungen:</b> Die Relation zum Objekt 'AX_Georeferenzierte
 * Geb�udeadresse' muss nur dann gebildet werden, wenn die Relation zu einem Objekt
 * 'AX_Geb�ude' existiert und wenn 'AX_GeoreferenzierteGebaeude' dauerhaft im
 * ALKIS-Bestand gef�hrt wird. Bei �nderungen des Objekts
 * 'AX_LagebezeichnungMitHausnummer' muss stets auch das Objekt 'AX_Georeferenzierte
 * Geb�udeadresse' entsprechend fortgef�hrt werden.
 * 
 * @Abgeleitet AA_NREO
 * @Objekttyp NREO
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Mixins({AA_Lebenszeitintervall.class})
@NameInStore("ax_lagebezeichnungmithausnummer")
public class AX_LagebezeichnungOhneHausnummer
        extends AX_Lage {

    /**
     * 'Hausnummer' ist die von der Gemeinde f�r ein bestehendes oder geplantes
     * Geb�ude vergebene Nummer und ggf. einem Adressierungszusatz. Diese Attri-
     * butart wird in Verbindung mit dem Stra�ennamen (verschl�sselte oder unver-
     * schl�sselte Lagebezeichnung) vergeben.
     */
    public Property<String>         hausnummer;

}
