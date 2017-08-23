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

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.hooks.BundleFileWrapperFactoryHook;


/**
 * A {@link BundleFileWrapperFactoryHook} into which a {@link BundleFileWrapper} implementation can be plugged. The {@link BundleFileWrapper}
 * implementation is called to wrap {@link BundleFile BundleFiles} during bundle installation and update.
 *
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class PluggableBundleFileWrapperFactoryHook implements BundleFileWrapperFactoryHook {
    
    private volatile BundleFileWrapper wrapper;
    
    private final Object monitor = new Object();
    
    private static final PluggableBundleFileWrapperFactoryHook INSTANCE =  new PluggableBundleFileWrapperFactoryHook();
    
    private PluggableBundleFileWrapperFactoryHook() {
        
    }

    public static PluggableBundleFileWrapperFactoryHook getInstance() {
        return INSTANCE;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleFile wrapBundleFile(BundleFile bundleFile, Object content, BaseData data, boolean base) throws IOException {
        synchronized(this.monitor) {
            if (wrapper != null) {
                return wrapper.wrapBundleFile(bundleFile);
            } else {
                return null;
            }
        }
    }
    
    public void setBundleFileWrapper(BundleFileWrapper wrapper) {
        synchronized(this.monitor) {
            this.wrapper = wrapper;
        }
    }
}
