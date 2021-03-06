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

import org.polymap.model2.Mixins;
import org.polymap.model2.NameInStore;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * [E] 'Lagebezeichnung ohne Hausnummer' ist die ortsübliche oder amtlich
 * festgesetzte Benennung der Lage von Flurstücken, die keine Hausnummer haben (z.B.
 * Namen und Bezeichnungen von Gewannen, Straßen, Gewässern).
 * <p/>
 * <b>Konsistenzbedingungen:</b> 'Verschlüsselte Lagebezeichnung' und
 * 'Unverschlüsselte Lagebezeichnung' schließen sich gegenseitig aus; eine dieser
 * Attributarten muss vorhanden sein.
 * 
 * @Abgeleitet AA_NREO
 * @Objekttyp NREO
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Mixins({AA_Lebenszeitintervall.class})
@NameInStore("ax_lagebezeichnungohnehausnummer")
public class AX_LagebezeichnungOhneHausnummer
        extends AX_Lage {

    @Nullable
    @NameInStore("zusatzzurlagebezeichnung")
    public Property<String>             zusatz;

//    @Nullable
//    public Property<String>             ortsteil;

}
