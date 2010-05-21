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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.repository.ArtifactDescriptorPersister;

/**
 * An extension of {@link ArtifactDescriptorPersister} which allows export of the index file.
 *
 */
public interface ExportingArtifactDescriptorPersister extends ArtifactDescriptorPersister {
    /**
     * @return a persisted index file 
     * @throws IOException if such a file cannot be supplied
     */
    public File exportIndexFile() throws IOException;
}
