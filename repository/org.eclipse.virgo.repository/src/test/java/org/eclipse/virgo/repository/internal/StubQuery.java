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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;


/**
 * Stub impl of Query with util methods to inspect its use.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
public class StubQuery implements Query {

    private final Map<String, String> attribFilters = new HashMap<String, String>();

    public Query addFilter(String name, String value) {
        this.attribFilters.put(name, value);
        return this;
    }

    public Query addFilter(String name, String value, Map<String, Set<String>> properties) {
        this.attribFilters.put(name, value);
        return this;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange) {
        throw new UnsupportedOperationException("setVersionRangeFilter not implemented in stub");
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange, VersionRangeMatchingStrategy strategy) {
        throw new UnsupportedOperationException("setVersionRangeFilter not implemented in stub");
    }

    public Set<RepositoryAwareArtifactDescriptor> run() {
        return Collections.<RepositoryAwareArtifactDescriptor> emptySet();
    }
}
