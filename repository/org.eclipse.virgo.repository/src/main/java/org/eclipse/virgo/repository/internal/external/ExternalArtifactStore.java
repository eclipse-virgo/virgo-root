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

package org.eclipse.virgo.repository.internal.external;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.internal.external.FileSystemSearcher.SearchCallback;



/**
 * An <code>ExternalArtifactStore</code> locates artifacts in externally-managed
 * storage using a {@link FileSystemSearcher}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ExternalArtifactStore {
    
    private final Set<File> artifacts = new HashSet<File>();
    
    public ExternalArtifactStore(FileSystemSearcher fileSystemSearcher) {        
    
        fileSystemSearcher.search(new SearchCallback() {
            public void found(File file, boolean terminal) {
                if (terminal) {
                    artifacts.add(file);
                }
            }            
        });        
    }
    
    public Set<File> getArtifacts() {
        return this.artifacts;
    }
}
