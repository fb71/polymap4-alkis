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

import static org.polymap.alkis.model.AA_Objekt.Beziehungsart.istBestandteilVon;

import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

/**
 * [E] 'Buchungsblatt' enthält die Buchungen (Buchungsstellen und Namensnummern) des
 * Grundbuchs und des Liegenschhaftskatasters (bei buchungsfreien Grundstücken).
 * Das Buchungsblatt für Buchungen im Liegenschaftskataster kann entweder ein
 * Kataster-, Erwerber-, Pseudo- oder ein Fiktives Blatt sein.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ax_buchungsblatt")
public class AX_Buchungsblatt
        extends AA_NREO {

    /**
     * 'Buchungsblattkennzeichen' ist ein eindeutiges Fachkennzeichen für ein Bu-
     * chungsblatt. Aufbau Buchungsblattkennzeichen: 1.) Land (Verschlüsselung
     * zweistellig), 2 Ziffern 2.) Buchungsblattbezirk (Verschlüsselung vierstellig),
     * 4 Ziffern 3.) Buchungsblattnummer mit Buchstabenerweiterung (7 Stellen) Die
     * Elemente sind rechtsbündig zu belegen, fehlende Stellen sind mit führenden
     * Nullen zu belegen. Die Gesamtlänge des Buchungsblattkennzeichens beträgt immer
     * 13 Zeichen Das Attribut ist ein abgeleitetes Attribut und kann nicht gesetzt
     * werden.
     */
    @NameInStore("buchungsblattkennzeichen")
    public Property<String>                         kennzeichen;

    @NameInStore("bezirk")
    public Property<String>                         bezirk;

    public InverseManyAssociation<AX_Namensnummer>  namensnummern = new InverseManyAssociation( AX_Namensnummer.class, istBestandteilVon );
    
}
