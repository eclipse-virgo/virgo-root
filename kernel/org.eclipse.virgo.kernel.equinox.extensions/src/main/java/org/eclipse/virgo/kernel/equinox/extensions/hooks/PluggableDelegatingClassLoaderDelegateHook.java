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

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;


/**
 * A pluggable {@link ClassLoaderHook} into which one or more <code>ClassLoaderHook</code>
 * can be plugged.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class PluggableDelegatingClassLoaderDelegateHook extends ClassLoaderHook {
    
    private final List<ClassLoaderHook> delegates = new CopyOnWriteArrayList<ClassLoaderHook>();
    
    private static final PluggableDelegatingClassLoaderDelegateHook INSTANCE = new PluggableDelegatingClassLoaderDelegateHook();
    
    private PluggableDelegatingClassLoaderDelegateHook() {
    }
    
    public static PluggableDelegatingClassLoaderDelegateHook getInstance() {
        return INSTANCE;
    }
    
    public void addDelegate(ClassLoaderHook delegate) {
        this.delegates.add(delegate);
    }
    
    public void removeDelegate(ClassLoaderHook delegate) {
        this.delegates.remove(delegate);
    }
    
    public Class<?> postFindClass(String name, ModuleClassLoader classLoader) throws ClassNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            Class<?> clazz = delegate.postFindClass(name, classLoader);    
            if (clazz != null) {
                return clazz;
            } 
        }
        return null;
    }

    public String postFindLibrary(String name, ModuleClassLoader classLoader) {
        for (ClassLoaderHook delegate : this.delegates) {
            String library = delegate.postFindLibrary(name, classLoader);    
            if (library != null) {
                return library;
            }        
        }
        return null;
    }

    public URL postFindResource(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            URL resource = delegate.postFindResource(name, classLoader);    
            if (resource != null) {
                return resource;
            }        
        }
        return null;
    }

    public Enumeration<URL> postFindResources(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            Enumeration<URL> resources = delegate.postFindResources(name, classLoader);    
            if (resources != null) {
                return resources;
            }        
        }
        return null;
    }

    public Class<?> preFindClass(String name, ModuleClassLoader classLoader) throws ClassNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            Class<?> clazz = delegate.preFindClass(name, classLoader);    
            if (clazz != null) {
                return clazz;
            }        
        }
        return null;
    }

    public String preFindLibrary(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            String library = delegate.preFindLibrary(name, classLoader);    
            if (library != null) {
                return library;
            }        
        }
        return null;
    }

    public URL preFindResource(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            URL resource = delegate.preFindResource(name, classLoader);    
            if (resource != null) {
                return resource;
            }        
        }
        return null;
    }

    public Enumeration<URL> preFindResources(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        for (ClassLoaderHook delegate : this.delegates) {
            Enumeration<URL> resources = delegate.preFindResources(name, classLoader);    
            if (resources != null) {
                return resources;
            }        
        }
        return null;
    }
    
    
}
