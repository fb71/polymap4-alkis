/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.alkis.importer.nas;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NasImportPage
        extends WizardPage
        implements IWizardPage, UploadListener {

    private static Log log = LogFactory.getLog( NasImportPage.class );

    public static final String          ID = "NasImportPage";

    private Upload                      upload;

    private List                        filesList;
    
    ArrayList<UploadItem>               files = new ArrayList();


    protected NasImportPage() {
        super( ID );
        setTitle( "Import-Dateien auswählen." );
        setDescription( "Wählen Sie eine *.gml und eine *.xsd Datei für den Import aus.");
    }


    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        fileSelectionArea.setLayout( layout );

        upload = new Upload( fileSelectionArea, SWT.BORDER, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
        upload.setBrowseButtonText( "Datei" );
        upload.setUploadButtonText( "Laden" );
        upload.addUploadListener( this );
        upload.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );

        filesList = new List( fileSelectionArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
        filesList.setLayoutData( FormDataFactory.filled().top( upload ).create() );
        filesList.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
            }
        });

        setControl( fileSelectionArea );
        checkFinish();
    }


    protected void checkFinish() {
        setPageComplete( !files.isEmpty() );
        getWizard().getContainer().updateButtons();
    }


    // UploadListener *************************************

    public void uploadInProgress( UploadEvent ev ) {
    }

    public void uploadFinished( UploadEvent ev ) {
        try {
            UploadItem item = upload.getUploadItem();
            log.info( "Uploaded: " + item.getFileName() + ", path=" + item.getFilePath() );
            files.add( item );

            Iterable<String> labels = Iterables.transform( files, new Function<UploadItem,String>() {
                public String apply( UploadItem input ) {
                    return input.getFileName();
                }
            });
            filesList.setItems( Iterables.toArray( labels, String.class ) );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, NasImportPage.this, "Fehler beim Upload der Daten.", e );
        }
        checkFinish();
    }

}
