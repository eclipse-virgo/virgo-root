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

package org.eclipse.virgo.kernel.dmfragment.internal;

import java.lang.instrument.ClassFileTransformer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;

import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;

/**
 * {@link LoadTimeWeaver} implementation that plugs into the {@link InstrumentableClassLoader
 * InstrumentableClassLoaders} created for all installed bundles.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class KernelLoadTimeWeaver implements LoadTimeWeaver, BeanClassLoaderAware {

    private volatile InstrumentableClassLoader instrumentableClassLoader;

    /**
     * {@inheritDoc}
     */
    public void addTransformer(ClassFileTransformer transformer) {
        this.instrumentableClassLoader.addClassFileTransformer(transformer);
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getInstrumentableClassLoader() {
        return (ClassLoader) this.instrumentableClassLoader;
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getThrowawayClassLoader() {
        return this.instrumentableClassLoader.createThrowAway();
    }

    /**
     * {@inheritDoc}
     */
    public void setBeanClassLoader(ClassLoader classLoader) {
        InstrumentableClassLoader instrumentableClassLoader = null;
        if (classLoader instanceof InstrumentableClassLoader) {
            instrumentableClassLoader = (InstrumentableClassLoader) classLoader;
        } else if (classLoader instanceof BundleDelegatingClassLoader) {
            Bundle bundle = ((BundleDelegatingClassLoader) classLoader).getBundle();
            ClassLoader bundleClassLoader = getBundleClassLoader(bundle);
            if (bundleClassLoader instanceof InstrumentableClassLoader) {
                instrumentableClassLoader = (InstrumentableClassLoader) bundleClassLoader;
            }
        }
        if (instrumentableClassLoader == null) {
            throw new IllegalStateException("ClassLoader '" + classLoader + "' is not instrumentable.");
        }
        this.instrumentableClassLoader = instrumentableClassLoader;
    }

    /**
     * Gets the {@link ClassLoader} for the supplied {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return the <code>Bundles</code> <code>ClassLoader</code>.
     */
    private ClassLoader getBundleClassLoader(Bundle bundle) {
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference<OsgiFramework> serviceReference = bundleContext.getServiceReference(OsgiFramework.class);
        try {
            OsgiFramework framework = bundleContext.getService(serviceReference);
            return framework.getBundleClassLoader(bundle);
        } finally {
            bundleContext.ungetService(serviceReference);
        }
    }

}
