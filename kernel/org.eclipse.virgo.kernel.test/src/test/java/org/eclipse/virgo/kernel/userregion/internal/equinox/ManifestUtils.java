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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import java.io.File;
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

    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

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
                FileCopyUtils.copy(new InputStreamReader(jar.getInputStream(entry)), writer);
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
