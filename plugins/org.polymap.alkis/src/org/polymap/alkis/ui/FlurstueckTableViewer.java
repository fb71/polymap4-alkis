/*
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.table.FeatureTableViewer;
import org.polymap.rhei.table.FormFeatureTableColumn;
import org.polymap.rhei.table.IFeatureTableElement;

import org.polymap.alkis.model.AX_Flurstueck;
import org.polymap.alkis.model.AX_Flurstuecksnummer;
import org.polymap.alkis.model.AX_Gemarkung_Schluessel;
import org.polymap.alkis.ui.util.CompositesFeatureContentProvider;
import org.polymap.alkis.ui.util.CompositesFeatureContentProvider.FeatureTableElement;
import org.polymap.alkis.ui.util.PropertyAdapter;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class FlurstueckTableViewer
        extends FeatureTableViewer {

    private static Log                  log  = LogFactory.getLog( FlurstueckTableViewer.class );

    private static final FastDateFormat df   = FastDateFormat.getInstance( "dd.MM.yyyy" );

    private UnitOfWork                  uow;
    

    public FlurstueckTableViewer( UnitOfWork uow, Composite parent, Iterable<AX_Flurstueck> rs ) {
        super( parent, /* SWT.VIRTUAL | SWT.V_SCROLL | */SWT.FULL_SELECTION );
        this.uow = uow;
        try {
            // Gemarkung
            String propName = AX_Flurstueck.TYPE.gemarkung.info().getName();
            final ColumnLabelProvider lp[] = new ColumnLabelProvider[1];
            addColumn( new FormFeatureTableColumn( PropertyAdapter.descriptorFor( propName, String.class ) )
                .setWeight( 3, 80 )
                .setLabelProvider( lp[0] = new ColumnLabelProvider() {
                    @Override
                    public String getText( Object elm ) {
                        AX_Flurstueck entity = FeatureTableElement.entity( elm );
                        AX_Gemarkung_Schluessel gmk = entity.gemarkung.get();
                        return gmk != null ? gmk.label() : "(kein Gemarkung)";
                    }
                    @Override
                    public String getToolTipText( Object elm ) {
                        return getText( elm );
                    }
                })
                .setSortable( new Comparator<IFeatureTableElement>() {
                    public int compare( IFeatureTableElement e1, IFeatureTableElement e2 ) {
                        String l1 = lp[0].getText( e1 );
                        String l2 = lp[0].getText( e2 );
                        return l1.compareTo( l2 );
                    }
                }))
                .sort( SWT.DOWN );
            
            // Flurst�cksnummer
            propName = AX_Flurstueck.TYPE.flurstuecksnummer.info().getName();
            addColumn( new FormFeatureTableColumn( PropertyAdapter.descriptorFor( propName, String.class ) )
                .setWeight( 1, 60 )
                .setHeader( "Nummer" )
                .setLabelProvider( new ColumnLabelProvider() {
                    @Override
                    public String getText( Object elm ) {
                        AX_Flurstueck fst = FeatureTableElement.entity( elm );
                        return fst.flurstuecksnummer.get().anzeige();
                    }
                    @Override
                    public String getToolTipText( Object elm ) {
                        return getText( elm );
                    }
                }));
            
            // Fl�che
            NumberValidator flaecheValidator = new NumberValidator( Double.class, Locale.GERMANY, 10, 4, 1, 4 );
            addColumn( new FormFeatureTableColumn( PropertyAdapter.descriptorFor( AX_Flurstueck.TYPE.amtlicheFlaeche ) )
                .setWeight( 1, 60 )
                .setHeader( "Fläche\n(in ha)" )
                .setLabelProvider( flaecheValidator )
                .setEditing( new StringFormField(), flaecheValidator )
                .setSortable( false ) );  // standard comparator: ClassCastException wenn null
            
            // suppress deferred loading to fix "empty table" issue
            // setContent( fs.getFeatures( this.baseFilter ) );
            setContent( new CompositesFeatureContentProvider( rs ) );
            setInput( rs );

//            /* Register for property change events */
//            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
//                public boolean apply( PropertyChangeEvent input ) {
//                    return input.getSource() instanceof Flurstueck;
//                }
//            });
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


//    @EventHandler(display=true, delay=1000, scope=Event.Scope.JVM)
//    protected void entityChanged( List<PropertyChangeEvent> ev ) {
//        if (!getControl().isDisposed()) {
//            refresh( true );
//        }
//    }


    public List<AX_Flurstueck> getSelected() {
        return copyOf( transform( asList( getSelectedElements() ), new Function<IFeatureTableElement,AX_Flurstueck>() {
            public AX_Flurstueck apply( IFeatureTableElement input ) {
                return (AX_Flurstueck)((FeatureTableElement)input).getComposite();
            }
        }));
    }

}
