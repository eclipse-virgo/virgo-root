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

package org.eclipse.virgo.kernel.artifact.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.jar.JarFile;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.artifact.fs.internal.JarFileArtifactFS;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 * Utility methods for working with {@link BundleManifest BundleManifests}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class BundleManifestUtils {

    /**
     * Reads the <code>BundleManifest</code> from the supplied <code>file</code>. The <code>File</code> can either be a
     * file, i.e. a jar archive, or a directory. If the file is an archive its manifest will only be read if its name
     * ends with one of the supplied <code>archiveSuffixes</code>
     * 
     * @param file The file from which the manifest is to be read.
     * @param archiveSuffixes The suffixes with which an archive's file name must end
     * @return The <code>BundleManifest</code> from the file or <code>null</code> if one was not found.
     * @throws IOException Thrown if a manifest is detected but the reading of it fails.
     * @throws SecurityException Thrown if a manifest is detected but the reading of it fails because of signature
     *         checks (invalid signature file digest...)
     */
    public static BundleManifest readBundleManifest(File file, String... archiveSuffixes) throws IOException, SecurityException {
        String fileName = file.getName();

        Reader reader = null;

        if (file.isDirectory()) {
            File manifestFile = new File(file, JarFile.MANIFEST_NAME);
            if (manifestFile.exists()) {
                reader = manifestReaderFromFile(manifestFile);
            }
        } else {
            for (String suffix : archiveSuffixes) {
                if (fileName.endsWith(suffix)) {
                    reader = manifestReaderFromJar(file);
                }
            }
        }

        if (reader != null) {
            return BundleManifestFactory.createBundleManifest(reader);
        } else {
            return null;
        }
    }

    private static Reader manifestReaderFromJar(File file) throws IOException, SecurityException {
        if (!file.exists()) {
            throw new IOException("File '" + file + "' not found");
        }
        ArtifactFS fs = new JarFileArtifactFS(file);
        ArtifactFSEntry fsEntry = fs.getEntry(JarFile.MANIFEST_NAME);
        if (fsEntry == null || !fsEntry.exists()) {
            return null; // not an error -- no manifest means this isn't a bundle
        }
        StringWriter writer = new StringWriter();
        InputStream inputStream = fsEntry.getInputStream();
		FileCopyUtils.copy(new InputStreamReader(inputStream, UTF_8), writer);
		inputStream.close();
        return new StringReader(writer.toString());
    }

    private static Reader manifestReaderFromFile(File file) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            FileCopyUtils.copy(new InputStreamReader(new FileInputStream(file), UTF_8), writer);
            return new StringReader(writer.toString());
        } catch (IOException ioe) {
            throw new IOException("Failed to create reader for manifest file.", ioe);
        }
    }
}
