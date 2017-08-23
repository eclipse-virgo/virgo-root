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

package org.eclipse.virgo.kernel.deployer.app.spring;

import org.osgi.framework.Bundle;

/**
 * Utility methods for working with server application module bundles. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class ApplicationBundleUtils {
    
    private static final String MODULE_TYPE_MANIFEST_HEADER = "Module-Type";


    /**
     * Gets the server module type for the supplied {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return the server module type, or <code>null</code> if the supplied <code>Bundle</code> is not a server
     *         application module.
     */
    public static String getModuleType(Bundle bundle) {
        String value = (String) bundle.getHeaders().get(MODULE_TYPE_MANIFEST_HEADER);
        return (value == null || value.length() == 0 ? null : value);
    }
}
