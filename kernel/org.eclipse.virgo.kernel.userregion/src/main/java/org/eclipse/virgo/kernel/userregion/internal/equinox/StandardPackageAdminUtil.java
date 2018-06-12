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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import java.util.Arrays;

import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.FrameworkWiring;

/**
 * {@link StandardPackageAdminUtil} is the implementation of {@link PackageAdminUtil}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 */
public final class StandardPackageAdminUtil implements PackageAdminUtil {

    private final BundleContext bundleContext;

    public StandardPackageAdminUtil(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public void synchronouslyRefreshPackages(Bundle[] bundles) {
        FrameworkWiring frameworkWiring = this.bundleContext.getBundle().adapt(FrameworkWiring.class);
        frameworkWiring.refreshBundles(Arrays.asList(bundles));
    }
}
