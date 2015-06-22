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
 * 'Katalogeintrag' ist die abstrakte Oberklasse von Objektarten die einen
 * Katalogcharakter besit- zen. Es handelt sich um eine abstrakte Objektart.
 * 
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Mixins({AA_Lebenszeitintervall.class})
@NameInStore("ax_lagebezeichnungmithausnummer")
public class AX_Katalogeintrag
        extends AA_NREO {

    /**
     * 'Schl�ssel (gesamt)' enth�lt die geltende Abk�rzung des Katalogeintrags (bzw.
     * von dessen Bezeichnung). Er setzt sich ggf. aus mehreren Einzelteilen des
     * Schl�ssels des Katalogeintrags zusammen, die in der Attributart 'Schl�ssel'
     * und dem dazugeh�rigen Datentyp angegeben sind. Die Reihenfolge der
     * Schl�sselbestandteile ergibt sich ebenfalls aus diesem Datentyp. Im
     * 'Schl�ssel (gesamt)' werden Stellen, f�r die keine Schl�ssel vergeben sind,
     * mit Nullen gef�llt. Das Attribut ist ein abgeleitetes Attribut und kann nicht
     * gesetzt werden.
     */
    @NameInStore("schluesselgesamt")
    public Property<String>         schluesselGesamt;

    /**
     * 'Bezeichnung' enth�lt den langschriftlichen Namen des Katalogeintrags.
     */
    public Property<String>         bezeichnung;
    
}
