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

package org.eclipse.virgo.test.launcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class LauncherTests {

    private static final String SYSTEM_PROPERTY_TMPDIR = "java.io.tmpdir";
    private static final String TMPDIR = "tmp";

    private static final File TMP_DIR = new File(TMPDIR);

    @Before
    public void setSystemProperty() {
        System.setProperty(SYSTEM_PROPERTY_TMPDIR, TMPDIR);
    }

    @After
    public void deleteTmpDir() {
        if (TMP_DIR.exists()) {
            assumeTrue(TMP_DIR.delete());
        }
    }

    @Test
    public void creationOfTmpDir() throws IOException {
        assumeTrue(!TMP_DIR.exists());

        Launcher.ensureTmpDirExists();
        assertTrue(TMP_DIR.exists());
    }

    @Test
    public void noFailureIfTmpDirAlreadyExists() throws IOException {
        assertTrue(TMP_DIR.mkdirs());
        assumeTrue(TMP_DIR.exists());

        Launcher.ensureTmpDirExists();
        assertTrue(TMP_DIR.exists());
    }

    @Test(expected=IOException.class)
    public void failureIfTmpDirCannotBeCreated() throws IOException {
        assumeTrue(TMP_DIR.createNewFile());

        Launcher.ensureTmpDirExists();
    }
}
