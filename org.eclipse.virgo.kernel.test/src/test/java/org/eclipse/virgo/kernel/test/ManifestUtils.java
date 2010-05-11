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

package org.eclipse.virgo.kernel.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.virgo.util.io.FileCopyUtils;

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
    public static final Reader manifestReaderFromExplodedDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Must supply a valid directory");
        }
        try {
            File manifestFile = new File(directory.getAbsolutePath() + File.separator + MANIFEST_DIRECTORY_LOCATION);
            if (manifestFile.exists()) {
                return new FileReader(manifestFile);
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
    public static final Reader manifestReaderFromJar(File file) {
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry(MANIFEST_ENTRY);
            if (entry != null) {
                StringWriter writer = new StringWriter();
                FileCopyUtils.copy(new InputStreamReader(jar.getInputStream(entry)), writer);
                jar.close();
                return new StringReader(writer.toString());
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot read MANIFEST.MF from jar '" + file.getAbsolutePath() + "'.", e);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ioe) {
                    throw new RuntimeException("Failed to close jar '" + file.getAbsolutePath() + "'.", ioe);
                }
            }
        }
    }
}
