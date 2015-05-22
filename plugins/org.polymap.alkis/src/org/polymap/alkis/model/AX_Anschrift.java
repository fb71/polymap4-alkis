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
 * [E] 'Anschrift' ist die postalische Adresse, verbunden mit weiteren Adressen aus
 * dem Bereich elektronischer Kommunikationsmedien.
 * <p/>
 * <b>Bildungsregeln:</b> Die Relationsarten 'gehört_zu' und/oder 'bezieht_sich_auf'
 * sind objektbildend. Eine der beiden Relationsarten muss vorhanden sein.
 * 
 *
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore("ax_anschrift")
public class AX_Anschrift
        extends AA_NREO {

    /**
     * 'Ort (Post)' ist der postalische Ortsname.
     */
    @NameInStore("ort_post")
    public Property<String>         ort_Post;
    
    /**
     * 'Ortsteil' ist der Name eines Ortsteils nach dem amtlichen Ortsverzeichnis.
     */
    @Nullable
    public Property<String>         ortsteil;
    
    /**
     * 'Straße' ist der Straßen- oder Platzname nach dem amtlichen Straßenverzeichnis
     * bzw. wie bekannt geworden.
     */
    @Nullable
    public Property<String>         strasse;
    
    /**
     * 'Hausnummer' ist die von der Gemeinde für ein Gebäude vergebene Nummer,
     * gegebenenfalls mit einem Adressierungszusatz. Diese Attributart ist immer im
     * Zusammenhang mit der Attributart 'Straße' zu verwenden.
     */
    @Nullable
    public Property<String>         hausnummer;
    
    /**
     * 'Postleitzahl - Postzustellung' ist die Postleitzahl der Postzustellung.
     */
    @Nullable
    @NameInStore("postleitzahlpostzustellung")
    public Property<String>         postleitzahlPostzustellung;

}
