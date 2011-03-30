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

package org.eclipse.virgo.apps.admin.core;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

/**
 * <p>
 * DumpExtractor provides an interface for clients to obtain dump directories.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br/>
 *
 * Implementations of this interface must be thread safe.
 *
 */
public interface DumpLocator {

    /**
     * Returns a dump directory with the given dump name.
     * 
     * @param dump name
     * @return the dump directory
     * @throws ZipException when zipping
     * @throws IOException when reading
     */
    public File getDumpDir(String dump) throws ZipException, IOException;

}
