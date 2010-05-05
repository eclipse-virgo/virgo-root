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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.eclipse.virgo.repository.internal.external.ExternalArtifactStore;
import org.eclipse.virgo.repository.internal.external.FileSystemSearcher;
import org.junit.Test;


public class ExternalArtifactStoreTests {
    
    @Test
    public void repositoryPopulation() {
        
        final File file1 = new File("one");
        final File file2 = new File("two");
        final File file3 = new File("three");
        
        ExternalArtifactStore store = new ExternalArtifactStore(new FileSystemSearcher() {
            public void search(SearchCallback callback) {
                callback.found(file1, true);
                callback.found(file2, false);
                callback.found(file3, true);        
            }            
        });
        
        Set<File> artifacts = store.getArtifacts();
        
        assertEquals(2, artifacts.size());
        
        assertTrue(artifacts.contains(file1));
        assertTrue(artifacts.contains(file3));               
    }
}
