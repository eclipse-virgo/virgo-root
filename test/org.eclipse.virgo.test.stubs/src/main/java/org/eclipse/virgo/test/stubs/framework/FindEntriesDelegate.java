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

package org.eclipse.virgo.test.stubs.framework;

import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * An interface to allow delegation of calls to {@link Bundle#findEntries(String, String, boolean)} to be serviced in a
 * test environment.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface FindEntriesDelegate {

    /**
     * @param path The root to search from
     * @param filePattern The pattern to search for
     * @param recurse Whether to recurse
     * @return An enumeration of the files found
     * @see Bundle#findEntries(String, String, boolean)
     */
    Enumeration<URL> findEntries(String path, String filePattern, boolean recurse);
}
