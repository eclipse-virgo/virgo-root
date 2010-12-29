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

import org.osgi.framework.Bundle;

/**
 * {@link UserRegionNotInitialisedException} is thrown when an attempt is made to determine the region of a user region
 * bundle before the user region {@link Region} instance is available.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public class UserRegionNotInitialisedException extends IndeterminateRegionException {

    private static final long serialVersionUID = 7283427865453L;

    /**
     * Construct a {@link UserRegionNotInitialisedException} since the {@link Region} of the specified bundle is not yet
     * initialised.
     * 
     * @param bundle the bundle whose {@link Region} was requested
     */
    public UserRegionNotInitialisedException(Bundle bundle) {
        super("User region Region instance not available to return as the region of bundle '" + bundle + "'");
    }

    /**
     * Construct a {@link UserRegionNotInitialisedException} since the {@link Region} of the bundle with the specified bundle id is not yet
     * initialised.
     * 
     * @param bundleId the id of the bundle whose {@link Region} was requested
     */
    public UserRegionNotInitialisedException(Long bundleId) {
        super("User region Region instance not available to return as the region of bundle id '" + bundleId + "'");
    }
}
