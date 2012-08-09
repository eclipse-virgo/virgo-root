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

package org.eclipse.virgo.kernel.equinox.extensions.hooks;

import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;

/**
 * A {@link ClassLoadingHook} into which a {@link ClassLoaderCreator} can be plugged to provide a custom class loader
 * for a bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class PluggableClassLoadingHook implements ClassLoadingHook {

    private static final PluggableClassLoadingHook INSTANCE = new PluggableClassLoadingHook();

    private final Object monitor = new Object();

    private volatile ClassLoaderCreator creator;

    private ClassLoader bundleClassLoaderParent;

    private PluggableClassLoadingHook() {

    }

    /**
     * {@inheritDoc}
     */
    public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries, String cp, ClasspathManager hostmanager, BaseData sourcedata, ProtectionDomain sourcedomain) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public BaseClassLoader createClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, BundleProtectionDomain domain, BaseData data,
        String[] bundleclasspath) {
        if (this.creator != null) {
            return this.creator.createClassLoader(parent, delegate, domain, data, bundleclasspath);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String findLibrary(BaseData data, String libName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getBundleClassLoaderParent() {
        synchronized (this.monitor) {
            return this.bundleClassLoaderParent;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initializedClassLoader(BaseClassLoader baseClassLoader, BaseData data) {
    }

    /**
     * {@inheritDoc}
     */
    public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) {
        return null;
    }

    public void setClassLoaderCreator(ClassLoaderCreator creator) {
        synchronized (this.monitor) {
            this.creator = creator;
        }
    }

    /**
     * @return singleton
     */
    public static PluggableClassLoadingHook getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the class loader to be used as the parent of bundle class loaders.
     * 
     * @param bundleClassLoaderParent the class loader to be used
     */
    public void setBundleClassLoaderParent(ClassLoader bundleClassLoaderParent) {
        synchronized (this.monitor) {
            this.bundleClassLoaderParent = bundleClassLoaderParent;
        }
    }

}
