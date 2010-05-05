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

import java.net.URI;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.osgi.framework.Version;


/**
 * A <code>MutableRepository</code> is a repository which is programmatically mutable, i.e. it supports programmatic
 * publish and retract.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe
 * 
 */
public interface MutableRepository extends LocationsRepository {

    ArtifactDescriptor publish(URI uri) throws DuplicateArtifactException;

    boolean retract(String type, String name, Version version);
}
