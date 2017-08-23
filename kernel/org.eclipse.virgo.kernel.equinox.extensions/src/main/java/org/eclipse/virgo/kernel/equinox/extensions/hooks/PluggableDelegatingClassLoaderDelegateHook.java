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
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;


/**
 * A pluggable {@link ClassLoaderDelegateHook} into which one or more <code>ClassLoaderDelegateHook</code>
 * can be plugged.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class PluggableDelegatingClassLoaderDelegateHook implements ClassLoaderDelegateHook {
    
    private final List<ClassLoaderDelegateHook> delegates = new CopyOnWriteArrayList<ClassLoaderDelegateHook>();
    
    private static final PluggableDelegatingClassLoaderDelegateHook INSTANCE = new PluggableDelegatingClassLoaderDelegateHook();
    
    private PluggableDelegatingClassLoaderDelegateHook() {
    }
    
    public static PluggableDelegatingClassLoaderDelegateHook getInstance() {
        return INSTANCE;
    }
    
    public void addDelegate(ClassLoaderDelegateHook delegate) {
        this.delegates.add(delegate);
    }
    
    public void removeDelegate(ClassLoaderDelegateHook delegate) {
        this.delegates.remove(delegate);
    }
    
    public Class<?> postFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            Class<?> clazz = delegate.postFindClass(name, classLoader, data);    
            if (clazz != null) {
                return clazz;
            } 
        }
        return null;
    }

    public String postFindLibrary(String name, BundleClassLoader classLoader, BundleData data) {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            String library = delegate.postFindLibrary(name, classLoader, data);    
            if (library != null) {
                return library;
            }        
        }
        return null;
    }

    public URL postFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            URL resource = delegate.postFindResource(name, classLoader, data);    
            if (resource != null) {
                return resource;
            }        
        }
        return null;
    }

    public Enumeration<URL> postFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            Enumeration<URL> resources = delegate.postFindResources(name, classLoader, data);    
            if (resources != null) {
                return resources;
            }        
        }
        return null;
    }

    public Class<?> preFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            Class<?> clazz = delegate.preFindClass(name, classLoader, data);    
            if (clazz != null) {
                return clazz;
            }        
        }
        return null;
    }

    public String preFindLibrary(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            String library = delegate.preFindLibrary(name, classLoader, data);    
            if (library != null) {
                return library;
            }        
        }
        return null;
    }

    public URL preFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            URL resource = delegate.preFindResource(name, classLoader, data);    
            if (resource != null) {
                return resource;
            }        
        }
        return null;
    }

    public Enumeration<URL> preFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        for (ClassLoaderDelegateHook delegate : this.delegates) {
            Enumeration<URL> resources = delegate.preFindResources(name, classLoader, data);    
            if (resources != null) {
                return resources;
            }        
        }
        return null;
    }
    
    
}
