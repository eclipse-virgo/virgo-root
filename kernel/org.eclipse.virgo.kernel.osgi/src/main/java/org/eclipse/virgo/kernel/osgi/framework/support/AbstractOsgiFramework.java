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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
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
@SuppressWarnings("deprecation")
public abstract class AbstractOsgiFramework implements OsgiFramework {

    public static final String DIRECTIVE_SEPARATOR = ";";

    public static final boolean DIRECTIVE_PUBLISH_CONTEXT_DEFAULT = true;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PackageAdmin packageAdmin;

    private final BundleContext bundleContext;


    protected AbstractOsgiFramework(BundleContext context, PackageAdmin packageAdmin) {
        this.bundleContext = context;
        this.packageAdmin = packageAdmin;
    }

    public Bundle getClassBundle(Class<?> cls) {
        if (this.packageAdmin != null) {
            return this.packageAdmin.getBundle(cls);
        } else {
            return null;
        }
    }
    
    /**
     * Gets the {@link PackageAdmin} service.
     * 
     * @return the <code>PackageAdmin</code> service.
     */
    protected final PackageAdmin getPackageAdmin() {
        return this.packageAdmin;
    }

    /**
     * {@inheritDoc}
     */
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }
}
