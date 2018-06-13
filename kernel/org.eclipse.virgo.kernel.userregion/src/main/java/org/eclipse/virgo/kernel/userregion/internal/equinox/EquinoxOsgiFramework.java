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

import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.virgo.kernel.osgi.framework.ManifestTransformer;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.framework.support.AbstractOsgiFramework;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * Implementation of <code>OsgiFramework</code> using Equinox.
 * 
 */
public class EquinoxOsgiFramework extends AbstractOsgiFramework {
    
    private static final String FILE_SCHEME = "file:";
    
    private static final String REFERENCE_SCHEME = "reference:";

    private final OsgiServiceHolder<PlatformAdmin> platformAdmin;

    private final EquinoxBootDelegationHelper bootDelegationHelper;
    
    private final TransformedManifestProvidingBundleFileWrapper bundleTransformationHandler;


    /**
     * Creates a new <code>EquinoxOsgiFramework</code>.
     * @param context execution context of bundle
     * @param bundleTransformationHandler wrapper for bundle manifest transformations
     */
    public EquinoxOsgiFramework(BundleContext context, TransformedManifestProvidingBundleFileWrapper bundleTransformationHandler) {
        super(context);
        this.bootDelegationHelper = new EquinoxBootDelegationHelper(context.getProperty(FRAMEWORK_BOOTDELEGATION));
        this.platformAdmin = OsgiFrameworkUtils.getService(context, PlatformAdmin.class);
        this.bundleTransformationHandler = bundleTransformationHandler;
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(Bundle bundle) throws BundleException {
        ClassLoader cl = getBundleClassLoader(bundle);
        List<Bundle> refreshBundles = new ArrayList<>();
        if (cl instanceof KernelBundleClassLoader) {
            KernelBundleClassLoader pbcl = (KernelBundleClassLoader) cl;
            if (pbcl.isInstrumented()) {
                Bundle[] dependencies = getDirectDependencies(bundle);
                for (Bundle dependency : dependencies) {
                    if (OsgiFrameworkUtils.sameScope(bundle, dependency)) {
                        dependency.update();
                        refreshBundles.add(dependency);
                    }
                }
            }
        }
        bundle.update();
        refreshBundles.add(bundle);
        Bundle[] toRefresh = refreshBundles.toArray(new Bundle[0]);
        refreshPackages(toRefresh);
    }

    final void stop() {
        if (this.platformAdmin != null) {
            getBundleContext().ungetService(this.platformAdmin.getServiceReference());
        }
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getBundleClassLoader(Bundle bundle) {
        return EquinoxUtils.getBundleClassLoader(bundle);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBootDelegated(String className) {
        if (this.bootDelegationHelper != null) {
            return this.bootDelegationHelper.isBootDelegated(className);
        }

        throw new IllegalStateException("OsgiFramework must have been started prior to querying its boot delegation");
    }

    /**
     * {@inheritDoc}
     */
    public Bundle[] getDirectDependencies(Bundle bundle, boolean includeFragments) {
        BundleContext bundleContext = getBundleContext();
        ServiceReference<PlatformAdmin> serviceRef = bundleContext.getServiceReference(PlatformAdmin.class);
        try {
            PlatformAdmin serverAdmin = bundleContext.getService(serviceRef);
            return EquinoxUtils.getDirectDependencies(bundle, bundleContext, serverAdmin, includeFragments);
        } finally {
            bundleContext.ungetService(serviceRef);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Bundle[] getDirectDependencies(Bundle bundle) {
        return getDirectDependencies(bundle, false);
    }

    /** 
     * {@inheritDoc}
     */
    public void update(Bundle bundle, ManifestTransformer manifestTransformer, File location) throws BundleException {
        this.bundleTransformationHandler.pushManifestTransformer(manifestTransformer);
        try {
            bundle.update(openBundleStream(location));
        } finally {
            this.bundleTransformationHandler.popManifestTransformer();
        }
    }
    
    private InputStream openBundleStream(File location) throws BundleException {
        String absoluteBundleUriString = getAbsoluteUriString(location);

        try {
            // Use the reference: scheme to obtain an InputStream for either a file or a directory.
            return new URL(REFERENCE_SCHEME + absoluteBundleUriString).openStream();

        } catch (MalformedURLException e) {
            throw new BundleException("Invalid bundle URI '" + absoluteBundleUriString + "'", e);
        } catch (IOException e) {
            throw new BundleException("Invalid bundle at URI '" + absoluteBundleUriString + "'", e);
        }
    }

    private String getAbsoluteUriString(File location) {
        return FILE_SCHEME + location.getAbsolutePath();
    }
}
