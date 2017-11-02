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

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.ClassLoaderCreator;

/**
 * {@link ClassLoaderCreator} to replace the standard {@link EquinoxClassLoader} with the
 * {@link KernelBundleClassLoader}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class KernelClassLoaderCreator implements ClassLoaderCreator {

    /**
     * Creates a {@link KernelBundleClassLoader} in place of the standard {@link EquinoxClassLoader}.
     */
    public ModuleClassLoader createClassLoader(final ClassLoader parent, final EquinoxConfiguration configuration, final BundleLoader delegate,
        final Generation generation) {
        return AccessController.doPrivileged(new PrivilegedAction<EquinoxClassLoader>() {

            public EquinoxClassLoader run() {
                return new KernelBundleClassLoader(parent, delegate, configuration, generation);
            }
        });
    }
}
