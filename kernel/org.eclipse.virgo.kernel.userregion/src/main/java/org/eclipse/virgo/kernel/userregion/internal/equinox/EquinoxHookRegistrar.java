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

import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;

import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableBundleFileWrapperFactoryHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableClassLoadingHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableDelegatingClassLoaderDelegateHook;

/**
 * <code>EquinoxHookRegistrar</code> is responsible for registering
 * the kernel's hooks with Equinox.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class EquinoxHookRegistrar  {

    private final TransformedManifestProvidingBundleFileWrapper bundleFileWrapper;
    
    private final ClassLoaderDelegateHook metaInfResourceClassLoaderDelegateHook;

    public EquinoxHookRegistrar(TransformedManifestProvidingBundleFileWrapper bundleFileWrapper, ClassLoaderDelegateHook metaInfResourceClassLoaderDelegateHook) {
        this.bundleFileWrapper = bundleFileWrapper;
        this.metaInfResourceClassLoaderDelegateHook = metaInfResourceClassLoaderDelegateHook;
    }

    public void init() {
        PluggableClassLoadingHook.getInstance().setClassLoaderCreator(new KernelClassLoaderCreator());
        PluggableBundleFileWrapperFactoryHook.getInstance().setBundleFileWrapper(this.bundleFileWrapper);
        PluggableDelegatingClassLoaderDelegateHook.getInstance().addDelegate(this.metaInfResourceClassLoaderDelegateHook);
    }
    
    public void destroy() {
        PluggableClassLoadingHook.getInstance().setClassLoaderCreator(null);
        PluggableBundleFileWrapperFactoryHook.getInstance().setBundleFileWrapper(null);
        PluggableDelegatingClassLoaderDelegateHook.getInstance().removeDelegate(this.metaInfResourceClassLoaderDelegateHook);
    }
}
