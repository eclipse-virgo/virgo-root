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

package org.eclipse.virgo.kernel.model;

/**
 * Represents a bundle artifact (an artifact that is an OSGi Bundle) in the runtime model of this system. Acts as a
 * generic interface that delegates to more specific functionality in the running system. In all likelihood, there
 * should be very few sub-interfaces of this interface but quite a few implementations of this interface.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface BundleArtifact extends Artifact {

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
