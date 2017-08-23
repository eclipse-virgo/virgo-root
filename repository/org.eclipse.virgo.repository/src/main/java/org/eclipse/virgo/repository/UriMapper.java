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

import java.net.URI;

import org.osgi.framework.Version;

/**
 * A <code>UriMapper</code> can be provided when creating a {@link Repository} to allow a client to perform a
 * <code>URI</code> to <code>URI</code> mapping when an artifact is being entered into the index of the
 * <code>Repository</code>. Such mapping allows the URIs returned as part of an {@link ArtifactDescriptor} to be
 * different to the URIs from which the artifacts were originally sourced.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface UriMapper {

    /**
     * Maps the given <code>URI</code> to the returned <code>URI</code>. The given <code>URI</code> points to the
     * artefact with the given type name and version.
     * 
     * @param uri The uri to be mapped
     * @param type The type of the artefact identified by the given uri.
     * @param name The name of the artefact identified by the given uri.
     * @param version The version of the artefact identified by the given uri.
     * 
     * @return The mapped uri
     */
    URI map(URI uri, String type, String name, Version version);
}
