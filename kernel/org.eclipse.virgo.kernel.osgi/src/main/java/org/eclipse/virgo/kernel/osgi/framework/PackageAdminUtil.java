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

package org.eclipse.virgo.kernel.osgi.framework;

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
     * Synchronously drives refresh packages for the given bundles.
     *  
     * @param bundles The bundles to refresh.
     */
    void synchronouslyRefreshPackages(Bundle[] bundles);
}
