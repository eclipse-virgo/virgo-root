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

package org.eclipse.virgo.nano.shim.scope;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Creates {@link Scope} instances for {@link ServiceReference ServiceReferences} and for lookups. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ScopeFactory {

    /**
     * Get the {@link Scope} containing the given {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code> whose scope is required
     * @return the <code>Scope</code> containing the given bundle
     */
    Scope getBundleScope(Bundle bundle);

    /**
     * Get the global {@link Scope}.
     * 
     * @return the global <code>Scope</code>
     */
    Scope getGlobalScope();

    /**
     * Gets the {@link Scope} under which the referenced service is published.
     * @param ref service reference
     * @return the scope
     */
    Scope getServiceScope(ServiceReference ref);

    /**
     * Get the {@link Scope} for the given application scope name.
     * 
     * @param applicationScopeName the name of the scope
     * @return the {@link Scope} with the given name
     */
    Scope getApplicationScope(String applicationScopeName);

    /**
     * Destroy the given application {@link Scope}. Existing <code>Scope</code> instances equivalent to the given
     * <code>Scope</code> will continue to exist and share properties, but calls to get a <code>Scope</code> with the
     * same application scope name as the given <code>Scope</code> will produce a new <code>Scope</code> with a distinct
     * collection of properties. Effectively, this method delimits a 'generation' of an application scope.
     * 
     * @param applicationScope the application <code>Scope</code> to be destroyed.
     */
    void destroyApplicationScope(Scope applicationScope);

}
