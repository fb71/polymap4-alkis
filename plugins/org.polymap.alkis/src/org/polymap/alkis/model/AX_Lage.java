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

import java.util.Optional;

import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * [E] 'Lage' ist eine Klasse mit Eigenschaften, die für alle Objektarten dieser
 * Objektartengruppe gelten und an diese vererbt werden. Es handelt sich um eine
 * abstrakte Objektart.
 * 
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AX_Lage
        extends AA_NREO {

    /**
     * 'Unverschlüsselte Lagebezeichnung' ist die unverschlüsselte Bezeichnung einer
     * Lage.
     * <p/>
     * Aufgelöstes Attribute der AX_Lagebezeichnung.
     */
    @Nullable
    public Property<String>         unverschluesselt;

    @Nullable
    public Property<String>         kreis;
    
    @Nullable
    public Property<String>         gemeinde;
    
    @Nullable
    public Property<String>         land;
    
    @Nullable
    public Property<String>         regierungsbezirk;
    
    @Nullable
    public Property<String>         lage;
    
    public Optional<AX_LagebezeichnungKatalog> katalogeintrag() {
        return lage.get() != null
                ? Optional.of( AlkisRepository.instance.get().lageKatalog.get().get( lage.get() ) )
                : Optional.empty();
    }
    
}
