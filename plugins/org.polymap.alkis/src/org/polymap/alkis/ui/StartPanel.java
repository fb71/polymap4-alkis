/*
 * Copyright (C) 2014-2015, Falko Bräutigam. All rights reserved.
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

import static org.polymap.alkis.ui.util.UnitOfWorkHolder.Propagation.REQUIRES_NEW_LOCAL;

import java.util.Collections;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.form.batik.BatikFilterContainer;
import org.polymap.rhei.form.batik.BatikFormContainer;
import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.ui.EntitySearchField;
import org.polymap.rhei.fulltext.ui.FulltextProposal;
import org.polymap.rhei.table.FeatureTableFilterBar;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.LoginPanel.LoginForm;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.Messages;
import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AlkisRepository;
import org.polymap.model2.query.Query;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
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
//        createLoginContents( parent );
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
        new BatikFormContainer( loginForm ).createContents( section );
    }
    
    
    protected void createMainContents( Composite parent ) {
        newUnitOfWork( REQUIRES_NEW_LOCAL );
        
        getSite().setTitle( "Flurstücksuche" /*i18n.get( "title" )*/ );

        IPanelToolkit tk = getSite().toolkit();

        Composite body = parent;
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        Composite tableLayout = body;  //tk.createComposite( body );
        final FlurstueckTableViewer viewer = new FlurstueckTableViewer( uow().get(), tableLayout, Collections.EMPTY_LIST );
        getContext().propagate( viewer );
        // Details öffnen
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

        Button searchFormBtn = tk.createButton( body, "Suchformular", SWT.TOGGLE );
        searchFormBtn.setToolTipText( "Suchformular öffnen" );
        searchFormBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        });
                
        // searchField
        FulltextIndex fulltext = AlkisRepository.instance.get().fulltextIndex();
        EntitySearchField searchField = new EntitySearchField<AX_Flurstueck>( body, fulltext, uow().get(), AX_Flurstueck.class ) {
            @Override
            protected void doSearch( String queryString ) throws Exception {
                query = AlkisRepository.instance.get().fulltextQuery( queryString, uow );
            }
            @Override
            protected void doRefresh() {
                // SelectionEvent nach refresh() verhindern
                viewer.clearSelection();
                viewer.setInput( query.execute() );
            }
        };

        // search form
        BatikFilterContainer searchForm = new BatikFilterContainer( new FlurstueckFilterPage() {
            @Override
            public Filter doBuildFilter( Filter filter, IProgressMonitor monitor ) throws Exception {
                super.doBuildFilter( filter, monitor );                
                log.info( "Query: " + queryString.toString() );
                
                Query<AX_Flurstueck> query = AlkisRepository.instance.get().fulltextQuery( queryString, uow().get() );

                // SelectionEvent nach refresh() verhindern
                viewer.clearSelection();
                viewer.setInput( query.execute() );
                return filter;
            }
        } );
        searchForm.createContents( body );
//        UIUtils.setVariant( tk.adapt( searchForm.getContents() ), "alkis-search" );
        
        Button searchBtn = tk.createButton( searchForm.getContents(), "Start", SWT.PUSH );
        searchBtn.setToolTipText( "Suche starten" );
        searchBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    searchForm.buildFilter();
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "", e );
                }
            }
        });
        
        searchField.searchOnEnter.set( false );
        searchField.getText().setText( "105 " );
        searchField.searchOnEnter.set( true );
        searchField.getText().setFocus();
        new FulltextProposal( fulltext, searchField.getText() )
                .activationDelayMillis.put( 500 );
        
        // layout
        int displayHeight = UIUtils.sessionDisplay().getBounds().height;
        int tableHeight = (displayHeight - (2*50) - 75 - 70);  // margins, searchbar, toolbar+banner 
        searchFormBtn.setLayoutData( FormDataFactory.filled()
                .height( 27 ).noRight().noBottom().create() );
        filterBar.getControl().setLayoutData( FormDataFactory.filled()
                .height( 27 ).left( searchFormBtn ).noBottom().right( 50 ).create() );
        searchField.getControl().setLayoutData( FormDataFactory.filled()
                .height( 27 ).noBottom().left( filterBar.getControl() ).create() );
        searchForm.getContents().setLayoutData( FormDataFactory.filled()
                .height( 200 ).top( searchField.getControl() ).noBottom().create() );
        viewer.getTable().setLayoutData( FormDataFactory.filled()
                .top( searchForm.getContents() ).height( tableHeight - 160 ).width( 300 ).create() );
    }

}
