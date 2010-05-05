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

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * <code>StandardArtifactDescriptorDepository</code> is the provided implementation of
 * {@link ArtifactDescriptorDepository}. Contains the actual store of artifacts in the Repository and is also capable of
 * executing searches as specified by a set of filters. In order to improve performance a custom index over the set of
 * artifacts is used.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This implementation is thread-safe
 * 
 * @see org.eclipse.virgo.repository.internal.Index
 */
public class StandardArtifactDescriptorDepository implements ArtifactDescriptorDepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardArtifactDescriptorDepository.class);

    private final ArtifactDescriptorPersister artifactDescriptorPersister;

    private final Set<RepositoryAwareArtifactDescriptor> artifactDescriptors;

    private final Index index;

    private final Object artifactsLock = new Object();

    StandardArtifactDescriptorDepository(ArtifactDescriptorPersister artifactDescriptorPersister,
        Set<RepositoryAwareArtifactDescriptor> artifactDescriptors) throws IndexFormatException {
        if (artifactDescriptorPersister == null || artifactDescriptors == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        this.artifactDescriptorPersister = artifactDescriptorPersister;
        this.artifactDescriptors = this.artifactDescriptorPersister.loadArtifacts();
        this.artifactDescriptors.addAll(artifactDescriptors);
        this.index = new Index(this.artifactDescriptors);
    }

    StandardArtifactDescriptorDepository(Set<RepositoryAwareArtifactDescriptor> artifactDescriptors) {
        if (artifactDescriptors == null) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        this.artifactDescriptorPersister = null;
        this.artifactDescriptors = new HashSet<RepositoryAwareArtifactDescriptor>(16);
        this.artifactDescriptors.addAll(artifactDescriptors);
        this.index = new Index(this.artifactDescriptors);
    }

    /**
     * Constructor requiring only a persistence strategy. The {@link ArtifactDescriptorPersister} will be queried during
     * initialisation to see if any existing meta data about artifacts has been stored, if they have they will be loaded
     * and used to pre-populate the store of {@link ArtifactDescriptor}s in this Depository.
     * 
     * @param artifactDescriptorPersister persistence strategy
     * @throws IndexFormatException if initial load fails
     */
    public StandardArtifactDescriptorDepository(ArtifactDescriptorPersister artifactDescriptorPersister) throws IndexFormatException {
        this(artifactDescriptorPersister, new HashSet<RepositoryAwareArtifactDescriptor>(0));
    }

    /**
     * {@inheritDoc}
     */
    public int getArtifactDescriptorCount() {
        return this.artifactDescriptors.size();
    }

    /**
     * {@inheritDoc}
     */
    public void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) throws IllegalArgumentException,
        DuplicateArtifactException {
        if (artifactDescriptor == null) {
            throw new IllegalArgumentException("Argument can not be null");
        }
        synchronized (this.artifactsLock) {
            if (this.artifactDescriptors.contains(artifactDescriptor)) {
                RepositoryAwareArtifactDescriptor original = null;
                for (RepositoryAwareArtifactDescriptor temp : this.artifactDescriptors) {
                    if (temp.equals(artifactDescriptor)) {
                        original = temp;
                    }
                }
                throw new DuplicateArtifactException(original, artifactDescriptor);
            }
            this.artifactDescriptors.add(artifactDescriptor);
            this.index.addArtifactDescriptor(artifactDescriptor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryAwareArtifactDescriptor removeArtifactDescriptor(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Argument can not be null");
        }

        Query uriQuery = new StandardQuery(this, "uri", uri.toString());
        synchronized (this.artifactsLock) {
            Set<RepositoryAwareArtifactDescriptor> artifacts = uriQuery.run();

            if (artifacts.size() > 1) {
                LOGGER.debug("More than one artifact with URI '{}' in depository {}.", uri, this);
                throw new IllegalStateException("Internal failure: multiple artifacts with the same URI.");
            }

            RepositoryAwareArtifactDescriptor artifact = null;

            if (!artifacts.isEmpty()) {
                artifact = artifacts.iterator().next();
                this.index.removeArtifactDescriptor(artifact);
                this.artifactDescriptors.remove(artifact);
            }

            return artifact;
        }
    }

    public boolean removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        synchronized (this.artifactsLock) {
            this.index.removeArtifactDescriptor(artifactDescriptor);
            return this.artifactDescriptors.remove(artifactDescriptor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void persist() throws IOException {
        if (null != this.artifactDescriptorPersister) {
            synchronized (this.artifactsLock) {
                this.artifactDescriptorPersister.persistArtifactDescriptors(this.artifactDescriptors);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> resolveArtifactDescriptors(Set<Attribute> filters) {
        synchronized (this.artifactsLock) {
            if (filters == null || filters.isEmpty()) {
                return new HashSet<RepositoryAwareArtifactDescriptor>(this.artifactDescriptors);
            }
            Set<RepositoryAwareArtifactDescriptor> results = new HashSet<RepositoryAwareArtifactDescriptor>(this.artifactDescriptors);
            for (Attribute filter : filters) {
                results.retainAll(findMatchingArtifacts(filter));
            }
            return results;
        }
    }

    /**
     * Execute a single filter of a query.
     * 
     * @param filter
     * @return
     */
    private Set<ArtifactDescriptor> findMatchingArtifacts(Attribute filter) {
        Set<Attribute> attributes = this.index.findMatchingAttributes(filter.getKey(), filter.getValue());

        Set<Attribute> propertyFilteredAttributes = new HashSet<Attribute>();

        for (Attribute attribute : attributes) {
            if (this.propertiesContains(filter.getProperties(), attribute.getProperties())) {
                propertyFilteredAttributes.add(attribute);
            }
        }

        Set<ArtifactDescriptor> matches = new HashSet<ArtifactDescriptor>();
        for (Attribute attribute : propertyFilteredAttributes) {
            matches.add(this.index.getArtifactDescriptor(attribute));
        }
        return matches;
    }

    /**
     * Returns true if and only if the attributeProperties contains all of the filterProperties
     * 
     * @param filterProperties
     * @param attributeProperties
     * @return
     */
    private boolean propertiesContains(Map<String, Set<String>> filterProperties, Map<String, Set<String>> attributeProperties) {
        for (Entry<String, Set<String>> filterProperty : filterProperties.entrySet()) {
            if (!(attributeProperties.containsKey(filterProperty.getKey()) && attributeProperties.get(filterProperty.getKey()).containsAll(
                filterProperty.getValue()))) {
                return false;
            }
        }
        return true;
    }

}
