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

package org.eclipse.virgo.kernel.artifact.fs.internal;

import java.io.File;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSFactory;


public final class StandardArtifactFSFactory implements ArtifactFSFactory {

    /**
     * {@inheritDoc}
     */
    public ArtifactFS create(File file) {
        if (file.isDirectory()) {
            return new DirectoryArtifactFS(file);
        }
        return new FileArtifactFS(file);
    }

}
