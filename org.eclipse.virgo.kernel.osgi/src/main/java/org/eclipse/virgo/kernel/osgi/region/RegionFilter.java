/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import org.eclipse.virgo.kernel.osgi.common.Version;

/**
 * A {@link RegionFilter} is associated with a connection from one region to another and determines the bundles,
 * packages, and services which are visible across the connection.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread safe.
 * 
 */
public interface RegionFilter {
    
    /**
     * Note that the system bundle has the OSGi defined symbolic name "system.bundle".
     * 
     * @param bundleSymbolicName
     * @param bundleVersion
     * @return
     */
    RegionFilter importBundle(String bundleSymbolicName, Version bundleVersion);
    
    /**
     * @param packageName
     * @return
     */
    RegionFilter importPackage(String packageName);
    
    /**
     * @param packageStem
     * @return
     */
    RegionFilter importWildcardedPackage(String packageStem);
    
    /**
     * @param serviceFilter
     * @return
     * @see org.osgi.framework.Filter more information about service filters
     */
    RegionFilter importService(String serviceFilter);

}
