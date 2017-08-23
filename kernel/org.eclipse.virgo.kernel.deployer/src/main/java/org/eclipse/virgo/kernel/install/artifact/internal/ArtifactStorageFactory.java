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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.File;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;


/**
 * An {@link ArtifactStorageFactory} can be used to create {@link ArtifactStorage} instances.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface ArtifactStorageFactory {

    /**
     * Creates a new <code>ArtifactStorage</code> for the artifact in the given <code>File</code>. The artifact has the
     * supplied <code>artifactIdentity</code>.
     * 
     * @param artifact The artifact's <code>File</code>.
     * @param artifactIdentity The identity of the artifact for which storage is being created
     * @return <code>ArtifactStorage</code> for the artifact.
     */
    ArtifactStorage create(File artifact, ArtifactIdentity artifactIdentity);

    /**
     * Creates a new empty <code>ArtifactStorage</code>. Once created, the artifact must have the supplied
     * <code>artifactIdentity</code>.
     * 
     * @param artifactIdentity The identity of the artifact for which storage is being created
     * @param directoryName The name of the directory for the artifact
     * @param scopeName The scope in which the artifact resides
     * @return <code>ArtifactStorage</code> for the artifact.
     */
    ArtifactStorage createDirectoryStorage(ArtifactIdentity artifactIdentity, String directoryName);

}
