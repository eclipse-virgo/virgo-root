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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

/**
* {@link StandardArtifactIdentityDeterminer} is a {@link ArtifactIdentityDeterminer} that can determine basic kernel artifact
* identities.
* <p />
*
* <strong>Concurrent Semantics</strong><br />
*
* This class is thread safe.
*
*/
final class StandardArtifactIdentityDeterminer implements ArtifactIdentityDeterminer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardArtifactIdentityDeterminer.class);

    private final Set<ArtifactBridge> bridges;

    public StandardArtifactIdentityDeterminer(Set<ArtifactBridge> bridges) {
        this.bridges = bridges;
    }

    public ArtifactIdentity determineIdentity(File file, String scopeName) {
        ArtifactDescriptor artifactDescriptor = null;
        for (ArtifactBridge artifactBridge : this.bridges) {
            try {
                artifactDescriptor = artifactBridge.generateArtifactDescriptor(file);
            } catch (ArtifactGenerationException e) {
                LOGGER.error(String.format("Error occurred while determining the identity of an Artifact '%s' with the bridge '%s'.", file,
                    artifactBridge.getClass().getSimpleName()), e);
                return null;
            }
            if (artifactDescriptor != null) {
                break;
            }
        }

        if (artifactDescriptor == null) {
            return null;
        }

        return new ArtifactIdentity(artifactDescriptor.getType(), artifactDescriptor.getName(), artifactDescriptor.getVersion(), scopeName);
    }
}
