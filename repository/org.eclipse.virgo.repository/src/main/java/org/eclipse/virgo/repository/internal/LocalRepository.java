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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.UriMapper;
import org.eclipse.virgo.repository.configuration.LocalRepositoryConfiguration;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public abstract class LocalRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRepository.class);

    private final Set<ArtifactBridge> artifactBridges;

    private final UriMapper mapper;

    private final EventLogger eventLogger;

    protected LocalRepository(LocalRepositoryConfiguration configuration, ArtifactDescriptorPersister artifactDescriptorPersister,
        EventLogger eventLogger) throws IndexFormatException {
        super(configuration, new StandardArtifactDescriptorDepository(artifactDescriptorPersister));
        this.eventLogger = eventLogger;
        this.mapper = configuration.getUriMapper();
        this.artifactBridges = new HashSet<ArtifactBridge>(configuration.getArtefactBridges());
    }

    protected RepositoryAwareArtifactDescriptor createArtifactDescriptor(File artifactFile) {
        boolean seenFailure = false; // track rejections for this file in case they are rescinded
        for (ArtifactBridge artifactBridge : this.artifactBridges) {
            try {
                ArtifactDescriptor artifactDescriptor = artifactBridge.generateArtifactDescriptor(artifactFile);
                if (artifactDescriptor != null) {
                    if (seenFailure) {
                        // This artifact was rejected by a previous bridge; we log subsequent success
                        LOGGER.debug("ArtifactBridge '{}' rescued artifact '{}'.", artifactBridge, artifactFile);
                        eventLogger.log(RepositoryLogEvents.ARTIFACT_RECOVERED, artifactFile.getName(), this.getName());
                    }
                    return new DelegatingRepositoryAwareArtifactDescriptor(artifactDescriptor, getName(), this.mapper);
                }
            } catch (ArtifactGenerationException age) {
                LOGGER.error(String.format("ArtifactBridge '%s' failed to generate descriptor for artifact '%s'.", artifactBridge, artifactFile), age);
                eventLogger.log(RepositoryLogEvents.BRIDGE_PARSE_FAILURE, artifactFile.getName(), age.getArtifactType(), this.getName());
                seenFailure = true;
            } catch (Exception e) {
                LOGGER.error(String.format("ArtifactBridge '%s' threw unexpected execption for artifact '%s'.", artifactBridge, artifactFile), e);
                eventLogger.log(RepositoryLogEvents.BRIDGE_UNEXPECTED_EXCEPTION, artifactFile.getName(), this.getName());
                seenFailure = true;
            }
        }
        return null;
    }
}
