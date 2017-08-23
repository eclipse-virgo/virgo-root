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

package org.eclipse.virgo.kernel.stubs;

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;


/**
 * <code>StubArtifactFS</code> implements the {@link ArtifactFS} interface for testing.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 * 
 */
public class StubArtifactFS implements ArtifactFS {

    private final URI sourceURI;

    private final File location;

    private final String name;

    public StubArtifactFS(URI sourceURI, File location, String name) {
        this.sourceURI = sourceURI;
        this.location = location;
        this.name = name;
    }

    public File getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public URI getSourceURI() {
        return this.sourceURI;
    }

    /**
     * {@inheritDoc}
     */
    public File getFile() {
        return this.location;
    }

    public ArtifactFSEntry getEntry(String name) {
        return null;
    }
}
