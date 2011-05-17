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

package org.eclipse.virgo.kernel.model.management;

import javax.management.MXBean;

/**
 * Represents a bundle artifact (an artifact that represents an OSGi bundle) in the runtime model of this system. Acts
 * as a generic interface that delegates to the richer {@BundleArtifact} type and translates types that
 * are JMX-unfriendly to types that are JMX-friendly.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
@MXBean
public interface ManageableBundleArtifact extends ManageableArtifact {

    /**
     * Update an entry within this bundle. If the target path does not already exist, creates a new entry at that
     * location.
     * 
     * @param inputPath The path to read update from
     * @param targetPath The bundle relative path to write the update to
     */
    void updateEntry(String inputPath, String targetPath);

    /**
     * Delete an entry in a bundle
     * 
     * @param targetPath The bundle relative path to delete
     */
    void deleteEntry(String targetPath);
    
}
