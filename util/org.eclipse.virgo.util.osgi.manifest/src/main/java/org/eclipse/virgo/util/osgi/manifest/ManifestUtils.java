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

package org.eclipse.virgo.util.osgi.manifest;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.virgo.util.io.FileCopyUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class for extracting a {@link Reader} for manifest data in a JAR file and in exploded JAR directories.
 * <p/>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public final class ManifestUtils {

    private static final String MANIFEST_DIRECTORY_LOCATION = "META-INF" + File.separator + "MANIFEST.MF";

    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    /**
     * Creates a {@link Reader} for the manifest in the supplied exploded JAR directory.
     *
     * @param directory the exploded JAR directory.
     * @return the <code>Reader</code> or <code>null</code> if the manifest cannot be found.
     */
    public static Reader manifestReaderFromExplodedDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Must supply a valid directory");
        }
        try {
            File manifestFile = new File(directory.getAbsolutePath() + File.separator + MANIFEST_DIRECTORY_LOCATION);
            if (manifestFile.exists()) {
                return new InputStreamReader(new FileInputStream(manifestFile), UTF_8);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read MANIFEST for exploded directory '" + directory.getAbsolutePath() + "'.", e);
        }
    }

    /**
     * Creates a {@link Reader} for the manifest in the supplied JAR file.
     *
     * @param file the JAR file.
     * @return the <code>Reader</code> or <code>null</code> if the manifest cannot be found.
     */
    public static Reader manifestReaderFromJar(File file) {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry(MANIFEST_ENTRY);
            if (entry != null) {
                StringWriter writer = new StringWriter();
                FileCopyUtils.copy(new InputStreamReader(jar.getInputStream(entry), UTF_8), writer);
                jar.close();
                return new StringReader(writer.toString());
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot read MANIFEST.MF from jar '" + file.getAbsolutePath() + "'.", e);
        }
    }
}
