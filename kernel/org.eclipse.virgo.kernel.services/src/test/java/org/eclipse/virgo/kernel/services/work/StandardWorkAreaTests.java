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

package org.eclipse.virgo.kernel.services.work;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.services.work.StandardWorkArea;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class StandardWorkAreaTests {

    private static final String WORK_DIR_NAME = "com.foo";

    private static final String KERNEL_WORK_DIR_NAME = "bar";

    private static final String KERNEL_BSN = "org.eclipse.virgo.kernel." + KERNEL_WORK_DIR_NAME;

    @Before
    public void before() {
        FileSystemUtils.deleteRecursively(new File("./build/work", WORK_DIR_NAME));
        FileSystemUtils.deleteRecursively(new File("./build/work", KERNEL_WORK_DIR_NAME));
    }

    @Test
    public void nonNullWorkDirectory() {

        StubBundle bundle = new StubBundle(WORK_DIR_NAME, Version.emptyVersion);

        StandardWorkArea manager = new StandardWorkArea(new File("./build/work"), bundle);
        PathReference workDir = manager.getWorkDirectory();

        assertNotNull(workDir);
        assertTrue("work dir does not exist", workDir.exists());
        assertTrue(workDir.isDirectory());

        assertTrue(new File("./build/work", WORK_DIR_NAME + "_" + Version.emptyVersion).exists());
    }

    @Test
    public void kernelWorkDirectory() {
        StubBundle bundle = new StubBundle(KERNEL_BSN, Version.emptyVersion);

        StandardWorkArea manager = new StandardWorkArea(new File("./build/work"), bundle);
        PathReference workDir = manager.getWorkDirectory();

        assertNotNull(workDir);
        assertTrue("work dir does not exist", workDir.exists());
        assertTrue(workDir.isDirectory());

        assertTrue(new File("./build/work", KERNEL_WORK_DIR_NAME).exists());
    }
}
