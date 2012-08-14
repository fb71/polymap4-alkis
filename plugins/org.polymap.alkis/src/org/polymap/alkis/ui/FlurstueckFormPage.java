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

import org.polymap.core.data.ui.featuretable.CollectionContentProvider;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model2.runtime.Entities;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.alkis.AlkisPlugin;
import org.polymap.alkis.model.alb.ALBRepository;
import org.polymap.alkis.model.alb.Flurstueck;
import org.polymap.alkis.model.alb.Lagehinweis2;

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
        asection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( gsection ).create() );

        Section lsection = createLagehinweiseSection( flurstueck );
        lsection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( fsection ).create() );
    }
    
    
    protected Section createFlurstueckSection( Flurstueck flurstueck ) {
        Section section1 = newSection( "Basisdaten", false, null );
        
        newFormField( flurstueck.zaehler.getInfo().getNameInStore() )
                .setParent( section1 ).setLabel( "Zähler" ).create();
        
        newFormField( flurstueck.nenner.getInfo().getNameInStore() )
                .setParent( section1 ).setLabel( "Nenner" ).create();
        
        newFormField( flurstueck.lagehinweis.getInfo().getNameInStore() )
                .setParent( section1 ).setLabel( "Straße" ).create();
        return section1;
    }


    protected Section createGemarkungSection( Flurstueck flurstueck ) {
        Section section = newSection( "Gemarkung", false, null );
        
        return section;
    }


    protected Section createAbschnitteSection( Flurstueck flurstueck ) {
        Section section = newSection( "Abschnitte", false, null );
        
        //log.info( "Abschnitte: " + flurstueck.lagehinweise() )
        return section;
    }


    protected Section createLagehinweiseSection( Flurstueck flurstueck ) {
        Section section = newSection( "Lagehinweise", false, null );
        try {
            Collection<Lagehinweis2> hinweise = flurstueck.lagehinweise();
            log.info( "Lagehinweise: " + hinweise.size() );
            FeatureType schema = repo.getSchema( Lagehinweis2.class );

            // viewer
            FeatureTableViewer viewer = new FeatureTableViewer( (Composite)section.getClient(), SWT.NONE );
            viewer.setContent( new CollectionContentProvider( null, schema ) );
            viewer.setInput( Iterables.transform( hinweise, Entities.toStates( Feature.class ) ) );
            
            // columns
            PropertyDescriptor prop = schema.getDescriptor( "ALBHINF_TEXT" );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hinweis" ));
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( AlkisPlugin.PLUGIN_ID, this, "", e );
            Label msg = new Label( (Composite)section.getClient(), SWT.None );
            msg.setText( "Fehler beim Ermitteln der Lagehinweise." );
        }
        return section;
    }

}
