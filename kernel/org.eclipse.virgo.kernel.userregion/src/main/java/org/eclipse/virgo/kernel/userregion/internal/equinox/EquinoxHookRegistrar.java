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

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.BundleFileClosingBundleFileWrapperFactoryHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableBundleFileWrapperFactoryHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableClassLoaderHook;
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
    
    private final ClassLoaderHook metaInfResourceClassLoaderDelegateHook;

    public EquinoxHookRegistrar(TransformedManifestProvidingBundleFileWrapper bundleFileWrapper, ClassLoaderHook metaInfResourceClassLoaderDelegateHook) {
        this.bundleFileWrapper = bundleFileWrapper;
        this.metaInfResourceClassLoaderDelegateHook = metaInfResourceClassLoaderDelegateHook;
    }

    public void init() {
        PluggableClassLoaderHook.getInstance().setClassLoaderCreator(new KernelClassLoaderCreator());
        PluggableBundleFileWrapperFactoryHook.getInstance().setBundleFileWrapper(this.bundleFileWrapper);
        PluggableDelegatingClassLoaderDelegateHook.getInstance().addDelegate(this.metaInfResourceClassLoaderDelegateHook);
    }
    
    public void destroy() throws Exception {
        PluggableClassLoaderHook.getInstance().setClassLoaderCreator(null);
        PluggableBundleFileWrapperFactoryHook.getInstance().setBundleFileWrapper(null);
        PluggableDelegatingClassLoaderDelegateHook.getInstance().removeDelegate(this.metaInfResourceClassLoaderDelegateHook);
        BundleFileClosingBundleFileWrapperFactoryHook.getInstance().cleanup();
    }
}
