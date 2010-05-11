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

package org.eclipse.virgo.kernel.dm.internal;

import org.eclipse.virgo.kernel.serviceability.Assert;
import org.osgi.framework.Bundle;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

import org.eclipse.virgo.kernel.module.BundleDelegatingClassLoaderFactory;

/**
 */
final class StandardBundleDelegatingClassLoaderFactory implements BundleDelegatingClassLoaderFactory {

    /**
     * {@inheritDoc}
     */
    public ClassLoader createBundleDelegatingClassLoader(Bundle... bundles) {

        Assert.notEmpty(bundles, "at least one bundle must be specified");

        int delegates = bundles.length;
        Bundle head = bundles[0];
        
        if (delegates == 1) {
            return BundleDelegatingClassLoader.createBundleClassLoaderFor(head);
        } else {
            Bundle[] tail = new Bundle[delegates - 1];
            System.arraycopy(bundles, 1, tail, 0, delegates - 1);
            return BundleDelegatingClassLoader.createBundleClassLoaderFor(head, createBundleDelegatingClassLoader(tail));
        }
    }

}
