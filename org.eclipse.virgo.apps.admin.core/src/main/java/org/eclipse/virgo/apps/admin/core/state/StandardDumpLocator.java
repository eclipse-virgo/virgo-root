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

package org.eclipse.virgo.apps.admin.core.state;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.apps.admin.core.DumpLocator;
import org.eclipse.virgo.apps.admin.core.dump.DumpPathLocator;

/**
 * <p>
 * DumpStateExtractor can extract the osgi state from an equinox dump and place it in a staging location for use by
 * clients of this class.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * DumpStateExtractor is threadsafe
 * 
 */
final class StandardDumpLocator implements DumpLocator {

    private final DumpPathLocator dumpPathLocator;

    /**
     * @param dumpPathLocator to get dumps from
     */
    public StandardDumpLocator(DumpPathLocator dumpPathLocator) {
        this.dumpPathLocator = dumpPathLocator;
    }

    /**
     * {@inheritDoc}
     */
    public File getDumpDir(String dump) throws IOException {
        if (dump == null) {
            throw new IllegalArgumentException("Requested dump cannot be null");
        }
        File dumpFolder = this.dumpPathLocator.getDumpFolder(dump);
        if (dumpFolder == null) {
            throw new IOException(String.format("Requested dump does not exist or is not a folder '%s'", dump));
        }
        return dumpFolder;
    }

}
