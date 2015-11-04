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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.toolkit.md.MdAppDesign;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AlkisDesign
        extends MdAppDesign
        implements IAppDesign {

    private static Log log = LogFactory.getLog( AlkisDesign.class );

    @Override
    protected Composite fillHeaderArea( Composite parent ) {
        Composite result = UIUtils.setVariant( new Composite( parent, SWT.NO_FOCUS ), CSS_HEADER );
        result.setLayout( FormLayoutFactory.defaults().margins( 5, 0, 0, 0 ).create() );

        boolean showText = UIUtils.sessionDisplay().getClientArea().width > 900;

        Label l = UIUtils.setVariant( new Label( result, SWT.NONE ), CSS_HEADER );
        l.setText( showText ? "ALKIS-Auskunft" : "ALKIS" );

        return result;
    }

}
