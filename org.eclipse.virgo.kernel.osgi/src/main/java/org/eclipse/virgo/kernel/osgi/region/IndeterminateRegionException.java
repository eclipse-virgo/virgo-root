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

/**
 * {@link IndeterminateRegionException} is thrown when the region of a bundle cannot be determined.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public abstract class IndeterminateRegionException extends Exception {

    private static final long serialVersionUID = 76745737151453L;

    /**
     * Construct an {@link IndeterminateRegionException} for the given bundle.
     * 
     * @param bundleId the id of the bundle whose region cannot be determined
     */
    public IndeterminateRegionException(String message) {
        super(message);
    }

}
