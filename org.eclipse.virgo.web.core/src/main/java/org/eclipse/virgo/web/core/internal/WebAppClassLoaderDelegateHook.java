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

package org.eclipse.virgo.web.core.internal;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;
import org.osgi.framework.Bundle;

import org.eclipse.gemini.web.core.WebApplication;


/**
 * A {@link ClassLoaderDelegateHook} that delegates requests to a web application's
 * class loader.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class WebAppClassLoaderDelegateHook implements ClassLoaderDelegateHook {
    
    private static final Object DELEGATION_IN_PROGRESS_MARKER = new Object();
    
    private final ConcurrentHashMap<Bundle, ClassLoader> webAppClassLoaders = new ConcurrentHashMap<Bundle, ClassLoader>();
    
    private final ThreadLocal<Object> delegationInProgress = new ThreadLocal<Object>();
    
    void addWebApplication(WebApplication webApplication, Bundle bundle) {
        this.webAppClassLoaders.put(bundle, webApplication.getClassLoader());
    }
    
    void removeWebApplication(Bundle bundle) {
        this.webAppClassLoaders.remove(bundle);
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?> postFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);
                
                ClassLoader webAppClassLoader = this.webAppClassLoaders.get(classLoader.getBundle());
                if (webAppClassLoader != null) {
                    try {
                        return webAppClassLoader.loadClass(name);
                    } catch (Throwable ignored) {       
                    }
                }
            } finally {
                this.delegationInProgress.set(null);
            }
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String postFindLibrary(String name, BundleClassLoader classLoader, BundleData data) {        
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public URL postFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {  
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);
                
                ClassLoader webAppClassLoader = this.webAppClassLoaders.get(classLoader.getBundle());
                if (webAppClassLoader != null) {
                    try {
                        return webAppClassLoader.getResource(name);
                    } catch (Throwable ignored) {       
                    }
                }                
            } finally {
                this.delegationInProgress.set(null);
            }
        }        
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public Enumeration<URL> postFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        if (this.delegationInProgress.get() == null) {
            try {
                this.delegationInProgress.set(DELEGATION_IN_PROGRESS_MARKER);
                
                ClassLoader webAppClassLoader = this.webAppClassLoaders.get(classLoader.getBundle());
                if (webAppClassLoader != null) {
                    try {
                        Enumeration<URL> resources = webAppClassLoader.getResources(name);
                        if (resources.hasMoreElements()) {
                            return resources;
                        }
                    } catch (Throwable ignored) {       
                    }
                }
            } finally {
                this.delegationInProgress.set(null);
            }
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?> preFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String preFindLibrary(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public URL preFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public Enumeration<URL> preFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

}
