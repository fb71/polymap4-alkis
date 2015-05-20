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

import java.util.List;

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

import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.ui.EntitySearchField;
import org.polymap.rhei.fulltext.ui.FulltextProposal;
import org.polymap.rhei.table.workbench.FeatureTableFilterBar;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.LoginPanel.LoginForm;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.Messages;
import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AlkisRepository;
import org.polymap.alkis.model.alb.Flurstueck;
import org.polymap.alkis.model.alb.Gemarkung;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.ResultSet;

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
        createLoginContents( parent );
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

        LoginForm loginForm = new LoginPanel.LoginForm( getContext(), getSite(), user ) {
            @Override
            protected boolean login( String name, String passwd ) {
                if (super.login( name, passwd )) {
                    getSite().setTitle( i18n.get( "title" ) );
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
        IPanelToolkit tk = getSite().toolkit();

//        // results table
//        IPanelSection tableSection = tk.createPanelSection( parent, "Waldbesitzer" );
//        tableSection.addConstraint( new PriorityConstraint( 10 ), WbvPlugin.MIN_COLUMN_WIDTH );
//        tableSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        Composite body = parent;
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        newUnitOfWork( REQUIRES_NEW_LOCAL );
        
        ResultSet<AX_Flurstueck> all = uow().get().query( AX_Flurstueck.class ).execute();
        log.info( "Query result: " + all.size() );
        
        Composite tableLayout = body;  //tk.createComposite( body );
        final FlurstueckTableViewer viewer = new FlurstueckTableViewer( uow(), tableLayout, all, SWT.NONE );
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

        // waldbesitzer anlegen
        Button createBtn = tk.createButton( body, "Neu", SWT.PUSH );
        createBtn.setToolTipText( "Einen neuen Waldbesitzer anlegen" );
        createBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                selected.set( null );
                getContext().openPanel( getSite().getPath(), FlurstueckPanel.ID );
            }
        });

        // filterBar
        FeatureTableFilterBar filterBar = new FeatureTableFilterBar( viewer, body );

        // searchField
        FullTextIndex fulltext = AlkisRepository.instance.get().fulltextIndex();
        EntitySearchField search = new EntitySearchField<Waldbesitzer>( body, fulltext, uow(), Waldbesitzer.class ) {
            @Override
            protected void doRefresh() {
                if (revier.get() != null) {
                    Waldbesitzer wb = Expressions.template( Waldbesitzer.class, AlkisRepository.instance.get().repo() );
                    Flurstueck fl = Expressions.template( Flurstueck.class, AlkisRepository.instance.get().repo() );
                    
                    List<Gemarkung> gemarkungen = revier.get().gemarkungen;
                    Gemarkung[] revierGemarkungen = gemarkungen.toArray( new Gemarkung[gemarkungen.size()] );
                    query.andWhere( Expressions.anyOf( wb.flurstuecke, 
                                    Expressions.isAnyOf( fl.gemarkung, revierGemarkungen ) ) );
                }
                // SelectionEvent nach refresh() verhindern
                viewer.clearSelection();
                viewer.setInput( query.execute() );
            }
        };
//        search.setSearchOnEnter( false );
//        search.getText().setText( "Im" );
//        search.getText().setFocus();
        search.setSearchOnEnter( true );
        new FulltextProposal( fulltext, search.getText() );
        
        // layout
        int displayHeight = UIUtils.sessionDisplay().getBounds().height;
        int tableHeight = (displayHeight - (2*50) - 75 - 70);  // margins, searchbar, toolbar+banner 
        createBtn.setLayoutData( FormDataFactory.filled().clearRight().clearBottom().create() );
        filterBar.getControl().setLayoutData( FormDataFactory.filled().bottom( viewer.getTable() ).left( createBtn ).right( 50 ).create() );
        search.getControl().setLayoutData( FormDataFactory.filled().height( 27 ).bottom( viewer.getTable() ).left( filterBar.getControl() ).create() );
        viewer.getTable().setLayoutData( FormDataFactory.filled()
                .top( createBtn ).height( tableHeight ).width( 300 ).create() );
        
//        // map
//        IPanelSection karte = tk.createPanelSection( parent, null );
//        karte.addConstraint( new PriorityConstraint( 5 ) );
//        karte.getBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).create() );
//
//        try {
//            map = new WbvMapViewer( getSite() );
//            map.createContents( karte.getBody() )
//                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, tableHeight + 35 ) );
//                    //.setLayoutData( FormDataFactory.filled().height( tableHeight + 35 ).create() );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    
//        // context menu
//        map.getContextMenu().addProvider( new WaldflaechenMenu() );
//        map.getContextMenu().addProvider( new IContextMenuProvider() {
//            @Override
//            public IContextMenuContribution createContribution() {
//                return new FindFeaturesMenuContribution() {
//                    @Override
//                    protected void onMenuOpen( FeatureStore fs, Feature feature, ILayer layer ) {
//                        log.info( "Feature: " + feature );
//                    }
//                };            
//            }
//        });
    }

}
