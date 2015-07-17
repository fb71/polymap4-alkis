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
package org.polymap.alkis.ui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.tx.TxProvider;

import org.polymap.alkis.model.AlkisRepository;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AlkisUowProvider
        extends TxProvider<UnitOfWork> {

    private static Log log = LogFactory.getLog( AlkisUowProvider.class );
    
    private AlkisRepository         repo;
    
    
    public AlkisUowProvider() {
        repo = AlkisRepository.instance.get();
    }
    
    @Override
    protected UnitOfWork newTx( UnitOfWork parent ) {
        return parent != null ? parent.newUnitOfWork() : repo.newUnitOfWork();
    }

    @Override
    protected void commitTx( UnitOfWork uow ) {
        uow.commit();
    }

    @Override
    protected void rollbackTx( UnitOfWork uow ) {
        uow.rollback();
    }

    @Override
    protected void closeTx( UnitOfWork uow ) {
        uow.close();
    }

}
