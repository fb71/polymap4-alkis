/* 
 * polymap.org
 * Copyright 2011, Falko BRäutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.alkis.internal.flurliste;

import java.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.hydrologis.jgrass.csv2shape.importwizard.CsvImporter;
import eu.hydrologis.jgrass.csv2shape.importwizard.CsvOperation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.polymap.alkis.geocoder.Flurstueck;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class FlurlisteCsvOperation
        implements CsvOperation {

    private static final Log  log = LogFactory.getLog( FlurlisteCsvOperation.class );

    public static final String[] PROP_Y = { "hw", "hoch", "hochwert", "y" };
    public static final String[] PROP_X = { "rw", "rechts", "rechtswert", "x" };
    public static final String[] PROP_ZAEHLER = { "zaehler", "flurstücksnummer" };
    public static final String[] PROP_NENNER = { "nenner", "flurstücks-unternummer" };
    public static final String[] PROP_FLUR = { "flur" };


    public FlurlisteCsvOperation() {
    }

    public void perform( CsvImporter importer )
            throws Exception {
        log.info( "perform(): ..." );
        ByteArrayOutputStream logmsg = new ByteArrayOutputStream();
        PrintStream logOut = new PrintStream( logmsg );

        // find flur schluessel fields
        String[] header = importer.getHeader();
        if (header == null) {
            throw new Exception( "Es sind noch keine Feldnamen festgelegt. Benutzen Sie die automatische Zuordnung aus dem CSV-Header oder setzen sie diese von Hand." );
        }
        logOut.println( "Kopfzeile" );
        logOut.println( "----------------------------------------" );
        logOut.println( "Kopfzeile: " + Arrays.asList( header ) );

        int flurField = -1;
        int zaehlerField = -1;
        int nennerField = -1;
        int xField = -1;
        int yField = -1;
        
        for (int i=0; i<header.length; i++) {
            if (ArrayUtils.contains( PROP_FLUR, header[i].toLowerCase() )) {
                flurField = i;
                logOut.println( "    Feld: " + header[i] + ", Position: " + i );
            }
            else if (ArrayUtils.contains( PROP_ZAEHLER, header[i].toLowerCase() )) {
                zaehlerField = i;
                logOut.println( "    Feld: " + header[i] + ", Position: " + i );
            }
            else if (ArrayUtils.contains( PROP_NENNER, header[i].toLowerCase() )) {
                nennerField = i;
                logOut.println( "    Feld: " + header[i] + ", Position: " + i );
            }
            else if (ArrayUtils.contains( PROP_X, header[i].toLowerCase() )) {
                xField = i;
                logOut.println( "    Feld: " + header[i] + ", Position: " + i );
            }
            else if (ArrayUtils.contains( PROP_Y, header[i].toLowerCase() )) {
                yField = i;
                logOut.println( "    Feld: " + header[i] + ", Position: " + i );
            }
        }
        
        // 
        logOut.println( "Daten" );
        logOut.println( "----------------------------------------" );
        
        for (String[] values : importer.getLines()) {
            String code = values[0];
            
            String rwCode = code.substring( 0, 2 ) + code.substring( 4, 6 ) + code.substring( 8, 14 );
            float rw = Float.parseFloat( rwCode ) / 1000;
            String hwCode = code.substring( 2, 4 ) + code.substring( 6, 8 ) + code.substring( 14, 20 );
            float hw = Float.parseFloat( hwCode ) / 1000;
            
            //log.info( code + " -> " + rw + " / " + hw );
            
            values[xField] = importer.getNumberFormat().format( rw );
            values[yField] = importer.getNumberFormat().format( hw );
            
            String old = values[nennerField];
            values[nennerField] = Flurstueck.normalize( values[nennerField] );
            if (!old.equals( values[nennerField] )) {
                log.info( "Nenner: " + old + " -> " + values[nennerField] );
            }
            old = values[zaehlerField];
            values[zaehlerField] = Flurstueck.normalize( values[zaehlerField] );
            if (!old.equals( values[zaehlerField] )) {
                log.info( "Zaehler: " + old + " -> " + values[zaehlerField] );
            }
        }
        logOut.flush();
        
        // result dialog
        Display display = Polymap.getSessionDisplay();
        ResultDialog dialog = new ResultDialog( display.getActiveShell(), logmsg.toString() );
        dialog.open();
    }
    
    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class ResultDialog
            extends Dialog {

        private Text            text;

        private String          msg;
        
        
        protected ResultDialog( Shell parentShell, String msg ) {
            super( parentShell );
            this.msg = msg;
        }

        protected boolean isResizable() {
            return true;
        }

        protected void configureShell( Shell shell ) {
            super.configureShell( shell );
            shell.setText( "Ergebnisse der Addresssuche" );
            shell.setSize( 800, 600 );

            Rectangle parentSize = getParentShell().getBounds();
            Rectangle mySize = shell.getBounds();
            int locationX, locationY;
            locationX = (parentSize.width - mySize.width)/2+parentSize.x;
            locationY = (parentSize.height - mySize.height)/2+parentSize.y;
            shell.setLocation( new org.eclipse.swt.graphics.Point( locationX, locationY ) );
        }

        protected Control createDialogArea( Composite parent ) {
            Composite composite = (Composite)super.createDialogArea( parent );
            
            Text l = new Text( composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP );
            l.setText( "Nachfolgend sehen sie das Protokoll der Adresssuche. Jeweils 3 Zeilen bezeichnen einen Datensatz. " +
                    "Leere Zeilen ([]) konnten nicht zugeordnet werden. Nach verlassen des Dialoges sind die Koordinaten für die einzelnen Datensätze gesetzt und werden beim Import verwendet. " +
                    "Datensätze ohne Adresszuordnung werden ignoriert und nicht importiert." );
            l.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
            
//            ScrolledComposite scroll = new ScrolledComposite( composite, SWT.V_SCROLL | SWT.BORDER );
//            scroll.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
            
            text = new Text( composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL );
            text.setText( msg );
            text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));

//            applyDialogFont( composite );
            return composite;
        }
        
        protected void createButtonsForButtonBar( Composite parent ) {
            createButton( parent, IDialogConstants.OK_ID,
                    IDialogConstants.get().OK_LABEL, false );
//            //do this here because setting the text will set enablement on the ok
//            // button
//            text.setFocus();
//            if (value != null) {
//                text.setText(value);
//                text.selectAll();
//            }
        }
    }

}
