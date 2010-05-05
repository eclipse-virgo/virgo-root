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

package org.eclipse.virgo.repository;

import java.util.Map;
import java.util.Set;

/**
 * A <code>Query</code> is created by a {@link Repository} and is used to obtain a <code>Set</code> of
 * {@link ArtifactDescriptor ArtifactDescriptors} from the <code>Repository</code>.
 * <p />
 * 
 * A <code>Query</code> encapsulates requirements that an <code>ArtifactDescriptor</code> must satisfy in order for it
 * to match the <code>Query</code>. These requirements are in the form of name-value pairs with an optional
 * <code>Map</code> of properties. For an <code>ArtifactDescriptor</code> to match a <code>Query</code>, for each
 * name-value pair and optional map of properties, it must have a matching {@link Attribute}. An <code>Attribute</code>
 * is deemed to match a requirement if it has a name and value that match the name and value of the requirement and, if
 * the requirement includes a map of properties, this map must be equal to, or a subset of, the <code>Attribute</code>'s
 * properties.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface Query {

    /**
     * Apply a new filter to this <code>Query</code> such that, for an <code>ArtifactDescriptor</code> to match this
     * <code>Query</code>, it must have an <code>Attribute</code> with the specified name and value.
     * 
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @return the Query to allow method chaining
     */
    public Query addFilter(String name, String value);

    /**
     * Apply a new filter to this <code>Query</code> such that, for an {@link ArtifactDescriptor} to match this
     * <code>Query</code>, it must have an <code>Attribute</code> with the specified name and value, and an equal set of
     * properties.
     * 
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @param properties The properties of the attribute
     * @return the Query to allow method chaining
     */
    public Query addFilter(String name, String value, Map<String, Set<String>> properties);

    /**
     * Run the <code>Query</code>, returning a set of zero or more <code>ArtifactDescriptor</code>s.
     * 
     * @return the <code>Set</code> of <code>ArtifactDescriptor</code>s that match the query
     */
    public Set<RepositoryAwareArtifactDescriptor> run();
}
