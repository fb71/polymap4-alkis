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

import static org.polymap.alkis.model.AA_Objekt.Beziehungsart.istBestandteilVon;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.polymap.model2.Computed;
import org.polymap.model2.ComputedProperty;
import org.polymap.model2.NameInStore;
import org.polymap.model2.Property;

/**
 * [E] 'Buchungsblatt' enth�lt die Buchungen (Buchungsstellen und Namensnummern) des
 * Grundbuchs und des Liegenschhaftskatasters (bei buchungsfreien Grundst�cken).
 * Das Buchungsblatt f�r Buchungen im Liegenschaftskataster kann entweder ein
 * Kataster-, Erwerber-, Pseudo- oder ein Fiktives Blatt sein.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@NameInStore("ax_buchungsblatt")
public class AX_Buchungsblatt
        extends AA_NREO {

    public static Map<String,AX_Blattart_Buchungsblatt> blattarten = 
            Arrays.stream( AX_Blattart_Buchungsblatt.values() ).collect( Collectors.toMap( ba -> ba.wert, ba -> ba ) );
    
    /**
     * 
     */
    public enum AX_Blattart_Buchungsblatt {
        /**
         * Ein Grundbuchblatt ist ein Buchungsblatt, das die Buchung im Grundbuch
         * enth�lt.
         */
        Grundbuchblatt( 1000 ),
        /**
         * Ein Katasterblatt ist ein Buchungsblatt, das die Buchung im
         * Liegenschaftskataster enth�lt.
         */
        Katasterblatt( 2000 ),
        /**
         * Ein Pseudoblatt ist ein Buchungsblatt, das die Buchung, die bereits vor
         * Eintrag im Grundbuch Rechtskraft erlangt hat, enth�lt (z.B. �bernahme von
         * Flurbereinigungsverfahren, Umlegungsverfahren).
         */
        Pseudoblatt( 3000 ),
        /**
         * Ein Erwerberblatt ist ein Buchungsblatt, das die Buchung, die bereits im
         * Liegenschaftskataster, aber noch nicht im Grundbuch gebucht ist, enth�lt
         * (Buchungsvorschlag f�r die Grundbuchverwaltung).Pseudoblatt und
         * Erwerberblatt werden nach Eintragung in das Grundbuch historisch.
         */
        Erwerberblatt( 4000 ),
        /**
         * Das fiktive Blatt enth�lt die aufgeteilten Grundst�cke und Rechte als
         * Ganzes. Es bildet um die Miteigentumsanteile eine fachliche Klammer.
         */
        FiktivesBlatt( 5000 );

        private String          wert;

        private AX_Blattart_Buchungsblatt( int wert ) {
            this.wert = Integer.valueOf( wert ).toString();
        }
    }

    /**
     * 'Buchungsblattkennzeichen' ist ein eindeutiges Fachkennzeichen f�r ein Bu-
     * chungsblatt. Aufbau Buchungsblattkennzeichen: 1.) Land (Verschl�sselung
     * zweistellig), 2 Ziffern 2.) Buchungsblattbezirk (Verschl�sselung vierstellig),
     * 4 Ziffern 3.) Buchungsblattnummer mit Buchstabenerweiterung (7 Stellen) Die
     * Elemente sind rechtsb�ndig zu belegen, fehlende Stellen sind mit f�hrenden
     * Nullen zu belegen. Die Gesamtl�nge des Buchungsblattkennzeichens betr�gt immer
     * 13 Zeichen Das Attribut ist ein abgeleitetes Attribut und kann nicht gesetzt
     * werden.
     */
    @NameInStore("buchungsblattkennzeichen")
    public Property<String>                         kennzeichen;

    @NameInStore("buchungsblattnummermitbuchstabenerweiterung")
    public Property<String>                         nummer;

    /** 
     * Schl�ssel f�r {@link #bezirk()}. 
     */
    @NameInStore("bezirk")
    public Property<String>                         bezirknummer;

    public AX_Buchungsblattbezirk bezirk() {
        return AlkisRepository.instance.get().bbbezirk.get().get( bezirknummer.get() );
    }

    @NameInStore("blattart")
    public Property<String>                         blattartnummer;
    
    @Computed(ComputedBlattart.class)
    public Property<AX_Blattart_Buchungsblatt>      blattart;

    public InverseManyAssociation<AX_Namensnummer>  namensnummern = new InverseManyAssociation( AX_Namensnummer.class, istBestandteilVon );
    
    
    public static class ComputedBlattart
            extends ComputedProperty<AX_Blattart_Buchungsblatt> {
        @Override
        public AX_Blattart_Buchungsblatt get() {
            return blattarten.get( ((AX_Buchungsblatt)composite).blattartnummer.get() );
        }
    }
            
}
