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

package org.eclipse.virgo.repository.internal.management;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.StandardQuery;
import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.eclipse.virgo.repository.management.RepositoryInfo;


/**
 * Abstract base implementation of {@link RepositoryInfo}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
abstract class AbstractRepositoryInfo implements RepositoryInfo {

    private final String name;

    private final ArtifactDescriptorDepository artifactDepository;

    public AbstractRepositoryInfo(String name, ArtifactDescriptorDepository artifactDepository) {
        this.name = name;
        this.artifactDepository = artifactDepository;
    }

    /**
     * {@inheritDoc}
     */
    public Set<ArtifactDescriptorSummary> getAllArtifactDescriptorSummaries() {
        Set<RepositoryAwareArtifactDescriptor> artefactDescriptors = this.artifactDepository.resolveArtifactDescriptors(Collections.<Attribute> emptySet());
        Set<ArtifactDescriptorSummary> artifactDescriptorSummaries = new HashSet<ArtifactDescriptorSummary>(artefactDescriptors.size());

        for (RepositoryAwareArtifactDescriptor artifactDescriptor : artefactDescriptors) {
            String type = artifactDescriptor.getType();
            String name = artifactDescriptor.getName();
            String version = artifactDescriptor.getVersion().toString();
            artifactDescriptorSummaries.add(new ArtifactDescriptorSummary(type, name, version));
        }

        return artifactDescriptorSummaries;
    }

    public RepositoryAwareArtifactDescriptor getArtifactDescriptor(String type, String name, String version) {
        Set<RepositoryAwareArtifactDescriptor> artefacts = new StandardQuery(this.artifactDepository, ArtifactDescriptor.TYPE, type).addFilter(
            ArtifactDescriptor.NAME, name).addFilter(ArtifactDescriptor.VERSION, version).run();
        if (artefacts.size() == 1) {
            return artefacts.iterator().next();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

}
