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
package org.polymap.alkis.ui;

import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_ANSCHRIFT;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.BESITZER_NAME;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.GMD;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.GMK;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.NENNER;
import static org.polymap.alkis.model.fulltext.FlurstueckTransformer.ZAEHLER;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.filter.DefaultFilterPage;
import org.polymap.rhei.filter.IFilterPage2;
import org.polymap.rhei.filter.IFilterPageSite;

import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
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
        PropertyInfo propInfo = AX_Flurstueck.TYPE.zaehler.info();
        site.newFilterField( ZAEHLER, propInfo.getType() ).label.put( "Zähler" ).create();

        propInfo = AX_Flurstueck.TYPE.nenner.info();
        site.newFilterField( NENNER, propInfo.getType() ).label.put( "Nenner" ).create();

        site.newFilterField( GMK, String.class ).label.put( "Gemarkung" ).create();
        site.newFilterField( GMD, String.class ).label.put( "Gemeinde" ).create();        

        site.newFilterField( BESITZER_NAME, String.class ).label.put( "Besitzer Name" ).create();
        site.newFilterField( BESITZER_ANSCHRIFT, String.class ).label.put( "Anschrift" ).create();        
    }

    
    @Override
    public Filter doBuildFilter( Filter filter, IProgressMonitor monitor ) throws Exception {
        StringBuilder result = new StringBuilder( 256 );
        appendQueryField( ZAEHLER, result, false );
        appendQueryField( NENNER, result, false );
        appendQueryField( GMD, result, false );
        appendQueryField( GMK, result, false );
        appendQueryField( BESITZER_NAME, result, true );
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
                stringValue = isLike ? "*" + stringValue + "*" : stringValue;
                result.append( field ).append( ":\"").append( stringValue ).append( "\"" );
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
