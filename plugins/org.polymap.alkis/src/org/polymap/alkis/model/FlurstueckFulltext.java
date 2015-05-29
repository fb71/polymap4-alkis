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

import org.json.JSONObject;

import com.google.common.base.Joiner;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class FlurstueckFulltext {

    public static final String    ZAEHLER = "zaehler";
    public static final String    NENNER = "nenner";
    public static final String    FLUR = "flurnummer";
    public static final String    LAGE = "lagebezeichnung";
    public static final String    BESITZER_NAME = "name";
    public static final String    BESITZER_VORNAME = "vorname";
    public static final String    BESITZER_ANSCHRIFT = "anschrift";
    
    private JSONObject      result = new JSONObject();
        
    private AX_Flurstueck   fst;
    
    
    public FlurstueckFulltext( AX_Flurstueck fst ) {
        this.fst = fst;
    }


    public JSONObject transform() {
        add( ZAEHLER, fst.zaehler.get() );
        add( NENNER, fst.nenner.get() );
        add( FLUR, fst.flurnummer.get() );
        fst.lagebezeichnung.get().stream().forEach( lbz -> {
            add( LAGE, lbz.unverschluesselt.get() );
            add( LAGE, lbz.hausnummer.get() );
        });
        AX_Buchungsstelle bst = fst.buchungsstelle.get();
        AX_Buchungsblatt bb = bst.buchungsblatt.get();
//        for (Object nn : bb.namensnummern.get()) {
//            System.out.println( nn );
//        }
        bb.namensnummern.get().stream().forEach( nn -> {
            nn.person.get().ifPresent( p -> {
                add( BESITZER_NAME, p.nachnameOderFirma.get() );
                add( BESITZER_NAME, p.geburtsname.get() );
                add( BESITZER_VORNAME, p.vorname.get() );
                
                p.anschrift.get().stream().forEach( a -> {
                    add( BESITZER_ANSCHRIFT, a.ort_Post.get() );
                    add( BESITZER_ANSCHRIFT, a.ortsteil.get() );
                    add( BESITZER_ANSCHRIFT, a.hausnummer.get() );
                    add( BESITZER_ANSCHRIFT, a.postleitzahlPostzustellung.get() );
                    add( BESITZER_ANSCHRIFT, a.strasse.get() );
                });
            });
        });
        
        return result;
    }
    
    
    protected void add( String key, Object value ) {
        String stringValue = null;
        if (value == null) {
            return;
        }
        else {
            stringValue = value.toString();
        }
        result.put( key, Joiner.on( " " ).skipNulls().join( result.optString( key ), stringValue ) );
    }
    
}