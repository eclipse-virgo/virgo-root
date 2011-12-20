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

package org.eclipse.virgo.kernel.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.osgi.extender.support.ApplicationContextConfiguration;

import org.eclipse.virgo.util.common.StringUtils;


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
     * Queries whether the supplied {@link Bundle} is Spring-DM powered.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is Spring-DM powered, otherwise <code>false</code>.
     */
    public static boolean isSpringOsgiPoweredBundle(Bundle bundle) {
        return new ApplicationContextConfiguration(bundle).isSpringPoweredBundle();
    }        
    
    /**
     * Queries whether the supplied {@link Bundle} is a fragment
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is fragment, otherwise <code>false</code>.
     */
    // TODO Move this method into utils project
    public static boolean isFragmentBundle(Bundle bundle) {
        String fragmentHostHeader = (String) bundle.getHeaders().get(Constants.FRAGMENT_HOST);
        return StringUtils.hasText(fragmentHostHeader);
    }
    
    
}
