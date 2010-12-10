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
 * TODO Document RegionMembership
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * TODO Document concurrent semantics of RegionMembership
 * 
 */
public interface RegionMembership {

    /**
     * Determines whether or not the specified bundle belongs to the region represented by this {@link RegionMembership}.
     * 
     * @param bundle the {@link Bundle} to be checked for membership
     * @return <code>true</code> if and only if the specified bundle belongs to the region
     */
    boolean isMember(Bundle bundle);

}
