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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.Feature;

import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormPageProvider;
import org.polymap.rhei.form.workbench.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FlurstueckFormPageProvider
        implements IFormPageProvider {

    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        List<IFormEditorPage> result = new ArrayList();
        if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "ALBFLU" )) {
            result.add( new FlurstueckFormPage( feature, formEditor.getFeatureStore() ) );
        }
        return result;
    }
    
}
