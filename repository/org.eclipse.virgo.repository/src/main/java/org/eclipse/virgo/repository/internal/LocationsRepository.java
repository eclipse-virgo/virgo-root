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

package org.eclipse.virgo.repository.internal;

import java.util.Set;


/**
 * {@link org.eclipse.virgo.repository.Repository Repository}s that have file locations 
 * through which artifacts are found must implement this interface.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 *
 */
public interface LocationsRepository {
    /**
     * Calculates the filepaths that a given filename could have and be found by 
     * the repository in its storage area.
     * <br/>
     * There is no guarantee of validity of the filename or the paths returned. 
     * 
     * @param filename of artifact that might be placed in the locations
     * @return all the valid filepaths of an artifact file of that name
     */
    Set<String> getArtifactLocations(String filename);
}
