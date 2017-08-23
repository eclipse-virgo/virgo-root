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

import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;

/**
 * A <code>BundleFileWrapper</code> implementation can be plugged into the {@link PluggableBundleFileWrapperFactoryHook}
 * at runtime. It will be called to wrap each {@BundleFile BundleFile} that is subsequently accessed by
 * Equinox.<p />
 * 
 * <strong>Concurrent Semantics</strong><br /> Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface BundleFileWrapper {

    /**
     * Provides an opportunity to wrap the supplied {@link BundleFile}. If the wrapper does not wish to wrap the
     * supplied <code>BundleFile</code> then <code>null</code> must be returned.
     * 
     * @param bundleFile The <code>BundleFile</code> to be wrapped
     * @return The wrapped <code>BundleFile</code>, or <code>null</code> if the no wrapping is required.
     */
    BundleFile wrapBundleFile(BundleFile bundleFile);
}
