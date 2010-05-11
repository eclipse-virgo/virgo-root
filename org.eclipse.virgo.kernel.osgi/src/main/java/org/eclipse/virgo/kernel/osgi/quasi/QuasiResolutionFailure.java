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

package org.eclipse.virgo.kernel.osgi.quasi;


/**
 * {@link QuasiResolutionFailure} describes a failure to resolve a {@link QuasiBundle} in a {@link QuasiFramework}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class are thread safe.
 * 
 */
public interface QuasiResolutionFailure {
    
    /**
     * Gets a human readable description of the failure.
     * 
     * @return a human readable String
     */
    String getDescription();
    
    /**
     * Gets the unresolved {@link QuasiBundle}.
     * 
     * @return the unresolved <code>QuasiBundle</code>
     */
    QuasiBundle getUnresolvedQuasiBundle();

}
