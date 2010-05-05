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

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWO;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;


/**
 * <p>
 * Stub of ArtefactDepository with some util methods to query its use.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
public class StubArtefactDepository implements ArtifactDescriptorDepository {

    private String lastCall = "";

    private Set<Attribute> filters = null;

    private Set<RepositoryAwareArtifactDescriptor> artefacts = new HashSet<RepositoryAwareArtifactDescriptor>();

    private RepositoryAwareArtifactDescriptor returnItem = null;

    public static final String NONE = "none";

    public static final String ONE = "one";

    public static final String MANY = "many";

    /**
     * {@inheritDoc}
     */
    public void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artefact) {
        this.lastCall = "addArtefact" + artefact.toString();
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryAwareArtifactDescriptor removeArtifactDescriptor(URI uri) {
        this.lastCall = "removeArtefact" + uri.toString();
        return returnItem;
    }

    /**
     * {@inheritDoc}
     */
    public void persist() {
        this.lastCall = "persistArtefactDepository";
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> resolveArtifactDescriptors(Set<Attribute> filters) {
        this.filters = filters;
        return artefacts;
    }

    /**
     * {@inheritDoc}
     */
    public int getArtifactDescriptorCount() {
        return 0;
    }

    public String getLastMethodCall() {
        return this.lastCall;
    }

    public Set<Attribute> getFilters() {
        return filters;
    }

    public void setNextReturnCount(String number) {
        this.artefacts = new HashSet<RepositoryAwareArtifactDescriptor>();
        if (number.equals(NONE)) {
            // no-op
        } else if (number.equals(ONE)) {
            this.artefacts.add(TEST_ARTEFACT_ONE);
        } else if (number.equals(MANY)) {
            this.artefacts.add(TEST_ARTEFACT_ONE);
            this.artefacts.add(TEST_ARTEFACT_TWO);
        } else {
            throw new RuntimeException("Test error, unknown number of items requested from the test");
        }

    }

    public void setReturnItem(RepositoryAwareArtifactDescriptor artefact) {
        this.returnItem = artefact;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        throw new UnsupportedOperationException();
    }

}
