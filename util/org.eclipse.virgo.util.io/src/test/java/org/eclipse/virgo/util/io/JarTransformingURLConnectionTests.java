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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;

import org.eclipse.virgo.util.io.JarTransformer;
import org.eclipse.virgo.util.io.JarTransformingURLConnection;
import org.eclipse.virgo.util.io.JarTransformer.JarTransformerCallback;
import org.junit.Test;

public class JarTransformingURLConnectionTests {

    @Test
    public void testOpenConnection() throws Exception {
        URL url = new URL("file:src/test/resources/simple-manifest-only.jar");
        MockTransformer transformer = new MockTransformer();

        JarTransformingURLConnection connection = new JarTransformingURLConnection(url, new JarTransformer(transformer));
        InputStream inputStream = connection.getInputStream();
        assertNotNull(inputStream);

        assertTrue(transformer.entryNames.contains("META-INF/MANIFEST.MF"));
        assertTrue(transformer.entryNames.contains("META-INF/INDEX.LIST"));
    }

    private static class MockTransformer implements JarTransformerCallback {

        List<String> entryNames = new ArrayList<String>();

        public boolean transformEntry(String entryName, InputStream is, JarOutputStream jos) throws IOException {
            entryNames.add(entryName);
            return false;
        }

    }
}
