/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    VMware Inc. - initial contribution
 */

package org.eclipse.virgo.kernel.artifact.fs.internal;

import java.io.File;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;

/**
 * {@link JarFileArtifactFS} is an {@link ArtifactFS} implementation for JAR files.
 */
final class JarFileArtifactFS extends FileArtifactFS implements ArtifactFS {

    /**
     * Constructs a new {@link JarFileArtifactFS} for the given file which is assumed to be in JAR format.
     * 
     * @param file a JAR file
     */
    JarFileArtifactFS(File file) {
        super(file);
    }

}
