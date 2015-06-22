/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br‰utigam. All rights reserved.
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

import com.google.common.base.Joiner;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;
import org.polymap.rhei.form.batik.BatikFormDashlet;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.Messages;
import org.polymap.alkis.model.AA_Objekt;
import org.polymap.alkis.model.AX_Buchungsblatt;
import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AX_Person;
import org.polymap.alkis.ui.util.PropertyAdapter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
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
        getSite().setTitle( "Flurst¸ck" );

        Dashboard dashboard = new Dashboard( getSite(), ID.id() );

        BatikFormDashlet dashlet = new BatikFormDashlet( new FlurstueckPage( fst.get() ) );
        dashlet.constraints.get().add( new PriorityConstraint( 100 ) );
        dashlet.constraints.get().add( AlkisPlugin.MIN_COLUMN_WIDTH );
        dashlet.title.put( "Basisdaten" ); 
        //dashlet.setEnabled( false );
        dashboard.addDashlet( dashlet );
        
        dashlet = new BatikFormDashlet( new BlattPage( fst.get() ) );
        dashlet.constraints.get().add( new PriorityConstraint( 90 ) );
        dashlet.constraints.get().add( AlkisPlugin.MIN_COLUMN_WIDTH );
        dashlet.title.put( "Buchungsblatt" ); 
        //dashlet.setEnabled( false );
        dashboard.addDashlet( dashlet );
        
        dashboard.addDashlet( new EigentuemerDashlet( fst.get() ) );
        
        dashboard.createContents( parent );
    }
        

    /**
     * 
     */
    public static class EigentuemerDashlet
            extends DefaultDashlet {
        
        private AX_Flurstueck           fst;

        public EigentuemerDashlet( AX_Flurstueck fst ) {
            this.fst = fst;
        }

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            dashletSite.title.set( "Eigent¸mer" );
            dashletSite.constraints.get().add( new PriorityConstraint( 80 ) );
            dashletSite.constraints.get().add( AlkisPlugin.MIN_COLUMN_WIDTH );
        }

        @Override
        public void createContents( Composite parent ) {
            parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).create() );
            
            AX_Buchungsblatt bb = fst.buchungsstelle.get().buchungsblatt.get();
            bb.namensnummern.get().stream().forEach( nn -> {
                nn.person.get().ifPresent( p -> createPersonSection( parent, p ) ); 
            });
        }

        protected void createPersonSection( final Composite parent, AX_Person person ) {
            String title = person.nachnameOderFirma.get();
            if (person.vorname.get() != null) {
                title += ", " + person.vorname.get();
            }
            
            IPanelToolkit tk = dashletSite.panelSite().toolkit();
            final Section section = tk.createSection( parent, title, Section.TREE_NODE /*| Section.SHORT_TITLE_BAR*/ | Section.FOCUS_TITLE );
            //section.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
            ((Composite)section.getClient()).setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
            section.setExpanded( false );

            // KontaktForm
            BatikFormContainer form = new BatikFormContainer( new PersonPage( person ) );
            form.createContents( (Composite)section.getClient() );
            form.getContents().setLayoutData( FormDataFactory.filled().create() );
        }
    }
    
    
    /**
     * 
     */
    public static class PersonPage
            extends AlkisFormPage<AX_Person> {
        
        public PersonPage( AX_Person person ) {
            super( person );
        }
    
        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            
            Composite row = createColumnRow( site.getPageBody(), 2 );
            pageSite.newFormField( new PropertyAdapter( entity.anredeWert ) )
                    .parent.put( row )
                    .label.put( "Anrede" ).create();
    
            pageSite.newFormField( new PropertyAdapter( entity.namensbestandteil ) )
                    .parent.put( row )
                    .label.put( "Zusatz" ).create();
    
            row = createColumnRow( site.getPageBody(), 2 );
            pageSite.newFormField( new PropertyAdapter( entity.nachnameOderFirma ) )
                    .parent.put( row )
                    .label.put( "Name / Firma" ).create();

            pageSite.newFormField( new PropertyAdapter( entity.vorname ) )
                    .parent.put( row )
                    .label.put( "Vorname" ).create();

            row = createColumnRow( site.getPageBody(), 2 );
            pageSite.newFormField( new PropertyAdapter( entity.geburtsname ) )
                    .parent.put( row )
                    .label.put( "Geburtsname" ).create();

            pageSite.newFormField( new PropertyAdapter( entity.geburtsdatum ) )
                    .parent.put( row )
                    .field.put( new StringFormField() )
                    .label.put( "Geburtsdatum" ).create();
            
            // erste Anschrift
            entity.anschrift.get().stream().findFirst().ifPresent( anschrift -> {
                Composite row2 = createColumnRow( site.getPageBody(), 2 );
                pageSite.newFormField( new PropertyAdapter( anschrift.strasse ) )
                        .parent.put( row2 )
                        .label.put( "Straﬂe / HNr." ).create();

                pageSite.newFormField( new PropertyAdapter( anschrift.hausnummer ) )
                        .parent.put( row2 )
                        .label.put( IFormFieldLabel.NO_LABEL ).create();
                
                row2 = createColumnRow( site.getPageBody(), 2 );
                pageSite.newFormField( new PropertyAdapter( anschrift.postleitzahlPostzustellung ) )
                        .parent.put( row2 )
                        .label.put( "PLZ / Ort" ).create();

                pageSite.newFormField( new PropertyAdapter( anschrift.ort_Post ) )
                        .parent.put( row2 )
                        .label.put( IFormFieldLabel.NO_LABEL ).create();
            });
        }
    }


    /**
     * 
     */
    public static class BlattPage
            extends AlkisFormPage<AX_Flurstueck> {
        
        public BlattPage( AX_Flurstueck fst ) {
            super( fst );
        }

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            
            Composite row = createColumnRow( site.getPageBody(), 2 );
            AX_Buchungsblatt blatt = entity.buchungsstelle.get().buchungsblatt.get();
            pageSite.newFormField( new PropertyAdapter( blatt.blattartnummer ) )
                    .parent.put( row )
                    .label.put( "Blattnummer" ).create();

            pageSite.newFormField( new PropertyAdapter( blatt.bezirk ) )
                    .parent.put( row )
                    .label.put( "Buchungsbezirk" ).create();
        }
    }
    
    
    /**
     * 
     */
    public static class FlurstueckPage
            extends AlkisFormPage<AX_Flurstueck> {
        
        public FlurstueckPage( AX_Flurstueck fst ) {
            super( fst );
        }

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            
//            site.setPageTitle( "Flurst¸ck: " 
//                    + entity.gemarkung().bezeichnung.get() + "(" 
//                    + entity.flurnummer.get() + entity);
            
//            EntityHierachyPrinter.on( entity, (entity,assocname,assocType) -> true ).run();
            
            Composite row = createColumnRow( site.getPageBody(), 2 );
            String zaehlerNenner = Joiner.on( " / " ).skipNulls().join( entity.zaehler.get(), entity.nenner.get() );
            pageSite.newFormField( new PlainValuePropertyAdapter( "entity", zaehlerNenner ) )
                    .parent.put( row )
                    .label.put( "Flurst¸ck" ).tooltip.put( "Z‰hler / Nenner" ).create();
            
//            pageSite.newFormField( new PropertyAdapter( entity.nenner ) )
//                    .parent.put( row )
//                    .label.put( IFormFieldLabel.NO_LABEL ).create();

            pageSite.newFormField( new PropertyAdapter( entity.flurstueckskennzeichen ) )
                    .parent.put( row )
                    .label.put( "Kennzeichen" ).create();

            row = createColumnRow( site.getPageBody(), 2 );
            pageSite.newFormField( new PlainValuePropertyAdapter( "gemeinde", entity.gemeinde().bezeichnung.get() ) )
                    .parent.put( row )
                    .label.put( "Gemeinde" ).create();
            
            pageSite.newFormField( new PlainValuePropertyAdapter( "gemarkung", entity.gemarkung().bezeichnung.get() ) )
                    .parent.put( row )
                    .label.put( "Gemarkung" ).create();

            row = createColumnRow( site.getPageBody(), 2 );
            pageSite.newFormField( new PropertyAdapter( entity.flurnummer ) )
                    .parent.put( row )
                    .label.put( "Flur" ).create();
    
            pageSite.newFormField( new PropertyAdapter( entity.amtlicheFlaeche ) )
                    .parent.put( row )
                    .label.put( "Amtl. Fl‰che (m≤)" ).create();

            String lage = "???"; //entity.lagebezeichnung.get().stream().map( lbz -> lbz.unverschluesselt.get() ).reduce( (s1,s2) -> s1 + " " + s2 ).orElse( "" );
            pageSite.newFormField( new PlainValuePropertyAdapter( "lage", lage ) )
                    .label.put( "Lage" ).create();

            String hinweis = "???"; //entity.lagebezeichnungOhne.get().stream().map( lbz -> lbz.unverschluesselt.get() ).reduce( (s1,s2) -> "" + s1 + " " + s2 ).orElse( "" );
            pageSite.newFormField( new PlainValuePropertyAdapter( "hinweis", hinweis ) )
                    .label.put( "Hinweis" ).create();
        }
    }

    
    /**
     * 
     */
    public abstract static class AlkisFormPage<T extends AA_Objekt>
            extends DefaultFormPage {
        
        protected T                 entity;
        
        public AlkisFormPage( T entity ) {
            this.entity = entity;
        }

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            
            site.getPageBody().setLayout( 
                    ColumnLayoutFactory.defaults().spacing( 3 ).margins( 5 ).columns( 1, 1 ).create() );
        }
        
        protected Composite createColumnRow( Composite parent, int cols ) {
            Composite result = pageSite.getToolkit().createComposite( parent );
            result.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 0 ).columns( cols, cols ).create() );
            return result;
        }
    }

}
