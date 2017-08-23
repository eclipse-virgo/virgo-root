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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

public class JarUtilsTests {

    private final PathReference expected = new PathReference("build/dummy");

    @Before
    public void before() {
        expected.delete(true);
    }

    @Test
    public void testUnpackIntoDir() throws Exception {
        PathReference jar = new PathReference("src/test/resources/jars/dummy.jar");
        PathReference target = new PathReference("./build");
        JarUtils.unpackTo(jar, target);
        assertTrue(this.expected.exists());
        PathReference file = expected.newChild("com/foo/bar/dummyDoc.txt");
        assertTrue(file.exists());
        try (Reader fileReader = new InputStreamReader(new FileInputStream(file.toFile()), UTF_8)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                String line = reader.readLine();
                assertEquals("Hello There!", line);
            }
        }
    }
}
