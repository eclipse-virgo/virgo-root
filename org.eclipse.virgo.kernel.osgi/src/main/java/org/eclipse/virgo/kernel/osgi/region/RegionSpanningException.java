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

import org.osgi.framework.Bundle;

/**
 * {@link RegionSpanningException} is thrown when an attempt is made to determine the region a bundle belongs when the
 * bundle spans multiple regions. The principle example is the system bundle which spans all regions.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public class RegionSpanningException extends IndeterminateRegionException {


    private static final long serialVersionUID = 545234523452345L;

    /**
     * Construct a {@link RegionSpanningException} for the given bundle.
     * 
     * @param bundleId the id of the bundle whose region cannot be uniquely determined
     */
    public RegionSpanningException(Long bundleId) {
        super("Bundle with id '" + bundleId + "' spans multiple regions");
    }
    
    /**
     * Construct a {@link RegionSpanningException} for the given bundle.
     * 
     * @param bundle the bundle whose region cannot be uniquely determined
     */
    public RegionSpanningException(Bundle bundle) {
        super("Bundle '" + bundle + "' spans multiple regions");
    }

}
