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
package org.polymap.alkis.model.alb;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@NameInStore(Gemarkung.TABLE_NAME)
public class Gemarkung
        extends Entity {
    
    public static final String      TABLE_NAME = "ALBGEMA";
    
}

//table="ALBGEMA"
//    dynamic-update="false"
//    mutable="false"
//    >
//    <cache usage="read-only" region="polymap2.fs_alb.hibernate" />
//    
//    <id
//       name="id"
//       column="ALBGEM_ID"
//       type="java.lang.String"
//       >
//       <generator class="assigned" />
//    </id>
//    
//    <property name="gemarkung"    type="java.lang.String" column="ALBGEM_GEMARKUNG" />
//    <property name="gemeinde"     type="java.lang.String" column="ALBGEM_GEMEINDE" />
//    <property name="nr"           type="java.lang.String" column="ALBGEM_NR" />
//    <property name="gericht"      type="java.lang.String" column="ALBGEM_GERICHT" />
//    <property name="gemeindeteil" type="java.lang.String" column="ALBGEM_GEMTEIL" />
