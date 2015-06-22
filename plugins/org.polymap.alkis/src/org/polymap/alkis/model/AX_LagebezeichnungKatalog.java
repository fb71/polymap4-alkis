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
 * 'Lagebezeichnung Katalogeintrag' enth�lt die eindeutige Verschl�sselung von
 * Lagebezeichnungen und Stra�en innerhalb einer Gemeinde mit der entsprechenden
 * Bezeichnung.
 * 
 * @Abgeleitet AA_NREO
 * @Objekttyp NREO
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Mixins({AA_Lebenszeitintervall.class})
@NameInStore("ax_lagebezeichnungkatalogeintrag")
public class AX_LagebezeichnungKatalog
        extends AX_Katalogeintrag {

      public Property<String>         lage;
    
}
