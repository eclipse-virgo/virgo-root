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

package org.eclipse.virgo.apps.admin.core;

/**
 * <p>
 * FailedResolutionHolder represents the cause of a bundle failing to resovle.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of FailedResolutionHolder must be thread-safe
 *
 */
public interface FailedResolutionHolder {

    /**
     * Gets a human readable description of the failure.
     * 
     * @return a human readable String
     */
    String getDescription();
    
    /**
     * Gets the unresolved {@link BundleHolder}.
     * 
     * @return the unresolved <code>BundleHolder</code>
     */
    BundleHolder getUnresolvedBundle();
    
}
