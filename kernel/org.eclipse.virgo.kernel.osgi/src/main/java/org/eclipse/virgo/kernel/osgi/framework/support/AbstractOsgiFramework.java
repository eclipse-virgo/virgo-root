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

import static java.util.Arrays.asList;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext bundleContext;


    protected AbstractOsgiFramework(BundleContext context) {
        this.bundleContext = context;
    }

    // http://git.eclipse.org/c/equinox/rt.equinox.framework.git/tree/bundles/org.eclipse.osgi/container/src/org/eclipse/osgi/internal/framework/legacy/PackageAdminImpl.java#n148
    public Bundle getClassBundle(Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            return getBundlePriv(clazz);
        }
        throw new IllegalStateException("Running with SecurityManager is currently not supported.");
        // TODO - think about getting this code from PackageAdmin also
        // See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=344276 - As user I want to start virgo with security manager
        //        return AccessController.doPrivileged(new GetBundleAction(this, clazz));
    }

    private Bundle getBundlePriv(Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof BundleReference) {
            return ((BundleReference) cl).getBundle();
        }
        if (cl == getClass().getClassLoader()) {
            return bundleContext.getBundle(0);
        }
        return null;
    }

    protected void refreshPackages(Bundle[] toRefresh) {
        FrameworkWiring frameworkWiring = bundleContext.getBundle(0).adapt(FrameworkWiring.class);
        frameworkWiring.refreshBundles(asList(toRefresh));
    }

    /**
     * {@inheritDoc}
     */
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }
}
