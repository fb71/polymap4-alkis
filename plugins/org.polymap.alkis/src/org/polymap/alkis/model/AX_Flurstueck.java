/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.Property;

/**
 * [A] 'Flurst�ck' ist ein Teil der Erdoberfl�che, der von einer im
 * Liegenschaftskataster festgelegten Grenzlinie umschlossen und mit einer Nummer
 * bezeichnet ist. Es ist die Buchungseinheit des Liegenschaftskatasters.
 * <p/>
 * <h4>Konsistenzbedingungen:</h4>
 * L�ckenlose und �berschneidungsfreie Fl�chendeckung der Objekte der Objektart
 * Flurst�ck. Die Positionen der Knoten der Kante m�ssen zugleich identisch sein mit
 * den Positionen der Endpunkte der Linie. Jede Linie ist durch genau zwei Positionen
 * bestimmt. Es mu� entweder die Relation 'zeigt_auf' oder 'weist_auf' belegt sein.
 * Jedes Flurst�ck geh�rt zu genau einer Gemarkung oder einer Flur/Gemarkungsteil.
 * 
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
//@Mixins( TA_MultiSurfaceComponent.class )
public class AX_Flurstueck
        extends AX_Flurstueck_Kerndaten {

    public Property<MultiPolygon>                   geom;
    
    public Association<AX_Buchungsstelle>           istGebucht;
    
}
