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

/**
 * A <code>FileSystemSearcher</code> is used to search the file system to find artefacts
 * 
 * <strong>Concurrent Semantics</strong><br /> 
 * Implementations need not be thread-safe.
 * 
 */
interface FileSystemSearcher {

    /**
     * Searches the search space, notifying the given callback of matching files or directories that are found.
     * @param callback the <code>SearchCallback</code> to notify when a file or directory is found.
     */
    void search(SearchCallback callback);

    /**
     * Callback for searching against the search path. <p/>
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations need not be thread-safe.
     * 
     */
    public interface SearchCallback {

        /**
         * Called when a file is found that matches the search pattern.
         * 
         * @param file the matching file.
         * @param terminal flag indicating whether the file matches against the terminal element of the search pattern.
         */
        void found(File file, boolean terminal);
    }
}
