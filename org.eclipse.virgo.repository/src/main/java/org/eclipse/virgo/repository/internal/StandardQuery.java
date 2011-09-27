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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <p>
 * The provided implementation of <code>Query</code>. The query is held as a set of required Attributes. In order for an
 * <code>Artefact</code> to match the query it must have all of the specified Attributes.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
final public class StandardQuery implements Query {

    private final Set<Attribute> filters = new HashSet<Attribute>();

    private final ArtifactDescriptorDepository artifactDepository;

    private VersionRange versionRangeFilter;

    private VersionRangeMatchingStrategy versionRangeMatchingStrategy = VersionRangeMatchingStrategy.ALL;

    private final Object filterLock = new Object();

    /**
     * Constructor taking a {@link ArtifactDescriptorDepository} to query and an initial query term.
     * 
     * @param artifactDepository to query
     * @param attributeName of attribute to initially filter on
     * @param attributeValue of attribute to match
     */
    public StandardQuery(ArtifactDescriptorDepository artifactDepository, String attributeName, String attributeValue) {
        this.artifactDepository = artifactDepository;
        this.addFilter(attributeName, attributeValue);
    }

    /**
     * Constructor taking a {@link ArtifactDescriptorDepository} to query and an initial query term.
     * 
     * @param artifactDepository to query
     * @param attributeName of attribute to initially filter on
     * @param attributeValue of attribute to match
     * @param attributeParameters of attribute to match
     */
    public StandardQuery(ArtifactDescriptorDepository artifactDepository, String attributeName, String attributeValue,
        Map<String, Set<String>> attributeParameters) {
        this.artifactDepository = artifactDepository;
        this.addFilter(attributeName, attributeValue, attributeParameters);
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value) {
        synchronized (filterLock) {
            this.filters.add(new StandardAttribute(name, value));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value, Map<String, Set<String>> parameters) {
        synchronized (filterLock) {
            this.filters.add(new StandardAttribute(name, value, parameters));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange) {
        return setVersionRangeFilter(versionRange, VersionRangeMatchingStrategy.HIGHEST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange, VersionRangeMatchingStrategy strategy) {
        synchronized (filterLock) {
            this.versionRangeFilter = versionRange;
            this.versionRangeMatchingStrategy = strategy;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> run() {
        final VersionRange localVersionRange = this.versionRangeFilter;
        final VersionRangeMatchingStrategy localVersionRangeMatchingStrategy = this.versionRangeMatchingStrategy;

        Set<RepositoryAwareArtifactDescriptor> resolved = this.artifactDepository.resolveArtifactDescriptors(this.filters);
        if (localVersionRangeMatchingStrategy != null) {
            resolved = localVersionRangeMatchingStrategy.match(resolved, localVersionRange);
        }
        return resolved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.filters.toString();
    }

}
