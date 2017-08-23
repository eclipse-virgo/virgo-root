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

import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;

/**
 * Ensures that a bundle has not been unregistered before method execution
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect ValidServiceRegistration {

    private pointcut ensureNotUninstalledMethod(StubServiceRegistration registration) : this(registration) && (
        execution(* org.eclipse.virgo.test.stubs.framework.StubServiceRegistration.getReference()) || 
        execution(* org.eclipse.virgo.test.stubs.framework.StubServiceRegistration.unregister())
    );

    /**
     * Ensures that a {@link ServiceRegistration} is not uninstalled before executing the method
     * 
     * @param registration The {@link ServiceRegistration} to check
     * @throws IllegalStateException if the {@link ServiceRegistration} has been unregistered
     */
    before(StubServiceRegistration registration) : ensureNotUninstalledMethod(registration) {
        if (registration.getUnregistered()) {
            throw new IllegalStateException("This ServiceRegistration has been unregistered.");
        }
    }
}
