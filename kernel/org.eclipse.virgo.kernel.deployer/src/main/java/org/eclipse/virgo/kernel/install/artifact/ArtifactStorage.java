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

package org.eclipse.virgo.kernel.install.artifact;

import java.net.URI;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;


public interface ArtifactStorage {

    void synchronize();

    void synchronize(URI sourceUri);
    
    void rollBack();

    void delete();

    ArtifactFS getArtifactFS();

}
