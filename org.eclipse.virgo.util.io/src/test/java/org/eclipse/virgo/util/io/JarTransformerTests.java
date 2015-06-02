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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.eclipse.virgo.util.io.JarTransformer.JarTransformerCallback;
import org.junit.Test;

public class JarTransformerTests {

    @Test
    public void testNoOpTransform() throws Exception {
        String inPath = "src/test/resources/simple-manifest-only.jar";
        String outPath = "build/testNoOpTransform.jar";

        JarTransformer jt = new JarTransformer(new NoOpJarTransformerCallback());
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(inPath);
            outStream = new FileOutputStream(outPath);
            jt.transform(inStream, outStream);

            JarFile jarFile = new JarFile(inPath);
            JarFile transformed = new JarFile(outPath);

            assertJarsSame(jarFile, transformed, true);
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
        }
    }

    @Test
    public void testSimpleManifestTweak() throws Exception {
        String inPath = "src/test/resources/simple-manifest-only.jar";
        String outPath = "build/testSimpleManifestTweak.jar";

        JarTransformer jt = new JarTransformer(new JarTransformer.JarTransformerCallback() {

            public boolean transformEntry(String entryName, InputStream is, JarOutputStream jos) throws IOException {
                if ("META-INF/MANIFEST.MF".equals(entryName)) {
                    jos.putNextEntry(new ZipEntry(entryName));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8));
                    Writer writer = new OutputStreamWriter(jos);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().length() > 0) {
                            writer.write(line);
                            writer.write("\n");
                        }
                    }
                    writer.write("My-Header: test.value\n");
                    writer.flush();
                    jos.closeEntry();
                    return true;
                } else {
                    return false;
                }
            }

        });
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(inPath);
            outStream = new FileOutputStream(outPath);
            jt.transform(inStream, outStream);

            JarFile jarFile = new JarFile(inPath);
            JarFile transformed = new JarFile(outPath);

            assertJarsSame(jarFile, transformed, true);
            String value = transformed.getManifest().getMainAttributes().getValue("My-Header");
            assertEquals("test.value", value);
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
        }
    }

    @Test
    public void ensurePresenceOfManifest() throws Exception {
        String inPath = "src/test/resources/jars/no-manifest.jar";
        String outPath = "build/ensurePresenceOfManifest.jar";

        final List<String> transformedEntries = new ArrayList<String>();

        JarTransformer jt = new JarTransformer(new JarTransformerCallback() {

            public boolean transformEntry(String entryName, InputStream is, JarOutputStream os) throws IOException {
                transformedEntries.add(entryName);
                return false;
            }
        });

        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        FileInputStream inStream2 = null;
        FileOutputStream outStream2 = null;
        try {
            inStream = new FileInputStream(inPath);
            outStream = new FileOutputStream(outPath);
            jt.transform(inStream, outStream, false);

            JarFile transformed = new JarFile(outPath);
            assertNull(transformed.getManifest());
            assertEquals(1, transformedEntries.size());
            assertFalse(transformedEntries.contains(JarFile.MANIFEST_NAME));
            transformed.close();

            transformedEntries.clear();

            inStream2 = new FileInputStream(inPath);
            outStream2 = new FileOutputStream(outPath);
            jt.transform(inStream2, outStream2, true);

            transformed = new JarFile(outPath);
            assertNotNull(transformed.getManifest());
            assertEquals(2, transformedEntries.size());
            assertTrue(transformedEntries.contains(JarFile.MANIFEST_NAME));
            transformed.close();
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(inStream2);
            IOUtils.closeQuietly(outStream2);
        }
    }

    private void assertJarsSame(JarFile a, JarFile b, boolean checkContent) throws IOException {
        List<JarEntry> aEntries = getEntries(a);
        List<JarEntry> bEntries = getEntries(b);

        assertEquals(aEntries.size(), bEntries.size());
        for (int x = 0; x < aEntries.size(); x++) {
            JarEntry ea = aEntries.get(x);
            JarEntry eb = bEntries.get(x);
            assertEquals(ea.getName(), eb.getName());

            if (checkContent) {
                try (InputStream ia = a.getInputStream(ea); InputStream ib = a.getInputStream(eb);) {
                    assertTrue(Arrays.equals(readAll(ia), readAll(ib)));
                }
            }
        }
    }

    private byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 16];
        int read;
        while ((read = is.read(buffer)) > 0) {
            bos.write(buffer, 0, read);
        }
        return bos.toByteArray();
    }

    private List<JarEntry> getEntries(JarFile f) {
        List<JarEntry> out = new ArrayList<JarEntry>();
        Enumeration<JarEntry> entries = f.entries();
        while (entries.hasMoreElements()) {
            out.add(entries.nextElement());
        }
        return out;
    }

    private static class NoOpJarTransformerCallback implements JarTransformerCallback {

        public boolean transformEntry(String entryName, InputStream is, JarOutputStream jos) {
            return false;
        }

    }
}
