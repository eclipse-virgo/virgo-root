/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.test.stubs.framework.aspects;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.test.stubs.framework.StubBundle;

/**
 * Ensures that a bundle has not been uninstalled before method execution
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect BundleValid {

    private pointcut ensureNotUninstalledMethod(StubBundle bundle) : this(bundle) && (
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getEntry(String)) || 
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getEntryPaths(String)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getRegisteredServices()) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getResource(String)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getResources(String)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.getServicesInUse()) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.hasPermission(Object)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.loadClass(String)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.start(int)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.stop(int)) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.uninstall()) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.update(..))
    );

    /**
     * Ensures that a {@link Bundle} has not been uninstalled before executing a message
     * 
     * @param bundle The {@link Bundle} to check
     * @throws IllegalStateException if the {@link Bundle} has been uninstalled
     */
    before(StubBundle bundle) : ensureNotUninstalledMethod(bundle) {
        if (bundle.getState() == Bundle.UNINSTALLED) {
            throw new IllegalStateException("This bundle has been uninstalled");
        }
    }
}
