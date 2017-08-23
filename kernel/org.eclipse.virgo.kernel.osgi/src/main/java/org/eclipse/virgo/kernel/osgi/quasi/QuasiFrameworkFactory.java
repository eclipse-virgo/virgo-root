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

package org.eclipse.virgo.kernel.osgi.quasi;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

/**
 * {@link QuasiFrameworkFactory} is used to create {@link QuasiFramework QuasiFrameworks}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface QuasiFrameworkFactory {

    /**
     * Creates a {@link QuasiFramework} from the current OSGi framework state.
     * 
     * @return the <code>QuasiFramework</code>, which is never <code>null</code>
     */
    QuasiFramework create();

    /**
     * Creates a {@link QuasiFramework} from a dump in the given directory.
     * 
     * @param stateDump a {@link File} containing the dump directory
     * @return the <code>QuasiFramework</code>, which is never <code>null</code>
     * @throws ZipException when unzipping
     * @throws IOException when reading
     */
    QuasiFramework create(File stateDump)  throws ZipException, IOException ;

}
