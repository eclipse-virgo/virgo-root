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

package org.eclipse.virgo.repository.internal.chain;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.osgi.framework.Version;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
class ChainedQuery implements Query {

    private final List<Query> queries;

    ChainedQuery(List<Query> queries) {
        this.queries = queries;
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value) {
        for (Query query : this.queries) {
            query.addFilter(name, value);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value, Map<String, Set<String>> properties) {
        for (Query query : this.queries) {
            query.addFilter(name, value, properties);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange) {
        for (Query query : this.queries) {
            query.setVersionRangeFilter(versionRange);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange, VersionRangeMatchingStrategy strategy) {
        for (Query query : this.queries) {
            query.setVersionRangeFilter(versionRange, strategy);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> run() {
        Set<RepositoryAwareArtifactDescriptor> artefacts = new TreeSet<RepositoryAwareArtifactDescriptor>();

        int index = 0;

        for (Query query : this.queries) {
            Set<RepositoryAwareArtifactDescriptor> queryResult = query.run();
            for (RepositoryAwareArtifactDescriptor artefact : queryResult) {
                artefacts.add(new ComparableArtifactDescriptor(index++, artefact));
            }
        }

        return artefacts;
    }

    static final class ComparableArtifactDescriptor implements RepositoryAwareArtifactDescriptor, Comparable<ComparableArtifactDescriptor> {

        private final int index;

        private final RepositoryAwareArtifactDescriptor artifactDescriptor;

        ComparableArtifactDescriptor(int index, RepositoryAwareArtifactDescriptor artefact) {
            this.index = index;
            this.artifactDescriptor = artefact;
        }

        /**
         * {@inheritDoc}
         */
        public Set<Attribute> getAttribute(String name) {
            return this.artifactDescriptor.getAttribute(name);
        }

        /**
         * {@inheritDoc}
         */
        public Set<Attribute> getAttributes() {
            return this.artifactDescriptor.getAttributes();
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            return this.artifactDescriptor.getName();
        }

        /**
         * {@inheritDoc}
         */
        public String getType() {
            return this.artifactDescriptor.getType();
        }

        /**
         * {@inheritDoc}
         */
        public URI getUri() {
            return this.artifactDescriptor.getUri();

        }

        /**
         * {@inheritDoc}
         */
        public Version getVersion() {
            return this.artifactDescriptor.getVersion();
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(ComparableArtifactDescriptor o) {
            return this.index - o.index;
        }

        @Override
        public boolean equals(Object o) {
            return this.artifactDescriptor.equals(o);
        }

        @Override
        public int hashCode() {
            return this.artifactDescriptor.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public String getFilename() {
            return this.artifactDescriptor.getFilename();
        }

        public String getRepositoryName() {
            return this.artifactDescriptor.getRepositoryName();
        }
    }
}
