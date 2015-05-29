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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.NameInStore;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * 'Flurstück_Kerndaten' enthält Eigenschaften des Flurstücks, die auch für andere
 * Flurstücksobjektarten gelten (z.B. Historisches Flurstück). Es handelt sich um
 * eine abstrakte Objektart.
 * 
 * @Kennung 11004
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AX_Flurstueck_Kerndaten
        extends AA_NREO {

    private static Log log = LogFactory.getLog( AX_Flurstueck_Kerndaten.class );

//    /**
//     * 'Gemarkung' enthält die Eigenschaften aus dem 'AX_Gemarkung_Schlüssel': 'land'
//     * und 'gemarkungsnummer'.
//     */
//    public Property<AX_Gemarkung_Schluessel>   gemarkung;
    
//    /**
//     * 'Flurstücksnummer' ist die Bezeichnung (Zähler/Nenner), mit der ein Flurstück
//     * innerhalb einer Flur (Flurnummer muss im Land vorhanden sein) oder Gemarkung
//     * identifiziert werden kann. Das Attribut setzt sich zusammen aus: 1. Spalte:
//     * Zähler 2. Spalte: Nenner Die 2. Spalte ist optional.
//     */
//    public Property<AX_Flurstuecksnummer>      flurstuecksnummer;

    protected Property<Integer>                zaehler;
    
    @Nullable
    protected Property<String>                 nenner;
    
    /**
     * 'Flurstückskennzeichen' ist ein von der Katasterbehörde zur eindeutigen Be-
     * zeichnung des Flurstücks vergebenes Ordnungsmerkmal. Bildungsregel: Die
     * Attributart setzt sich aus den nachfolgenden expliziten Attributarten in der
     * angegebenen Reihenfolge zusammen:
     * <ul>
     * <li>1. Land (2 Stellen)
     * <li>2. Gemarkungsnummer (4 Stellen)
     * <li>3. Flurnummer (3 Stellen)
     * <li>4. Flurstücksnummer
     * <li>4.1 Zähler (5 Stellen)
     * <li>4.2 Nenner (4 Stellen)
     * <li>5. Flurstücksfolge (2 Stellen)
     * </ul>
     * Die Elemente sind rechtsbündig zu belegen, fehlende Stellen sind mit führenden
     * Nullen zu belegen. Da die Flurnummer und die Flurstücksfolge optional sind,
     * sind aufgrund der bundeseinheitlichen Definition im Flurstückskennzeichen die
     * entsprechenden Stellen, sofern sie nicht belegt sind, durch Unterstrich "_"
     * ersetzt. Gleiches gilt für Flurstücksnummern ohne Nenner, hier ist der
     * fehlende Nenner im Flurstückskennzeichen durch Unterstriche zu ersetzen. Die
     * Gesamtlänge des Flurstückkennzeichens beträgt immer 20 Zeichen. Das Attribut
     * ist ein abgeleitetes Attribut und kann nicht gesetzt werden.
     */
    public Property<String>                    flurstueckskennzeichen;
    
    /**
     * 'Amtliche Fläche' ist der im Liegenschaftskataster festgelegte Flächeninhalt
     * des Flurstücks in [qm]. Flurstücksflächen kleiner 0,5 qm können mit bis zu
     * zwei Nachkommastellen geführt werden, ansonsten ohne Nachkommastellen.
     */
    @NameInStore("amtlicheflaeche")
    public Property<Float>                     amtlicheFlaeche;

    /**
     * 'Flurnummer' ist die von der Katasterbehörde zur eindeutigen Bezeichnung
     * vergebene Nummer einer Flur, die eine Gruppe von zusammenhängenden Flurstücken
     * innerhalb einer Gemarkung umfasst.
     */
    public Property<Integer>                   flurnummer;
    
    /**
     * 'Flurstücksfolge' ist eine weitere Angabe zur Flurstücksnummer zum Nachweis
     * der Flurstücksentwicklung.
     */
    @Nullable
    public Property<String>                    flurstuecksfolge;
    
//    /**
//     * 'Objektkoordinaten' sind die Koordinaten [mm] eines das Objekt 'Flurstück'
//     * repräsentierenden Punktes in einem amtlichen Lagebezugssystem. Die
//     * 'Objektkoordinaten' sind übergangsweise aus bestehenden Verfahrenslösungen
//     * übernommen (Datenmigration).
//     */
//    @Nullable
//    public Property<Point>                     objektkoordinaten;
    
    /**
     * 'Sonstige Eigenschaften' sind flurstücksbezogene Informationen, die in dem
     * Datentyp AX_SonstigeEigenschaften enthalten sind. Die Attributart setzt sich
     * zusammen aus:
     * <ul>
     * <li>1. Kennung, Schlüssel gemäß Festlegung im ALB
     * <li>2. Fläche des Abschnitts [qm]
     * <li>3. Angaben zum Abschnitt/Flurstück (unstrukturiert)
     * <li>4. Angaben zum Abschnitt - Stelle
     * <li>5. Angaben zum Abschnitt - Nummer, Aktenzeichen
     * <li>6. Angaben zum Abschnitt - Bemerkung,
     * </ul>
     * Die Angaben zum Abschnitt/Flurstück sind unstrukturiert (3. Stelle) oder
     * strukturiert (4. - 6. Stelle). Die Attributart kommt vor wenn sie
     * übergangsweise im Rahmen der Migration aus bestehenden Verfahrenslösungen
     * benötigt wird oder wenn die Angaben nicht als eigenständige raumbezogene
     * Elementarobjekte aus dem Objektbereich 'Gesetzliche Festlegungen,
     * Zuständigkeiten und Gebietseinheiten' geführt werden.
     */
//    @Nullable
//    public Property<Collection<AX_SonstigeEigenschaften_Flurstueck>> sonstigeEigenschaften;
    
    /**
     * "Zeitpunkt der Entstehung" ist der Zeitpunkt, zu dem das Flurstück fachlich
     * entstanden ist.
     * <p/>
     * Das Attribut kommt vor, wenn der Zeitpunkt der Entstehung von dem Zeitpunkt
     * abweicht, der systemseitig bei der Eintragung in den Bestandsdaten als Anfang
     * der Lebenszeit (siehe Lebenszeitintervall bei Objekten) gesetzt wird. Die
     * Regelungen hierzu sind länderspezifisch gefasst.
     */
    @Nullable
    @NameInStore("zeitpunktderentstehung")
    public Property<Date>                      zeitpunktDerEntstehung;
    
//    /**
//     * 'Gemeindezugehörigkeit' enthält das Gemeindekennzeichen zur Zuordnung der
//     * Flustücksdaten zu einer Gemeinde.
//     */
//    @Nullable
//    public Property<AX_Gemeindekennzeichen>    gemeindezugehoerigkeit;

    
    public String bildeFlurstueckskennzeichen() {
        throw new RuntimeException( "Not yet implemented" );    
    }
    
}
