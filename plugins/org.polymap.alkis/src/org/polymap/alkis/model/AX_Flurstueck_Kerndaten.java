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

import static org.polymap.alkis.model.AlkisRepository.ff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

import org.polymap.model2.NameInStore;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.store.geotools.FilterWrapper;

/**
 * 'Flurst�ck_Kerndaten' enth�lt Eigenschaften des Flurst�cks, die auch f�r andere
 * Flurst�cksobjektarten gelten (z.B. Historisches Flurst�ck). Es handelt sich um
 * eine abstrakte Objektart.
 * 
 * @Kennung 11004
 * @Modellart DLKM
 * @Grunddatenbestand DLKM
 * @version ALKIS-OK 6.0 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class AX_Flurstueck_Kerndaten
        extends AA_NREO {

    private static Log log = LogFactory.getLog( AX_Flurstueck_Kerndaten.class );

    /**
     * 'Land' enth�lt den Schl�ssel f�r das Bundesland.
     * Teil des Gesamtschl�ssels f�r die Gemeinde.
     */
    protected Property<String>                  land;

    /**
     * Teil des Gesamtschl�ssels f�r die Gemeinde.
     */
    protected Property<String>                  kreis;

    /**
     * Teil des Gesamtschl�ssels f�r die Gemeinde.
     */
    protected Property<String>                  regierungsbezirk;

    @NameInStore("gemeinde")
    protected Property<String>                  gemeinde;
    
    private static Cache<List<String>,String>   gmdBezeichnungen = CacheConfig.defaults().createCache();
    
    /**
     * Bezeichnung der Gemeinde. 
     */
    public String gemeinde() {
        ArrayList<String> key = Lists.newArrayList( land.get(), regierungsbezirk.get(), kreis.get(), gemeinde.get() );
        return gmdBezeichnungen.get( key, k -> {
            FilterWrapper filter = new FilterWrapper( ff.and( Lists.newArrayList( 
                    ff.equals( ff.property( AX_Gemeinde.TYPE.land.info().getNameInStore() ), ff.literal( land.get() ) ),
                    ff.equals( ff.property( AX_Gemeinde.TYPE.regierungsbezirk.info().getNameInStore() ), ff.literal( regierungsbezirk.get() ) ),
                    ff.equals( ff.property( AX_Gemeinde.TYPE.kreis.info().getNameInStore() ), ff.literal( kreis.get() ) ),
                    ff.equals( ff.property( AX_Gemeinde.TYPE.gemeinde.info().getNameInStore() ), ff.literal( gemeinde.get() ) )
                    ) ) );
            try (
                ResultSet<AX_Gemeinde> rs = context.getUnitOfWork().query( AX_Gemeinde.class ).where( filter ).execute();
            ){
                AX_Gemeinde result = Iterables.getOnlyElement( rs );
                return result.bezeichnung.get();
            }
        });
    }
    
    /**
     * 'Gemarkungsnummer' enth�lt die von der Katasterbeh�rde zur eindeutigen
     * Bezeichnung der Gemarkung vergebene Nummer innerhalb eines Bundeslandes.
     */
    protected Property<String>                  gemarkungsnummer;
    
    private static Cache<List<String>,String>   gmkBezeichnungen = CacheConfig.defaults().createCache();
    
    /**
     * Bezeichnung der Gemarkung. 
     */
    public String gemarkung() {
        ArrayList<String> key = Lists.newArrayList( land.get(), gemarkungsnummer.get() );
        return gmdBezeichnungen.get( key, k -> {
            FilterWrapper filter = new FilterWrapper( ff.and( Lists.newArrayList( 
                    ff.equals( ff.property( AX_Gemarkung.TYPE.land.info().getNameInStore() ), ff.literal( land.get() ) ),
                    ff.equals( ff.property( AX_Gemarkung.TYPE.gemarkungsnummer.info().getNameInStore() ), ff.literal( gemarkungsnummer.get() ) )
                    ) ) );
            try (
                ResultSet<AX_Gemarkung> rs = context.getUnitOfWork().query( AX_Gemarkung.class ).where( filter ).execute();
            ){
                AX_Gemarkung result = Iterables.getOnlyElement( rs );
                return result.bezeichnung.get();
            }
        });
    }
    
    
