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

import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;

/**
 * Configures Equinox hooks with which its runtime behaviour is customised.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ExtensionsHookConfigurator implements HookConfigurator {        

    /** 
     * {@inheritDoc}
     */
    public void addHooks(HookRegistry hookRegistry) {
        hookRegistry.addClassLoaderHook(PluggableClassLoaderHook.getInstance());
        // TODO why isn't this a singleton, too?
        hookRegistry.addBundleFileWrapperFactoryHook(new ExtendedBundleFileWrapperFactoryHook());
        hookRegistry.addBundleFileWrapperFactoryHook(BundleFileClosingBundleFileWrapperFactoryHook.getInstance());
        hookRegistry.addBundleFileWrapperFactoryHook(PluggableBundleFileWrapperFactoryHook.getInstance());
        hookRegistry.addClassLoaderHook(PluggableDelegatingClassLoaderDelegateHook.getInstance());
    }
}
