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

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.Property;

/**
 * [A] 'Flurstück' ist ein Teil der Erdoberfläche, der von einer im
 * Liegenschaftskataster festgelegten Grenzlinie umschlossen und mit einer Nummer
 * bezeichnet ist. Es ist die Buchungseinheit des Liegenschaftskatasters.
 * <p/>
 * <h4>Konsistenzbedingungen:</h4>
 * Lückenlose und überschneidungsfreie Flächendeckung der Objekte der Objektart
 * Flurstück. Die Positionen der Knoten der Kante müssen zugleich identisch sein mit
 * den Positionen der Endpunkte der Linie. Jede Linie ist durch genau zwei Positionen
 * bestimmt. Es muß entweder die Relation 'zeigt_auf' oder 'weist_auf' belegt sein.
 * Jedes Flurstück gehört zu genau einer Gemarkung oder einer Flur/Gemarkungsteil.
 * 
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
//@Mixins( TA_MultiSurfaceComponent.class )
public class AX_Flurstueck
        extends AX_Flurstueck_Kerndaten {

    public Property<MultiPolygon>                   geom;
    
    public Association<AX_Buchungsstelle>           istGebucht;
    
}
