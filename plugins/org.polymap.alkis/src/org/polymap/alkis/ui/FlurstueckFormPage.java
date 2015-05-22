/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.alkis.ui;

import java.util.Collection;

import java.io.IOException;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.table.CollectionContentProvider;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureTableViewer;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.model.alb.ALBRepository;
import org.polymap.alkis.model.alb.Abschnitt;
import org.polymap.alkis.model.alb.Flurstueck;
import org.polymap.alkis.model.alb.Gemarkung;
import org.polymap.alkis.model.alb.Lagehinweis2;
import org.polymap.model2.runtime.Entities;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurstueckFormPage
        extends DefaultFormEditorPage
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( FlurstueckFormPage.class );

    private ALBRepository           repo;
    
    protected UnitOfWork            uow;

    
    public FlurstueckFormPage( Feature feature, FeatureStore fs ) {
        super( "__ALBFLU__", "Flurstück", feature, fs );
    }


    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site ); 

        repo = ALBRepository.instance();
        uow = repo.newUnitOfWork();
        
        // den Titel des gesamten Formulareditors setzen; 
        // dieser Titel wird oberhalb des Formulars angezeigt
        Flurstueck flurstueck = uow.entityForState( Flurstueck.class, feature );
        site.setFormTitle( "Flurstück (" + flurstueck.zaehler.get() + "/" + flurstueck.nenner.get() + ")" );
        
        Section fsection = createFlurstueckSection( flurstueck );
        fsection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( 0, 0 ).create() );

        Section gsection = createGemarkungSection( flurstueck );
        gsection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( 0, 0 ).create() );

        Section asection = createAbschnitteSection( flurstueck );
        asection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( gsection, 15 ).create() );

        Section lsection = createLagehinweiseSection( flurstueck );
        lsection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( fsection, 15 ).create() );
    }
    
    
    @Override
    protected FormFieldBuilder newFormField( String propName ) {
        return super.newFormField( propName ).setEnabled( false );
    }


    protected Section createFlurstueckSection( Flurstueck flurstueck ) {
        Section section1 = newSection( "Basisdaten", false, null );
        
        newFormField( flurstueck.status.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Status" ).create();

        newFormField( flurstueck.zaehler.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Zähler" ).create();

        newFormField( flurstueck.nenner.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Nenner" ).create();
        
        newFormField( flurstueck.strasse.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Strasse" ).create();

        newFormField( flurstueck.hnr.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Hausnr." ).create();

        newFormField( flurstueck.hnrZusatz.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Zusatz" ).create();
        
        newFormField( flurstueck.flaeche.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Fläche (m²)" ).create();

        newFormField( flurstueck.erfasst.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Erfasst" ).create();

        newFormField( flurstueck.geaendert.info().getNameInStore() )
                .setParent( section1 ).setLabel( "Geändert" ).create();
        return section1;
    }


    protected Section createGemarkungSection( Flurstueck flurstueck ) {
        Section section = newSection( "Gemarkung", false, null );

        Gemarkung gemarkung = flurstueck.gemarkung();
        if (gemarkung != null) {
            @SuppressWarnings("hiding")
            Feature feature = (Feature)gemarkung.state();
            
            newFormField( gemarkung.gemeinde.info().getNameInStore() )
                    .setFeature( feature ).setParent( section ).setLabel( "Gemeinde" ).create();
            
            newFormField( gemarkung.gemarkung.info().getNameInStore() )
                    .setFeature( feature ).setParent( section ).setLabel( "Gemarkung" ).create();
        }        
        return section;
    }


    protected Section createAbschnitteSection( Flurstueck flurstueck ) {
        Section section = newSection( "Abschnitte", false, null );
        try {
            Collection<Abschnitt> abschnitte = flurstueck.abschnitte();
            FeatureType schema = repo.getSchema( Abschnitt.class );

            // viewer
            FeatureTableViewer viewer = new FeatureTableViewer( (Composite)section.getClient(), SWT.NONE );
            //viewer.getTable().setLayoutData( new SimpleFormData( ) )
            viewer.setContent( new CollectionContentProvider( null ) );
            viewer.setInput( Iterables.transform( abschnitte, Entities.toStates( Feature.class ) ) );
            
            // columns
            PropertyDescriptor prop1 = schema.getDescriptor( "ALBANUA_NUTZUNG" );
            viewer.addColumn( new DefaultFeatureTableColumn( prop1 ).setHeader( "Nutzung" ));
            PropertyDescriptor prop2 = schema.getDescriptor( "ALBANUA_FLAECHE" );
            viewer.addColumn( new DefaultFeatureTableColumn( prop2 ).setHeader( "Fläche" ));
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( AlkisPlugin.ID, this, "", e );
            Label msg = new Label( (Composite)section.getClient(), SWT.None );
            msg.setText( "Fehler beim Ermitteln der Lagehinweise." );
        }
        return section;
    }


    protected Section createLagehinweiseSection( Flurstueck flurstueck ) {
        Section section = newSection( "Lagehinweise", false, null );
        try {
            Collection<Lagehinweis2> hinweise = flurstueck.lagehinweise();
            FeatureType schema = repo.getSchema( Lagehinweis2.class );

            // viewer
            FeatureTableViewer viewer = new FeatureTableViewer( (Composite)section.getClient(), SWT.NONE );
            viewer.setContent( new CollectionContentProvider( null ) );
            viewer.setInput( Iterables.transform( hinweise, Entities.toStates( Feature.class ) ) );
            
            // columns
            PropertyDescriptor prop = schema.getDescriptor( "ALBHINF_TEXT" );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hinweis" ));
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( AlkisPlugin.ID, this, "", e );
            Label msg = new Label( (Composite)section.getClient(), SWT.None );
            msg.setText( "Fehler beim Ermitteln der Lagehinweise." );
        }
        return section;
    }

}
