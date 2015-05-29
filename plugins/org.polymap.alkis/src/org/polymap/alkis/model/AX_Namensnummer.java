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

import org.polymap.model2.NameInStore;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * [E] 'Namensnummer' ist die laufende Nummer der Eintragung, unter welcher der
 * Eigentümer oder Erbbauberechtigte im Buchungsblatt geführt wird.
 * Rechtsgemeinschaften werden auch unter AX_Namensnummer geführt.
 * 
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ax_namensnummer")
public class AX_Namensnummer
        extends AA_NREO {

    /**
     * 'Laufende Nummer nach DIN 1421' ist die interne laufende Nummer für die
     * Rangfolge der Person, die nach den Vorgaben aus DIN 1421 strukturiert ist.
     */
    @Nullable
    @NameInStore("laufendenummernachdin1421")
    public Property<String>                 laufendeNummerNachDIN1421;
    
    /**
     * 'Nummer' ist die laufende Nummer der Eintragung gemäß Abteilung 1
     * Grundbuchblatt, unter der eine Person aufgeführt ist (z.B. 1 oder 1a).
     */
    @Nullable
    public Property<String>                 nummer;

    @NameInStore("benennt")
    protected Property<String>              benenntId;

    /**
     * Die {@link #benenntId} Assoziation.
     */
    public OptionalAssociation<AX_Person>   person = new OptionalAssociation( AX_Person.class, ()->benenntId.get() );

    @NameInStore("istbestandteilvon")
    protected Property<String>              istBestandteilVonId;
    
    /**
     * Die {@link #istBestandteilVonId} Assoziation.
     */
    public OptionalAssociation<AX_Buchungsblatt>   buchungsblatt = new OptionalAssociation( AX_Buchungsblatt.class, ()->istBestandteilVonId.get() );
    
    
//    @Nullable
//    public Property<Integer>        eigentuemerartWert;
//
//    @Computed(ComputedEigentumsart.class)
//    public Property<AX_Eigentuemerart> eigentuemerart;
//
//    
//    public static class ComputedEigentumsart
//            extends ComputedProperty<AX_Eigentuemerart> {
//
//        public ComputedEigentumsart( PropertyInfo info, Composite composite ) {
//            super( info, composite );
//        }
//
//        @Override
//        public AX_Eigentuemerart get() {
//            AX_Namensnummer nn = ((AX_Namensnummer)composite);
//        }
//
//        @Override
//        public void set( AX_Eigentuemerart value ) {
//            throw new RuntimeException( "not yet implemented." );
//        }
//    }
//    
//    
//    public static enum AX_Eigentuemerart {
//        NatuerlichePersonen( 1000, "Natürliche Personen" ),
//        NatuerlichePersonenEhe( 1000, "Natürliche Person - Alleineigentum oder Ehepartner" );
//
//        public static Map<Integer,AX_Eigentuemerart>  map = new HashMap();
//        
//        public int          wert;
//        public String       bezeichner;
//        
//        private AX_Eigentuemerart( int wert, String bezeichner ) {
//            this.wert = wert;
//            this.bezeichner = bezeichner;
//            map.put( wert, this );
//        }
//    }
}
