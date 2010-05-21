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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.virgo.apps.admin.core.DumpExtractor;


/**
 */
final public class StubDumpExtractor implements DumpExtractor {

    public List<File> getListOfPossibleDumps() {
        return new ArrayList<File>();
    }

    /**
     * {@inheritDoc}
     */
    public File getStateDump(String dump) throws ZipException, IOException {
        return new File(dump);
    }

}
