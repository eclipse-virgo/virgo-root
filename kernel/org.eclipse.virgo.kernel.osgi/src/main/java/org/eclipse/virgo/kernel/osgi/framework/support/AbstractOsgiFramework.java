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

package org.eclipse.virgo.kernel.osgi.framework.support;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.osgi.framework.BundleContext;


/**
 * Base implementation of {@link OsgiFramework}.
 * <p/>
 * 
 * Core start and stop logic is handled by this base implementation. In particular, the requirement to self publish as
 * services in the service registration under <code>BundleInstaller</code> and <code>OsgiFramework</code> is handled.
 * <p/>
 *  
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementation is thread safe.
 * 
 */
public abstract class AbstractOsgiFramework implements OsgiFramework {

    private final BundleContext bundleContext;


    protected AbstractOsgiFramework(BundleContext context) {
        this.bundleContext = context;
    }

    /**
     * {@inheritDoc}
     */
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }
}
