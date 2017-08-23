/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.dmfragment.internal;

import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.context.support.ContextClassLoaderProvider;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;

/**
 * {@link KernelOsgiContextClassLoaderProvider} preserves any existing thread context class loader and uses the bundle
 * to load classes otherwise.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class KernelOsgiContextClassLoaderProvider implements ContextClassLoaderProvider {

    private final BundleDelegatingClassLoader bundleClassLoader;

    KernelOsgiContextClassLoaderProvider(Bundle bundle) {
        this.bundleClassLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getContextClassLoader() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl == null ? this.bundleClassLoader : tccl;
    }

}