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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.hooks.BundleFileWrapperFactoryHook;


/**
 * A {@link BundleFileWrapperFactoryHook} that keeps track of {@link BundleFile BundleFiles} and,
 * when instructed to clean up, ensures that they are closed.
 * 
 * <p />
 * 
 * This is a workaround for Equinox bug 290389. Unfortunately when working with nested frameworks
 * the suggested workaround of calling PackageAdmin.refreshPackages does not work for the
 * child framework's composite bundle.
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class BundleFileClosingBundleFileWrapperFactoryHook implements BundleFileWrapperFactoryHook {
    
    private final Object monitor = new Object();
    
    private final List<BundleFile> bundleFiles = new ArrayList<BundleFile>();
    
    private static final BundleFileClosingBundleFileWrapperFactoryHook INSTANCE = new BundleFileClosingBundleFileWrapperFactoryHook();
    
    private BundleFileClosingBundleFileWrapperFactoryHook() {
    }

    /** 
     * {@inheritDoc}
     */
    public BundleFile wrapBundleFile(BundleFile bundleFile, Object content, BaseData data, boolean base) throws IOException {
        synchronized (this.monitor) {
            this.bundleFiles.add(bundleFile);
        }
        return null;
    }
    
    public void cleanup() {
        List<BundleFile> localBundleFiles;
        synchronized (this.monitor) {
            localBundleFiles = new ArrayList<BundleFile>(this.bundleFiles);
            this.bundleFiles.clear();
        }
        for (BundleFile bundleFile : localBundleFiles) {
            try {
                bundleFile.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    public static BundleFileClosingBundleFileWrapperFactoryHook getInstance() {
        return INSTANCE;
    }
}
