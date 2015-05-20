/* 
 * polymap.org
 * Copyright (C) 2012-2015, Falko Br�utigam. All rights reserved.
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

import org.polymap.rhei.fulltext.model2.EntityFeatureTransformer;

import org.polymap.model2.Association;
import org.polymap.model2.Mixins;
import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

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
@Mixins({AA_Lebenszeitintervall.class})
@NameInStore("ax_flurstueck")
public class AX_Flurstueck
        extends AX_Flurstueck_Kerndaten {

    public static AX_Flurstueck                     TYPE;
    
    public static final EntityFeatureTransformer    FulltextTransformer = new EntityFeatureTransformer() {
        @Override
        protected void visitAssociation( Association prop ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    };
    

    @NameInStore("wkb_geometry")
    public Property<MultiPolygon>                   geom;
    
    /**
     * ist gebucht (11001-21008) - Grunddatenbestand
     * <p/>
     * Ein (oder mehrere) Flurst�ck(e) ist (sind) unter genau einer Buchungsstelle
     * gebucht. Bei Anteilsbuchungen ist dies nur dann m�glich, wenn ein fiktives
     * Buchungsblatt angelegt wird.
     * <p/>
     * Kardinalit�t: 1
     */
    @NameInStore("istgebucht")
    protected Property<String>                      istGebucht;

    /**
     * Die {@link #istGebucht} Assoziation.
     */
    public AX_Buchungsstelle buchungsstelle() {
        return context.getUnitOfWork().entity( AX_Buchungsstelle.class, istGebucht.get() );
    }

//    /**
//     * Die {@link #istGebucht} Assoziation.
//     */
//    public Association<AX_Buchungsstelle>           bst = new ComputedAssociation( AX_Buchungsstelle.class, context, istGebucht );
    
    
    /**
     * weist auf (11001-12002) - Grunddatenbestand
     * <p/>
     * 'Flurst�ck' weist auf 'Lagebezeichnung mit Hausnummer'.
     * <p/>
     * Kardinalit�t: 0..*
     */
    @NameInStore("weistauf")
    public Property<String>                         weistAuf;
    
    /**
     * Die {@link #weistAuf} Assoziation.
     */
    public void lagebezeichnung() {
    }
    
}
