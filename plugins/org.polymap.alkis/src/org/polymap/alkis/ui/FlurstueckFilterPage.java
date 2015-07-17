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
package org.polymap.alkis.ui;

import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BB;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_ANSCHRIFT;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_GEBURTSNAME;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_NAME;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_VORNAME;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.FLUR;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.GMD;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.GMK;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.LAGE;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.NENNER;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.ZAEHLER;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.filter.DefaultFilterPage;
import org.polymap.rhei.filter.IFilterPage2;
import org.polymap.rhei.filter.IFilterPageSite;

import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class FlurstueckFilterPage
        extends DefaultFilterPage
        implements IFilterPage2 {

    private static Log log = LogFactory.getLog( FlurstueckFilterPage.class );

    protected IFilterPageSite           pageSite;
    
    protected String                    queryString;
    
    
    @Override
    public void createFilterContents( IFilterPageSite site ) {
        this.pageSite = site;
        
        site.getPageBody().setLayout( ColumnLayoutFactory.defaults().margins( 100, 10 ).spacing( 3 ).columns( 1, 2 ).create() );
        
        PropertyInfo propInfo = AX_Flurstueck.TYPE.zaehler.info();
        site.newFilterField( ZAEHLER, propInfo.getType() ).label.put( "Z�hler" ).create();

        propInfo = AX_Flurstueck.TYPE.nenner.info();
        site.newFilterField( NENNER, propInfo.getType() ).label.put( "Nenner" ).create();

        site.newFilterField( FLUR, Integer.class ).label.put( "Flur" ).create();
        site.newFilterField( LAGE, String.class ).label.put( "Lage*" )
                .tooltip.put( "Zeichenkette, die in Lagebezeichnung mit/ohne Hausnummer oder verschl�sselte Lage enthalten ist" ).create();

        site.newFilterField( GMK, String.class ).label.put( "Gemarkung" ).create();
        site.newFilterField( GMD, String.class ).label.put( "Gemeinde" ).create();        

        site.newFilterField( BB, String.class ).label.put( "Buchungsblatt*" )
                .tooltip.put( "Zeichenkette, die in Buchungsblattnummer oder -Kennzeichen enthalten ist" ).create();        

        site.newFilterField( BESITZER_NAME, String.class ).label.put( "Besitzer-Name*" )
                .tooltip.put( "Zeichenkette, die im Namen oder Firma des Besitzers enthalten ist" ).create();
        
        site.newFilterField( BESITZER_VORNAME, String.class ).label.put( "Besitzer-Vorname*" )
                .tooltip.put( "Zeichenkette, die im Vornamen des Besitzers enthalten ist" ).create();        
        
        site.newFilterField( BESITZER_GEBURTSNAME, String.class ).label.put( "Geburtsname*" )
                .tooltip.put( "Zeichenkette, die im Vornamen des Besitzers enthalten ist" ).create();
        
        site.newFilterField( BESITZER_ANSCHRIFT, String.class ).label.put( "Besitzer-Anschrift*" )        
                .tooltip.put( "Zeichenkette, die in Ort, PLZ, Strasse oder Hausnummer enthalten ist" ).create();
    }

    
    @Override
    public Filter doBuildFilter( Filter filter, IProgressMonitor monitor ) throws Exception {
        StringBuilder result = new StringBuilder( 256 );
        appendQueryField( ZAEHLER, result, false );
        appendQueryField( NENNER, result, false );
        appendQueryField( GMD, result, false );
        appendQueryField( GMK, result, false );
        appendQueryField( FLUR, result, false );
        appendQueryField( BB, result, true );
        appendQueryField( LAGE, result, true );
        appendQueryField( BESITZER_NAME, result, true );
        appendQueryField( BESITZER_GEBURTSNAME, result, true );
        appendQueryField( BESITZER_VORNAME, result, true );
        appendQueryField( BESITZER_ANSCHRIFT, result, true );
        queryString = result.toString();
        return filter;
    }

    
    protected void appendQueryField( String field, StringBuilder result, boolean isLike ) {
        Object value = pageSite.getFieldValue( field );
        if (value != null) {
            String stringValue = value.toString();
            if (stringValue.length() > 0) {
                if (result.length() > 0) {
                    result.append( " AND " );
                }
                
                if (!stringValue.contains( "*" )) {
                    stringValue = "\"" + stringValue + "\"";
                }
                result.append( field ).append( ":").append( stringValue );
                
//                if (stringValue.contains( "*" )) {
//                    result.append( field ).append( ":").append( stringValue );
//                }
//                else if (isLike) {
//                    result.append( field ).append( ":\"*").append( stringValue ).append( "*\"" );
//                }
//                else {
//                    result.append( field ).append( ":\"").append( stringValue ).append( "\"" );
//                }
            }
        }
    }
    
    
    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void doLoad( IProgressMonitor monitor ) throws Exception {
    }

    @Override
    public void dispose() {
    }
    
}
