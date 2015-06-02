/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link IOUtils}.
 * 
 * @author Steve Powell
 */
public class IOUtilsTests {

    private static final File testDir = new File("build/testio");

    @Before
    @After
    public void setClearDir() {
        FileSystemUtils.deleteRecursively(testDir);
        testDir.mkdirs();
    }

    @Test
    public void testCloseQuietly() throws IOException {
        File file = new File(testDir, "closeQuietly.txt");
        assertTrue("Cannot not create test file", file.createNewFile());

        try (FileInputStream fis = new FileInputStream(file)) {
            FileChannel fc = fis.getChannel();
            assertEquals(-1, fis.read());
            IOUtils.closeQuietly(fis);
            assertFalse("Channel should have been closed", fc.isOpen());
        }
    }
}
