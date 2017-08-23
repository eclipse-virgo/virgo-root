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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;

import org.eclipse.virgo.kernel.equinox.extensions.hooks.ClassLoaderCreator;

/**
 * {@link ClassLoaderCreator} to replace the standard Equinox {@link DefaultClassLoader} with the
 * {@link KernelBundleClassLoader}. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class KernelClassLoaderCreator implements ClassLoaderCreator {

    /**
     * Creates a {@link KernelBundleClassLoader} in place of the standard Equinox {@link DefaultClassLoader}.
     */
    public BaseClassLoader createClassLoader(final ClassLoader parent, final ClassLoaderDelegate delegate, final BundleProtectionDomain domain,
        final BaseData data, final String[] bundleclasspath) {
        return AccessController.doPrivileged(new PrivilegedAction<BaseClassLoader>() {
            public BaseClassLoader run() {
                return new KernelBundleClassLoader(parent, delegate, domain, data, bundleclasspath);
            }
        });
    }
}
