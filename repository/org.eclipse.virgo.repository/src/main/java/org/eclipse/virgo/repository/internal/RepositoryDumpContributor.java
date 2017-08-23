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

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.codec.RepositoryCodec;
import org.eclipse.virgo.util.io.IOUtils;

/**
 * {@link DumpContributor} that adds a copy of the repository indexes to the dump folder.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class RepositoryDumpContributor implements DumpContributor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDumpContributor.class);

    private static final String CONTRIBUTOR_NAME = "repository";

    private final Map<String, ArtifactDescriptorDepository> depositories = new ConcurrentHashMap<String, ArtifactDescriptorDepository>();

    private final RepositoryCodec codec;

    public RepositoryDumpContributor(RepositoryCodec codec) {
        this.codec = codec;
    }

    /**
     * {@inheritDoc}
     */
    public void contribute(Dump dump) throws DumpContributionFailedException {
        for (Map.Entry<String, ArtifactDescriptorDepository> entry : this.depositories.entrySet()) {
            dumpDepository(entry.getKey(), entry.getValue(), dump);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return CONTRIBUTOR_NAME;
    }

    private void dumpDepository(String name, ArtifactDescriptorDepository depository, Dump dump) {
        OutputStream stream = null;
        try {
            stream = dump.createFileOutputStream(CONTRIBUTOR_NAME + "-" + name + ".index");
            Set<RepositoryAwareArtifactDescriptor> descriptors = depository.resolveArtifactDescriptors(null);
            codec.write(descriptors, stream);
        } catch (Exception e) {
            LOGGER.warn("Unable to dump repository index for repository '" + name + "'", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    void addDepository(String name, ArtifactDescriptorDepository depository) {
        this.depositories.put(name, depository);
    }
}
