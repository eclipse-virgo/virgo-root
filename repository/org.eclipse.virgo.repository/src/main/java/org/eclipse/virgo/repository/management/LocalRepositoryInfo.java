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

package org.eclipse.virgo.repository.management;

import java.util.Set;

import javax.management.MXBean;


/**
 * A specialisation of {@link RepositoryInfo} for {@link org.eclipse.virgo.repository.internal.LocalRepository LocalRepository} implementations.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 *
 */
@MXBean
public interface LocalRepositoryInfo extends RepositoryInfo {
    
    /**
     * Calculates the filepaths that a given filename could have and be found by 
     * the repository in its storage area.
     * <br/>
     * No attempt is made to validate the filename or the paths returned; 
     * if they pass the repository filters they are valid and not if not.
     * 
     * @param filename of artifact that might be placed in the locations
     * @return all the valid filepaths of an artifact file of that name
     */
    Set<String> getArtifactLocations(String filename);

}
