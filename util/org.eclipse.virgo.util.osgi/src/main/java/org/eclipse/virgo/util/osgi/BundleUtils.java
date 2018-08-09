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

package org.eclipse.virgo.util.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;

import static org.osgi.framework.wiring.BundleRevision.TYPE_FRAGMENT;

/**
 * <code>BundleUtils</code> provides utility methods for interacting with {@link Bundle Bundles}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
public final class BundleUtils {

    /**
     * Queries whether the supplied {@link Bundle} is a fragment
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is fragment, otherwise <code>false</code>.
     */
    public static boolean isFragmentBundle(Bundle bundle) {
        BundleRevision rev = bundle.adapt(BundleRevision.class);
        return rev != null && (rev.getTypes() & TYPE_FRAGMENT)!= 0;
    }

    /**
     * Queries whether the supplied {@link Bundle} is the system bundle.
     *
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if <code>bundle</code> is the system bundle, otherwise <code>false</code>.
     */
    public static boolean isSystemBundle(Bundle bundle) {
        return bundle != null && bundle.getBundleId() == 0;
    }
}
