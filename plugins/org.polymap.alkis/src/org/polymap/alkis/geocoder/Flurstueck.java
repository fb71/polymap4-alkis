/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.alkis.geocoder;

import java.text.NumberFormat;
import java.text.ParsePosition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Flurstueck {

    private static Log log = LogFactory.getLog( Flurstueck.class );

    private static NumberFormat nf = NumberFormat.getIntegerInstance();
    
    /** Without leading "0" */
    private String          zaehler, nenner, flur, gemarkung;

    private Geometry        geom;
    
    
    
    
    public static final String normalize( String nenner ) {
        ParsePosition pp = new ParsePosition( 0 );
        int zahl = nf.parse( nenner, pp ).intValue();
        
        if (Character.isLetter( nenner.charAt( nenner.length()-1 ) )) {
            char c = nenner.charAt( nenner.length()-1 );
            zahl = zahl + (9001 + (c - 'a'));
        }
        
        return String.valueOf( zahl );
    }

    
    /**
     * 
     * @param zaehler
     * @param nenner
     * @param flur
     * @param gemarkung
     * @throws IllegalArgumentException If the given arguments are not valid.
     */
    public Flurstueck( String zaehler, String nenner, String flur, String gemarkung ) {
        super();
        try {
            this.zaehler = normalize( zaehler );
            this.nenner = normalize( nenner );
            
            this.flur = flur != null ? new Integer( flur ).toString() : null;
            this.gemarkung = new Integer( gemarkung ).toString();
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException( "Flurstück ist nicht gültig: " + e.getLocalizedMessage(), e );
        }
    }

    public String getZaehler() {
        return zaehler;
    }

    public String getNenner() {
        return nenner;
    }
    
    public String getFlur() {
        return flur;
    }
    
    public String getGemarkung() {
        return gemarkung;
    }
    
    public Geometry getGeom() {
        return geom;
    }

    public void setGeometry( Geometry geom ) {
        this.geom = geom;
    }

    public String toString() {
        return "Flurstueck [zaehler=" + zaehler + ", nenner=" + nenner + ", flur=" + flur
                + ", gemarkung=" + gemarkung + ", geom=" + geom + "]";
    }

}
