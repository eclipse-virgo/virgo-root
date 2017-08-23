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

package org.eclipse.virgo.util.io;

import java.util.List;

/**
 * Listener that is notified of file system modifications.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations <code>must</code> be threadsafe.
 * 
 */
public interface FileSystemListener {

    /**
     * Signals an event at the supplied file path.
     * 
     * @param path the path for which the event occurred.
     * @param event the event that occurred.
     */
    void onChange(String path, FileSystemEvent event);

    /**
     * Signals once for all initially observed file system objects. The method is convenient when all initial file
     * system events need to be handled all together, not one by one.
     * 
     * @param paths all the file paths for which INITIAL event occurred
     */
    void onInitialEvent(List<String> paths);

}
