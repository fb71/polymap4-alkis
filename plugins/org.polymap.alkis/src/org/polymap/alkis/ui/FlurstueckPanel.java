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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
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
import org.polymap.rap.openlayers.base.OlFeature;
import org.polymap.rap.openlayers.base.OlMap;
import org.polymap.rap.openlayers.format.GeoJSONFormat;
import org.polymap.rap.openlayers.geom.PolygonGeometry;
import org.polymap.rap.openlayers.layer.VectorLayer;
import org.polymap.rap.openlayers.source.VectorSource;
import org.polymap.rap.openlayers.style.FillStyle;
import org.polymap.rap.openlayers.style.StrokeStyle;
import org.polymap.rap.openlayers.style.Style;
import org.polymap.rap.openlayers.types.Attribution;
import org.polymap.rap.openlayers.types.Color;
import org.polymap.rap.openlayers.types.Coordinate;
import org.polymap.rap.openlayers.types.Projection;
import org.polymap.rap.openlayers.types.Projection.Units;
import org.polymap.rap.openlayers.view.View;

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
        site().title.set( "Flurstück" );
        site().preferredWidth.set( 700 );

        Dashboard dashboard = new Dashboard( getSite(), ID.id() );

        BatikFormDashlet dashlet = new BatikFormDashlet( new FlurstueckPage( fst.get() ) );
        dashlet.setEnabled( false );
        dashlet.constraints.get().add( new PriorityConstraint( 100 ) );
        dashlet.constraints.get().add( AlkisPlugin.MIN_COLUMN_WIDTH );
        dashlet.title.put( "Basisdaten" ); 
        //dashlet.setEnabled( false );
        dashboard.addDashlet( dashlet );
        
        dashlet = new BatikFormDashlet( new BlattPage( fst.get() ) );
        dashlet.setEnabled( false );
        dashlet.constraints.get().add( new PriorityConstraint( 90 ) );
        dashlet.constraints.get().add( AlkisPlugin.MIN_COLUMN_WIDTH );
        dashlet.title.put( "Buchungsblatt" ); 
        //dashlet.setEnabled( false );
        dashboard.addDashlet( dashlet );
        
        dashboard.addDashlet( new EigentuemerDashlet( fst.get() ) );
        dashboard.addDashlet( new MapDashlet( fst.get() ) );
        
        dashboard.createContents( parent );
    }
        

    /**
     * 
     */
    public static class MapDashlet
            extends DefaultDashlet {
        
        private AX_Flurstueck           fst;

        public MapDashlet( AX_Flurstueck fst ) {
            this.fst = fst;
        }

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            dashletSite.title.set( "Karte" );
            dashletSite.constraints.get().add( new PriorityConstraint( 0 ) );
            dashletSite.constraints.get().add( new MinWidthConstraint( 550, 1 ) );
            dashletSite.isExpandable.set( true );
        }

        @Override
        public void createContents( Composite parent ) {
            parent.setLayout( FormLayoutFactory.defaults().create() );
            Button btn = dashletSite.toolkit().createButton( parent, "Anzeigen...", SWT.PUSH );
            FormDataFactory.on( btn ).left( 0 ).top( 0 );
            btn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    btn.dispose();
                    createMapContents( parent );
                    dashletSite.panelSite().layout( true );
                }
            });
        }
        

        protected void createMapContents( Composite parent ) {
            // map
            OlMap map = new OlMap( parent, SWT.NONE, new View()
                    .projection.put( new Projection( "EPSG:3857", Units.m ) )
                    .center.put( new Coordinate( 1387648, 6688702 ) )
                    .zoom.put( 14 ) );
            
            map.setLayoutData( FormDataFactory.filled().height( 500 ).create() );

//            // OSM
//            map.addLayer( new ImageLayer()
//                    .source.put( new ImageWMSSource()
//                            .url.put( "http://ows.terrestris.de/osm/service/" )
//                            .params.put( new ImageWMSSource.RequestParams().layers.put( "OSM-WMS" ) ) ) );
            // DTK
//            map.addLayer( new ImageLayer()
//                    .source.put( new ImageWMSSource()
//                            .url.put( "http://sec.geodatenportal.sachsen-anhalt.de/gateway/gateto/lvermgeo_intern-GDI-LSA_LVermGeo_DTKcolor_OpenData?" )
//                            .params.put( new ImageWMSSource.RequestParams().layers.put( "DTK100" ) ) ) );
            
            // vector source/layer
            VectorSource vectorSource = new VectorSource()
                    .format.put( new GeoJSONFormat() )
                    .attributions.put( Arrays.asList( new Attribution( "ALKIS" ) ) );

            VectorLayer vectorLayer = new VectorLayer()
                    .style.put( new Style()
                    .fill.put( new FillStyle().color.put( new Color( 0, 0, 255, 0.2f ) ) )
                    .stroke.put( new StrokeStyle().color.put( new Color( "red" ) ).width.put( 2f ) ) )
                    .source.put( vectorSource );

            map.addLayer( vectorLayer );

            // feature
            try {
                Polygon geom = fst.geom.get();
                Polygon transformed = Geometries.transform( geom, "EPSG:25832", "EPSG:3857" );
                
                List<Coordinate> coords = Arrays.stream( transformed.getCoordinates() )
                        .map( c -> new Coordinate( c.x, c.y ) )
                        .collect( Collectors.toList() );

                OlFeature feature = new OlFeature();
                feature.name.set( "Flurstück" );
                feature.geometry.set( new PolygonGeometry( coords ) );
                vectorSource.addFeature( feature );
                
                Point center = transformed.getCentroid();
                map.view.get()
                        .center.put( new Coordinate( center.getX(), center.getY() ) );
            }
            catch (Exception e) {
                StatusDispatcher.handleError( "", e );
                log.error( "", e );
            }
        }
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
            dashletSite.title.set( "Eigentümer" );
            dashletSite.constraints.get().add( new PriorityConstraint( 80 ) );
            dashletSite.constraints.get().add( new MinWidthConstraint( 650, 1 ) );
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
            form.setEnabled( false );
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
            pageSite.newFormField( new PlainValuePropertyAdapter( "anrede", entity.anrede() ) )
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
//                    .validator.put( new NullValidator() {
//                        @Override
//                        public Object transform2Field( Object modelValue ) throws Exception {
//                            DateFormat df = SimpleDateFormat.getDateInstance( SimpleDateFormat.SHORT, Locale.GERMAN );
//                            return modelValue != null ? df.format( modelValue ) : "";
//                        }
//                    })
                    .label.put( "Geburtsdatum" ).create();
            
            // erste Anschrift
            entity.anschrift.get().stream().findFirst().ifPresent( anschrift -> {
                Composite row2 = createColumnRow( site.getPageBody(), 2 );
                pageSite.newFormField( new PropertyAdapter( anschrift.strasse ) )
                        .parent.put( row2 )
                        .label.put( "Straße / HNr." ).create();

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
            pageSite.newFormField( new PropertyAdapter( blatt.nummer ) )
                    .parent.put( row )
                    .label.put( "Blattnummer" ).create();

            String bezirk = Joiner.on( " - " ).join( blatt.bezirk().bezeichnung.get(), blatt.bezirknummer.get() );
            pageSite.newFormField( new PlainValuePropertyAdapter( "bezirk", bezirk ) )
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
            
//            site.setPageTitle( "Flurstück: " 
//                    + entity.gemarkung().bezeichnung.get() + "(" 
//                    + entity.flurnummer.get() + entity);
            
//            EntityHierachyPrinter.on( entity, (entity,assocname,assocType) -> true ).run();
            
            Composite row = createColumnRow( site.getPageBody(), 2 );
            String zaehlerNenner = Joiner.on( " / " ).skipNulls().join( entity.zaehler.get(), entity.nenner.get() );
            pageSite.newFormField( new PlainValuePropertyAdapter( "entity", zaehlerNenner ) )
                    .parent.put( row )
                    .label.put( "Flurstück" ).tooltip.put( "Zähler / Nenner" )
                    .create();
            
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
                    .label.put( "Amtl. Fläche (m²)" ).create();

            StringBuilder lage = new StringBuilder( 1024 );
            entity.lagebezeichnung.get().forEach( lbz -> {
                    lbz.katalogeintrag().ifPresent( e -> lage.append( e.bezeichnung.get() ).append( " " ) );
                    lage.append( lbz.hausnummer.get() != null ? lbz.hausnummer.get()+" " : "" );
            });
            pageSite.newFormField( new PlainValuePropertyAdapter( "lage", lage.toString() ) )
                    .label.put( "Lage" ).create();

            StringBuilder hinweis = new StringBuilder( 1024 );
            entity.lagebezeichnungOhne.get().forEach( lbz -> {
                    lbz.katalogeintrag().ifPresent( e -> hinweis.append( e.bezeichnung.get() ).append( " " ) );
                    hinweis.append( lbz.unverschluesselt.get() != null ? lbz.unverschluesselt.get()+" " : "" );
            });
            pageSite.newFormField( new PlainValuePropertyAdapter( "hinweis", hinweis.toString() ) )
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
