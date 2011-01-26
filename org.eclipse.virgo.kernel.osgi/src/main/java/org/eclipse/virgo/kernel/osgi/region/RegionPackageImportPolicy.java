/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Map;

/**
 * {@link RegionPackageImportPolicy} determines the package names that are imported into a region.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RegionPackageImportPolicy {
    
    /**
     * Returns <code>true</code> if and only if the package with the specified name exported with the specified
     * attributes and directives is imported into the region.
     * 
     * @param packageName the name of the package
     * @param attributes the package's export attributes
     * @param directives the package's export directives
     * @return <code>true</code> if and only if the package is imported
     */
    boolean isImported(String packageName, Map<String, Object> attributes, Map<String, String> directives);

}
