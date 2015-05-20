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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.IDashlet;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.batik.FormContainer;

import org.polymap.alkis.Messages;
import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.ui.util.PropertyAdapter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurstueckPanel
        extends AlkisPanel {

    private static Log log = LogFactory.getLog( FlurstueckPanel.class );

    public static final PanelIdentifier     ID  = new PanelIdentifier( "flurstueck" );

    private static final IMessages          i18n = Messages.forPrefix( "FlurstueckPanel" );

    /** Das aktuell selektierte {@link AX_Flurstueck}. */
    private Context<AX_Flurstueck>          fst;

    
    @Override
    public void createContents( Composite parent ) {
        Dashboard dashboard = new Dashboard( getSite(), ID.id() )
                .addDashlet( new FlurstueckDashlet( fst.get() ) );
        
        dashboard.createContents( parent );
    }
    
    
    /**
     * 
     */
    public static class FlurstueckDashlet
            extends FormContainer
            implements IDashlet {
        
        private DashletSite         dashletSite;
        
        private AX_Flurstueck       fst;

        
        public FlurstueckDashlet( AX_Flurstueck fst ) {
            this.fst = fst;
        }

        @Override
        public void init( DashletSite site ) {
            this.dashletSite = site;
            this.dashletSite.title.set( "Basisdaten" );
        }

        @Override
        public void createFormContent( IFormEditorPageSite pageSite ) {
            Composite body = pageSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 10, 10 ).columns( 1, 1 ).create() );
            
            createField( body, new PropertyAdapter( fst.flurstuecksnummer.get().zaehler ) )
                    .setLabel( "Zähler" )
                    .create();
        }
        
    }
    
}
