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

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.osgi.storage.bundlefile.BundleEntry;

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
public final class PluggableClassLoaderHook extends ClassLoaderHook {

    private static final PluggableClassLoaderHook INSTANCE = new PluggableClassLoaderHook();

    private final Object monitor = new Object();

    private volatile ClassLoaderCreator creator;

    private ClassLoader bundleClassLoaderParent;

    private PluggableClassLoaderHook() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries, String cp, ClasspathManager hostmanager, Generation sourceGeneration) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleClassLoader createClassLoader(ClassLoader parent, EquinoxConfiguration configuration, BundleLoader delegate, Generation generation) {
        if (this.creator != null) {
            return this.creator.createClassLoader(parent, configuration, delegate, generation);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    // TODO Is returning "null" the correct behavior?!
    @Override
    public String preFindLibrary(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    // TODO Is returning "null" the correct behavior?!
    @Override
    public String postFindLibrary(String name, ModuleClassLoader classLoader) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    // TODO check this method
//    public void initializedClassLoader(BaseClassLoader baseClassLoader, BaseData data) {
//    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public static PluggableClassLoaderHook getInstance() {
        return INSTANCE;
    }

    // TODO reduce visibility - unsed for testing only?!
    public ClassLoader getBundleClassLoaderParent() {
        synchronized (this.monitor) {
            return this.bundleClassLoaderParent;
        }
    }

    /**
     * Sets the class loader to be used as the parent of bundle class loaders.
     * 
     * @param bundleClassLoaderParent the class loader to be used
     */
    // TODO reduce visibility - unsed for testing only?!
    public void setBundleClassLoaderParent(ClassLoader bundleClassLoaderParent) {
        synchronized (this.monitor) {
            this.bundleClassLoaderParent = bundleClassLoaderParent;
        }
    }

}
