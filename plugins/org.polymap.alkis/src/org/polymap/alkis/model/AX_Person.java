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

import static org.polymap.alkis.model.AA_Objekt.Beziehungsart.hat;

import java.util.Date;

import org.polymap.model2.NameInStore;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * [E] 'Person' ist eine natürliche oder juristische Person und kann z.B. in den
 * Rollen Eigentümer, Erwerber, Verwalter oder Vertreter in Katasterangelegenheiten
 * geführt werden.
 * <p/>
 * <b>Bildungsregeln:</b> Die Relationsarten 'weist_auf' und/oder 'benennt' und/oder
 * 'übt_aus' sowie die inverse Relationsart zum 'Benutzer' sind objektbildend. Eine
 * dieser Relationen muss vorhanden sein. Diese Relationen sind nicht zu verwenden,
 * wenn auf die 'Person' die rekursive Relation 'zeigtAuf' zeigt. In diesem Fall ist
 * 'zeigtAuf' objektbildend.
 *
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ax_person")
public class AX_Person
        extends AA_NREO {

//    public static enum AX_Anrede_Person {
//        Frau( 1000 ),
//        Herr( 2000 ),
//        Firma( 3000 );
//        
//        public int      wert;
//
//        private AX_Anrede_Person( int wert ) {
//            this.wert = wert;
//        }
//    }
    
    /**
     * 'Nachname oder Firma' ist bei einer natürliche Person der Nachname
     * (Familienname), bei einer juristischen Person, Handels- oder
     * Partnerschaftsgesellschaft der Name oder die Firma.
     */
    @NameInStore("nachnameoderfirma")
    public Property<String>             nachnameOderFirma;

    /**
     * 'Vorname' ist der Vorname/ sind die Vornamen einer natürlichen Person.
     */
    @Nullable
    public Property<String>             vorname;
    
    /**
     * 'Namensbestandteil' enthält z.B. Titel wie 'Baron'.
     */
    @Nullable
    public Property<String>             namensbestandteil;
    
    /**
     * 'Akademischer Grad' ist der akademische Grad der Person (z.B. Dipl.-Ing., Dr.,
     * Prof. Dr.).
     */
    @Nullable
    @NameInStore("akademischergrad")
    public Property<String>             akademischerGrad;
    
    /**
     * 'Geburtsname' ist der Geburtsname der Person.
     */
    @Nullable
    public Property<String>             geburtsname;
    
    @Nullable
    public Property<Date>               geburtsdatum;
    
//    /**
//     * 'Wohnort oder Sitz' ist der Wohnort oder der Sitz einer natürlichen oder
//     * juristischen Person (Par. 15 Grundbuchverfügung). Diese Attributart kommt
//     * nur bei Personen vor, die die Rolle 'Eigentümer' besitzen.
//     */
//    @Nullable
//    @NameInStore("wohnortodersitz")
//    public Property<String>             wohnortOderSitz;
    
    /**
     * 'Anrede' ist die Anrede der Person. Diese Attributart ist optional, da Körper-
     * schaften und juristischen Person auch ohne Anrede angeschrieben werden kön-
     * nen.
     */
    @Nullable
    @NameInStore("anrede")
    public Property<Integer>            anredeWert;
    
//    public Property<AX_Anrede_Person>   anrede;
    
    public ManyAssociation<AX_Anschrift> anschrift = new ManyAssociation( AX_Anschrift.class, hat );
}
