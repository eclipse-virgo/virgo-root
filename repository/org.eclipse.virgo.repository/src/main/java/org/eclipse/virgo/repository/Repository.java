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

import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * A <code>Repository</code> contains {@link ArtifactDescriptor}s that can be queried by searching
 * against the descriptors' name, type, version and associated meta-data. <code>ArtifactDescriptor</code>s can also
 * be retrieved directly by identifying them by type, name, and version range.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface Repository {

    /**
     * The name of the <code>Repository</code>.
     * 
     * @return name of the repository
     */
    String getName();

    /**
     * Creates a new <code>Query</code> that will run against this <code>Repository</code> and will initially have the
     * search filter specified by the key and value passed in.
     * 
     * @param key
     * @param value
     * @return a new <code>Query</code>
     */
    Query createQuery(String key, String value);

    /**
     * Creates a new <code>Query</code> that will run against this <code>Repository</code> and will initially have the
     * search filter specified by the key, value and properties passed in.
     * 
     * @param key
     * @param value
     * @param properties
     * @return a new <code>Query</code>
     */
    Query createQuery(String key, String value, Map<String, Set<String>> properties);
    
    /**
     * Returns the <code>ArtifactDescriptor</code> identified by the supplied type, name, and version range. If the
     * repository contains more than one <code>ArtifactDescriptor</code> with the supplied type and name, and version
     * within the supplied version range, the <code>ArtifactDescriptor</code> with the highest version within the
     * range is returned.
     * 
     * @param type The type of the artifact
     * @param name The name of the artifact
     * @param versionRange The version range within which the artifact's version must reside
     * @return The <code>ArtifactDescriptor</code>, or <code>null</code> is no matching artifact exists in the repository.
     */
    RepositoryAwareArtifactDescriptor get(String type, String name, VersionRange versionRange);
    
    /**
     * Stops the <code>Repository</code> allowing it to release any internal resources that it is using.
     */
    void stop();
    
}
