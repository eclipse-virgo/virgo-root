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

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.osgi.framework.Version;

/**
 * An <code>ArtifactDescriptor</code> describes an artifact. <code>ArtifactDescriptor</code>s are stored in a
 * {@link Repository}. Generally, an <code>ArtifactDesciptor</code> is created by an {@link ArtifactBridge}, using a
 * {@link org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder ArtifactDescriptorBuilder}. <code>ArtifactDescriptor</code>s can be retrieved from a repository, either by
 * requesting them directly by type, name, and version range, or by using a {@link Query} to search the repository.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface ArtifactDescriptor {

    /**
     * The name attribute key
     */
    String NAME = "name";

    /**
     * The type attribute key
     */
    String TYPE = "type";

    /**
     * The uri attribute key
     */
    String URI = "uri";

    /**
     * The version attribute key
     */
    String VERSION = "version";

    /**
     * The filename attribute key
     * 
     * @see File#getName()
     */
    String FILENAME_ATTRIBUTE = "filename";

    /**
     * Each <code>ArtefactDescriptor</code>'s URI is unique within a <code>Repository</code>.
     * 
     * @return the unique <code>URI</code>
     */
    URI getUri();

    /**
     * The type of the artifact
     * 
     * @return The artifact's type
     */
    String getType();

    /**
     * The name of the artifact
     * 
     * @return The artifact's name
     */
    String getName();

    /**
     * The version of the artifact
     * 
     * @return The artifact's version
     */
    Version getVersion();

    /**
     * The suggested filename for the described artifact. May be <code>null</code> if no filename could be derived from
     * the source <code>URI</code>.
     * 
     * @return The artifact's suggested filename
     */
    String getFilename();

    /**
     * Returns the <code>Attribute</code>s with the given name. If there are no attributes, an empty set is returned.
     * 
     * @param name The attribute name
     * @return the matching <code>Attribute</code>s, never <code>null</code>
     */
    Set<Attribute> getAttribute(String name);

    /**
     * Get all the <code>Attribute</code>s. If there are no attributes, an empty set is returned.
     * 
     * @return the <code>Set</code> of <code>Attribute</code>s, never <code>null</code>
     */
    Set<Attribute> getAttributes();

}