//    /**
//     * 'Flurst�cksnummer' ist die Bezeichnung (Z�hler/Nenner), mit der ein Flurst�ck
//     * innerhalb einer Flur (Flurnummer muss im Land vorhanden sein) oder Gemarkung
//     * identifiziert werden kann. Das Attribut setzt sich zusammen aus: 1. Spalte:
//     * Z�hler 2. Spalte: Nenner Die 2. Spalte ist optional.
//     */
//    public Property<AX_Flurstuecksnummer>      flurstuecksnummer;

    public Property<Integer>                    zaehler;
    
    @Nullable
    public Property<String>                     nenner;
    
    /**
     * 'Flurst�ckskennzeichen' ist ein von der Katasterbeh�rde zur eindeutigen Be-
     * zeichnung des Flurst�cks vergebenes Ordnungsmerkmal. Bildungsregel: Die
     * Attributart setzt sich aus den nachfolgenden expliziten Attributarten in der
     * angegebenen Reihenfolge zusammen:
     * <ul>
     * <li>1. Land (2 Stellen)
     * <li>2. Gemarkungsnummer (4 Stellen)
     * <li>3. Flurnummer (3 Stellen)
     * <li>4. Flurst�cksnummer
     * <li>4.1 Z�hler (5 Stellen)
     * <li>4.2 Nenner (4 Stellen)
     * <li>5. Flurst�cksfolge (2 Stellen)
     * </ul>
     * Die Elemente sind rechtsb�ndig zu belegen, fehlende Stellen sind mit f�hrenden
     * Nullen zu belegen. Da die Flurnummer und die Flurst�cksfolge optional sind,
     * sind aufgrund der bundeseinheitlichen Definition im Flurst�ckskennzeichen die
     * entsprechenden Stellen, sofern sie nicht belegt sind, durch Unterstrich "_"
     * ersetzt. Gleiches gilt f�r Flurst�cksnummern ohne Nenner, hier ist der
     * fehlende Nenner im Flurst�ckskennzeichen durch Unterstriche zu ersetzen. Die
     * Gesamtl�nge des Flurst�ckkennzeichens betr�gt immer 20 Zeichen. Das Attribut
     * ist ein abgeleitetes Attribut und kann nicht gesetzt werden.
     */
    public Property<String>                     flurstueckskennzeichen;
    
    /**
     * 'Amtliche Fl�che' ist der im Liegenschaftskataster festgelegte Fl�cheninhalt
     * des Flurst�cks in [qm]. Flurst�cksfl�chen kleiner 0,5 qm k�nnen mit bis zu
     * zwei Nachkommastellen gef�hrt werden, ansonsten ohne Nachkommastellen.
     */
    @NameInStore("amtlicheflaeche")
    public Property<Double>                     amtlicheFlaeche;

    /**
     * 'Flurnummer' ist die von der Katasterbeh�rde zur eindeutigen Bezeichnung
     * vergebene Nummer einer Flur, die eine Gruppe von zusammenh�ngenden Flurst�cken
     * innerhalb einer Gemarkung umfasst.
     */
    public Property<Integer>                    flurnummer;
    
    /**
     * 'Flurst�cksfolge' ist eine weitere Angabe zur Flurst�cksnummer zum Nachweis
     * der Flurst�cksentwicklung.
     */
    @Nullable
    public Property<String>                     flurstuecksfolge;
    
//    /**
//     * 'Objektkoordinaten' sind die Koordinaten [mm] eines das Objekt 'Flurst�ck'
//     * repr�sentierenden Punktes in einem amtlichen Lagebezugssystem. Die
//     * 'Objektkoordinaten' sind �bergangsweise aus bestehenden Verfahrensl�sungen
//     * �bernommen (Datenmigration).
//     */
//    @Nullable
//    public Property<Point>                     objektkoordinaten;
    
    /**
     * 'Sonstige Eigenschaften' sind flurst�cksbezogene Informationen, die in dem
     * Datentyp AX_SonstigeEigenschaften enthalten sind. Die Attributart setzt sich
     * zusammen aus:
     * <ul>
     * <li>1. Kennung, Schl�ssel gem�� Festlegung im ALB
     * <li>2. Fl�che des Abschnitts [qm]
     * <li>3. Angaben zum Abschnitt/Flurst�ck (unstrukturiert)
     * <li>4. Angaben zum Abschnitt - Stelle
     * <li>5. Angaben zum Abschnitt - Nummer, Aktenzeichen
     * <li>6. Angaben zum Abschnitt - Bemerkung,
     * </ul>
     * Die Angaben zum Abschnitt/Flurst�ck sind unstrukturiert (3. Stelle) oder
     * strukturiert (4. - 6. Stelle). Die Attributart kommt vor wenn sie
     * �bergangsweise im Rahmen der Migration aus bestehenden Verfahrensl�sungen
     * ben�tigt wird oder wenn die Angaben nicht als eigenst�ndige raumbezogene
     * Elementarobjekte aus dem Objektbereich 'Gesetzliche Festlegungen,
     * Zust�ndigkeiten und Gebietseinheiten' gef�hrt werden.
     */
//    @Nullable
//    public Property<Collection<AX_SonstigeEigenschaften_Flurstueck>> sonstigeEigenschaften;
    
    /**
     * "Zeitpunkt der Entstehung" ist der Zeitpunkt, zu dem das Flurst�ck fachlich
     * entstanden ist.
     * <p/>
     * Das Attribut kommt vor, wenn der Zeitpunkt der Entstehung von dem Zeitpunkt
     * abweicht, der systemseitig bei der Eintragung in den Bestandsdaten als Anfang
     * der Lebenszeit (siehe Lebenszeitintervall bei Objekten) gesetzt wird. Die
     * Regelungen hierzu sind l�nderspezifisch gefasst.
     */
    @Nullable
    @NameInStore("zeitpunktderentstehung")
    public Property<Date>                      zeitpunktDerEntstehung;
    
//    /**
//     * 'Gemeindezugeh�rigkeit' enth�lt das Gemeindekennzeichen zur Zuordnung der
//     * Flust�cksdaten zu einer Gemeinde.
//     */
//    @Nullable
//    public Property<AX_Gemeindekennzeichen>    gemeindezugehoerigkeit;

    
    public String bildeFlurstueckskennzeichen() {
        throw new RuntimeException( "Not yet implemented" );    
    }
    
}
