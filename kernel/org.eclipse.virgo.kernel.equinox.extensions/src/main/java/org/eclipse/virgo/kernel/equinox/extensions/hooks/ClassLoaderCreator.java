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

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.storage.BundleInfo.Generation;

/**
 * A <code>ClassLoaderCreator</code> is used to create an {@link EquinoxClassLoader} for a
 * {@link org.osgi.framework.Bundle Bundle}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations must be thread-safe.
 *
 */
public interface ClassLoaderCreator {

    public ModuleClassLoader createClassLoader(ClassLoader parent, EquinoxConfiguration configuration, BundleLoader delegate, Generation generation);
}
