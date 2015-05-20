/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Panels;

import org.polymap.model2.engine.UnitOfWorkNested;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * Provides {@link UnitOfWork} propagation between parent and child panels.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UnitOfWorkHolder
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( UnitOfWorkHolder.class );

    public static enum Propagation {
        /**
         * Same {@link UnitOfWork} will be used if there is an already opened
         * {@link UnitOfWork} in the current context. If there is no existing
         * UnitOfWork this holder will create a new one for the panel.
         */
        REQUIRED,
        /**
         * A new {@link UnitOfWork} will always be created for this panel. In other
         * words the inner transaction may commit or rollback independently of the
         * outer transaction, i.e. the outer transaction will not be affected by the
         * inner transaction result: they will run in distinct physical transactions.
         */
        REQUIRES_NEW,
        /**
         * Creates a new {@link UnitOfWork} for this panel only. This is similar the
         * {@link #REQUIRES_NEW} except that the UnitOfWork is hidden from the child
         * panels so that {@link #REQUIRED} will create a new UnitOfWork and
         * {@link #MANDATORY} throws an exception. In other words this requests a
         * child panel to manage its own UnitOfWork.
         */
        REQUIRES_NEW_LOCAL,
        /**
         * A new {@link UnitOfWork#newUnitOfWork() nested} UnitOfWork will always be
         * created for this panel. The inner transaction may rollback independently
         * of the outer transaction. Committing the nested UnitOfWork writes down the
         * modifications to the parent without changing the underlying store.
         */
        NESTED,
        /**
         * States that an existing opened {@link UnitOfWork} must already exist. If
         * not an exception will be thrown.
         */
        MANDATORY
    }
    
    public static enum Completion {
        /**
         * {@link UnitOfWork#commit() Commit} and {@link UnitOfWork#close() close}
         * the {@link UnitOfWork} of this panel.
         */
        COMMIT,
        /**
         * {@link UnitOfWork#rollback() Revert} all modification and
         * {@link UnitOfWork#close() close} the {@link UnitOfWork} of this panel.
         */
        REVERT
    }
    
    private Propagation                 propagation;

    private Context<UnitOfWork>         rootUow;
    
    private UnitOfWork                  uow;

    
    protected abstract UnitOfWork newRootUnitOfWork();
    
    
    protected UnitOfWork newUnitOfWork( Propagation _propagation ) {
        assert uow == null : "Current UnitOfWork has not yet been completed.";
        
        // rootUow
        if (rootUow.get() == null) {
            rootUow.set( newRootUnitOfWork() );
            assert !(rootUow.get() instanceof UnitOfWorkNested);
        }
        
        this.propagation = _propagation;
        //
        if (propagation == Propagation.REQUIRES_NEW
                || propagation == Propagation.REQUIRES_NEW_LOCAL) {
            uow = newRootUnitOfWork();
        }
        //
        else if (propagation == Propagation.MANDATORY) {
            UnitOfWorkHolder parent = parentPanel().orElseThrow( () -> new IllegalStateException( "MANDATORY: this is the Start panel, no UnitOfWork." ) );
            UnitOfWork parentUow = parent.uow().orElseThrow( () -> new IllegalStateException( "MANDATORY: parent panel has no UnitOfWork started." ) );
            if (parent.propagation == Propagation.REQUIRES_NEW_LOCAL) {
                new IllegalStateException( "MANDATORY: parent UnitOfWork is REQUIRES_NEW_LOCAL" );
            }
            uow = parentUow;
        }
        //
        else if (propagation == Propagation.NESTED) {
            UnitOfWorkHolder parent = parentPanel().orElseThrow( () -> new IllegalStateException( "NESTED: this is the Start panel, no UnitOfWork." ) );
            UnitOfWork parentUow = parent.uow().orElseThrow( () -> new IllegalStateException( "NESTED: parent panel has no UnitOfWork started." ) );
            if (parent.propagation == Propagation.REQUIRES_NEW_LOCAL) {
                new IllegalStateException( "NESTED: parent UnitOfWork is REQUIRES_NEW_LOCAL" );
            }
            uow = parentUow.newUnitOfWork();
        }
        // fail fast
        else {
            throw new IllegalStateException( "Unhandled propagation type: " + propagation );
        }
        return uow;
    }

    
    protected Optional<UnitOfWorkHolder> parentPanel() {
        PanelPath myPath = getSite().getPath();
        UnitOfWorkHolder result = myPath.size() > 0
                ? (UnitOfWorkHolder)getOnlyElement( getContext().findPanels( Panels.is( myPath.removeLast( 1 ) ) ) )
                : null;
        return Optional.ofNullable( result );
    }

    
    /**
     * The current {@link UnitOfWork} for this panel.
     * 
     * @return The current {@link UnitOfWork} for this panel, or null.
     */
    public Optional<UnitOfWork> uow() {
        return Optional.ofNullable( uow );
    }


    protected void closeUnitOfWork( Completion completion ) {
        assert uow != null : "No UnitOfWork has been started for this panel.";
        
        if (propagation == Propagation.MANDATORY) {
            throw new IllegalStateException( "MANDATORY: closing parent UnitOfWork is not permitted." );
        }
        
        if (propagation == Propagation.REQUIRED) {
        }
        
        
        
        if (completion == Completion.COMMIT) {
            uow.commit();
        }
        else if (completion == Completion.REVERT) {
            uow.rollback();
        }
        else {
            throw new IllegalStateException( "Unhandled completion type: " + completion );
        }
        uow.close();
        uow = null;
        propagation = null;
    }
    
}
