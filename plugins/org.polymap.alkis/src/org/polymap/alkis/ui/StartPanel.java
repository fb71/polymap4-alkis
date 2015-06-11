/*
 * Copyright (C) 2014-2015, Falko Br�utigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.alkis.ui;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.polymap.alkis.ui.util.UnitOfWorkHolder.Propagation.REQUIRES_NEW_LOCAL;
import static org.polymap.model2.store.geotools.FeatureStoreUnitOfWork.ff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.ui.EntitySearchField;
import org.polymap.rhei.fulltext.ui.FulltextProposal;
import org.polymap.rhei.table.workbench.FeatureTableFilterBar;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.LoginPanel.LoginForm;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.Messages;
import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AlkisRepository;
import org.polymap.model2.store.geotools.FilterWrapper;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class StartPanel
        extends AlkisPanel {

    private static Log log = LogFactory.getLog( StartPanel.class );

    public static final PanelIdentifier     ID  = new PanelIdentifier( "start" );

    private static final IMessages          i18n = Messages.forPrefix( "StartPanel" );
    
    /** Das aktuell selektierte {@link AX_Flurstueck}. */
    private Context<AX_Flurstueck>          selected;

    
    @Override
    public boolean wantsToBeShown() {
        return getSite().getPath().size() == 1;
    }

    
    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Login" );
        getSite().setPreferredWidth( 400 ); // table viewer
        createMainContents( parent );
    }
    
    
    protected void createLoginContents( final Composite parent ) {
        // welcome
        getSite().setTitle( i18n.get( "loginTitle" ) );
        IPanelToolkit tk = getSite().toolkit();
        IPanelSection welcome = tk.createPanelSection( parent, "Willkommen" /*i18n.get( "loginTitle" )*/ );
        welcome.addConstraint( new PriorityConstraint( 10 ), AlkisPlugin.MIN_COLUMN_WIDTH );
        String t = i18n.get( "welcomeText" );
        tk.createFlowText( welcome.getBody(), t );

        // login
        IPanelSection section = tk.createPanelSection( parent, null );
        section.addConstraint( new PriorityConstraint( 0 ), AlkisPlugin.MIN_COLUMN_WIDTH );

        LoginForm loginForm = new LoginPanel.LoginForm( getContext(), getSite(), null /*FIXME user*/ ) {
            @Override
            protected boolean login( String name, String passwd ) {
                if (super.login( name, passwd )) {
                    //getSite().setIcon( WbvPlugin.instance().imageForName( "icons/house.png" ) ); //$NON-NLS-1$
                    getSite().setStatus( new Status( Status.OK, AlkisPlugin.ID, "Erfolgreich angemeldet als: <b>" + name + "</b>" ) );
                    
                    getContext().setUserName( username );
                    
                    for (Control child : parent.getChildren()) {
                        child.dispose();
                    }
                    createMainContents( parent );
                    parent.layout( true );
                    return true;
                }
                else {
                    getSite().setStatus( new Status( Status.ERROR, AlkisPlugin.ID, "Nutzername oder Passwort nicht korrekt." ) );
                    return false;
                }
            }
        };
        loginForm.setShowRegisterLink( false );
        loginForm.setShowStoreCheck( true );
        loginForm.setShowLostLink( true );
        loginForm.createContents( section );
    }
    
    
    protected void createMainContents( Composite parent ) {
        newUnitOfWork( REQUIRES_NEW_LOCAL );
        
        getSite().setTitle( "Flurst�cksuche" /*i18n.get( "title" )*/ );

        IPanelToolkit tk = getSite().toolkit();

//        // results table
//        IPanelSection tableSection = tk.createPanelSection( parent, "Waldbesitzer" );
//        tableSection.addConstraint( new PriorityConstraint( 10 ), WbvPlugin.MIN_COLUMN_WIDTH );
//        tableSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        Composite body = parent;
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        Composite tableLayout = body;  //tk.createComposite( body );
        final FlurstueckTableViewer viewer = new FlurstueckTableViewer( uow().get(), tableLayout, Collections.EMPTY_LIST );
        getContext().propagate( viewer );
        // Details �ffnen
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            @Override
            public void selectionChanged( SelectionChangedEvent ev ) {
                if (!viewer.getSelected().isEmpty()) {
                    selected.set( viewer.getSelected().get( 0 ) );
                    getContext().openPanel( getSite().getPath(), FlurstueckPanel.ID );
                }
            }
        });

        // filterBar
        FeatureTableFilterBar filterBar = new FeatureTableFilterBar( viewer, body );

        // searchField
        FulltextIndex fulltext = AlkisRepository.instance.get().fulltextIndex();
        EntitySearchField search = new EntitySearchField<AX_Flurstueck>( body, fulltext, uow().get(), AX_Flurstueck.class ) {
            @Override
            protected void doSearch( String queryString ) throws Exception {
                Set<Identifier> ids = new HashSet( 1024 );
                for (JSONObject record : index.search( queryString, -1 )) {
                    String id = substringAfterLast( record.getString( FulltextIndex.FIELD_ID ), "." );
                    if (id.length() > 0) {
                        ids.add( ff.featureId( id ) );
                    }
                    else {
                        log.warn( "No FIELD_ID in record: " + record );
                    }
                }
                log.info( "Filter:" + ff.id( ids ) );
                query = uow.query( entityClass ).where( new FilterWrapper( ff.id( ids ) ) );
            }
            @Override
            protected void doRefresh() {
                // SelectionEvent nach refresh() verhindern
                viewer.clearSelection();
//                viewer.setInput( query.execute() );
            }
        };
        
//        search.setSearchOnEnter( false );
//        search.getText().setText( "Im" );
        search.getText().setFocus();
        search.searchOnEnter.set( true );
        new FulltextProposal( fulltext, search.getText() )
                .activationDelayMillis.put( 500 );
        
//        search.getText().addListener( SWT.FocusOut, new Listener() {
//            @Override
//            public void handleEvent( Event ev ) {
//                log.warn( "FOCUS OUT!" );
//            }
//        });

        
        // layout
        int displayHeight = UIUtils.sessionDisplay().getBounds().height;
        int tableHeight = (displayHeight - (2*50) - 75 - 70);  // margins, searchbar, toolbar+banner 
        filterBar.getControl().setLayoutData( FormDataFactory.filled().height( 27 ).noBottom().right( 50 ).create() );
        search.getControl().setLayoutData( FormDataFactory.filled().height( 27 ).noBottom().left( filterBar.getControl() ).create() );
        viewer.getTable().setLayoutData( FormDataFactory.filled()
                .top( search.getControl() ).height( tableHeight ).width( 300 ).create() );
    }

}
