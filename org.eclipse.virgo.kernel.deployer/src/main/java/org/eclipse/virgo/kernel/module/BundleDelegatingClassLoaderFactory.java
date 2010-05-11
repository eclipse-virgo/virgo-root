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

package org.eclipse.virgo.kernel.module;

import org.osgi.framework.Bundle;

/**
 * {@link BundleDelegatingClassLoaderFactory} is used to create class loaders that can delegate to a sequence of
 * bundles. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public interface BundleDelegatingClassLoaderFactory {

    /**
     * Create a {@link ClassLoader} that delegates to the given bundles in turn until class loading or resource finding
     * succeeds or the end of the sequence of bundles is reached.
     * 
     * @param bundles bundles to delegate to
     * @return a delegating class loader
     */
    ClassLoader createBundleDelegatingClassLoader(Bundle... bundles);

}
