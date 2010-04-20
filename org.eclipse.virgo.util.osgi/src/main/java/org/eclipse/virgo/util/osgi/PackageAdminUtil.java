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

/**
 * {@link PackageAdminUtil} provides utilities relating to {@link org.osgi.service.packageadmin.PackageAdmin PackageAdmin}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface PackageAdminUtil {

    /**
     * Issue {@link org.osgi.service.packageadmin.PackageAdmin#refreshPackages(Bundle[]) refreshPackages} and wait for at most the given timeout for the asynchronous refresh
     * operation to complete.
     * 
     * @param bundles the {@link Bundle Bundles} whose exported packages are to be refreshed or <code>null</code> for
     *        all <code>Bundle</code>s updated or uninstalled since {@link org.osgi.service.packageadmin.PackageAdmin#refreshPackages(Bundle[]) refreshPackages} was last called
     * @param timeoutMillis a number of milliseconds to wait for the asynchronous refresh operation to complete
     */
    void refreshPackages(Bundle[] bundles, long timeoutMillis);

}
