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

package org.eclipse.virgo.kernel.shell.internal;

import org.osgi.framework.BundleContext;

/**
 * A <code>CommandProviderResolver<code> the resolves command providers by finding them in the OSGi service registry.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 * 
 */
public final class ServiceRegistryCommandProviderResolver implements CommandProviderResolver {

    private final BundleContext bundleContext;

    /**
     * @param bundleContext
     */
    public ServiceRegistryCommandProviderResolver(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public Object getCommandProvider(String command) {
        return ServiceUtils.getService(this.bundleContext, Object.class, CommandProcessor.COMMAND_FUNCTION, command);
    }
}
