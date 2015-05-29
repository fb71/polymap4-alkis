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

import com.google.common.base.Joiner;

import org.polymap.model2.Composite;
import org.polymap.model2.Computed;
import org.polymap.model2.ComputedProperty;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * 'AX_Flurst�cksnummer' ist ein Datentyp, der alle Eigenschaften f�r den Aufbau der
 * Attributart 'Flurst�cksnummer' enth�lt.
 * 
 * @version ALKIS-OK 6.0 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AX_Flurstuecksnummer
        extends Composite {

    public static AX_Flurstuecksnummer  TYPE;

    /**
     * Dieses Attribut enth�lt den Z�hler der Flurst�cknummer ohne f�hrende Nullen.
     * Diese sind gebenefalls bei der Erzeugung des Flurst�ckskennzeichens zu
     * erg�nzen.
     */
    @Computed(ComputedNummer.class)
    public Property<Integer>        zaehler;

    /**
     * Dieses Attribut enth�lt den Nenner der Flurst�cknummer ohne f�hrende Nullen.
     * Diese sind gebenefalls bei der Erzeugung des Flurst�ckskennzeichens zu erg�n-
     * zen.
     */
    @Nullable
    @Computed(ComputedNummer.class)
    public Property<String>         nenner;
    
    /**
     * Z�hler und Nenner kombiniert f�r die Anzeige (Zaehler/Nenner). 
     */
    public String anzeige() {
        return Joiner.on( '/' ).useForNull( "-" ).join( zaehler.get(), nenner.get() );
    }

    protected AX_Flurstueck flurstueck() {
        return context.getCompositePart( AX_Flurstueck.class );
    }
    
    /**
     * 
     */
    static class ComputedNummer
            extends ComputedProperty {

        @Override
        public Object get() {
            AX_Flurstueck flurstueck = ((AX_Flurstuecksnummer)composite).flurstueck();
            String propName = info.getName();
            if (propName.equals( AX_Flurstueck.TYPE.nenner.info().getName() )) {
                return flurstueck.nenner.get();
            }
            else if (propName.equals( AX_Flurstueck.TYPE.zaehler.info().getName() )) {
                return flurstueck.zaehler.get();
            }
            else {
                throw new RuntimeException( "Unbekannter Teil der FLurst�cksnummer: " +  propName );
            }
        }
    }

}
