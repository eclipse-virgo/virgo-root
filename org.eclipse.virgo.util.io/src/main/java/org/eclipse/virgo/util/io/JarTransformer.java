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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for transforming the entries in a JAR file.
 * <p/>
 * Entries cannot be added, only changed or removed. Actual transformation of entries is performed by an implementation
 * of the {@link JarTransformerCallback} interface.
 */
public final class JarTransformer {

    private static final String MANIFEST_VERSION_HEADER = "Manifest-Version: 1.0";

    private final JarTransformerCallback callback;

    /**
     * Creates a new <code>JarTransformer</code> that uses the supplied {@link JarTransformerCallback} for
     * transformation.
     * 
     * @param callback the <code>JarTransformerCallback</code> to use for entry transformation.
     */
    public JarTransformer(JarTransformerCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        }
        this.callback = callback;
    }

    /**
     * Transforms the JAR content in <code>is</code> and writes the results to <code>os</code>.
     * 
     * @param is the JAR to transform.
     * @param stream the {@link OutputStream} to write the transformed JAR to.
     * @throws IOException if the JAR cannot be transformed.
     */
    public void transform(InputStream is, OutputStream stream) throws IOException {
        transform(is, stream, false);
    }

    /**
     * Transforms the JAR content in <code>is</code> and writes the results to <code>os</code>.
     * 
     * @param is the JAR to transform.
     * @param stream the {@link OutputStream} to write the transformed JAR to.
     * @param ensureManifestIsPresent if <code>true</code> ensures that the transformed JAR contains a manifest.
     * @throws IOException if the JAR cannot be transformed.
     */
    public void transform(InputStream is, OutputStream stream, boolean ensureManifestIsPresent) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(is);
        JarOutputStream jos = new JarOutputStream(stream);
        ZipEntry entry;
        boolean manifestPresent = false;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (JarFile.MANIFEST_NAME.equals(entryName)) {
                manifestPresent = true;
            }

            if (!entry.isDirectory()) {
                transformEntry(zipInputStream, entry, jos);
            } else {
                jos.putNextEntry(new JarEntry(entryName));
                jos.closeEntry();
            }
        }
        if (ensureManifestIsPresent && !manifestPresent) {
            JarEntry manifestEntry = new JarEntry(JarFile.MANIFEST_NAME);
            InputStream defaultManifestStream = getDefaultManifestStream();
            transformEntry(defaultManifestStream, manifestEntry, jos);
            defaultManifestStream.close();
        }
        jos.finish();
    }

    private InputStream getDefaultManifestStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println(MANIFEST_VERSION_HEADER);
            writer.println();
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void transformEntry(InputStream inputStream, ZipEntry entry, JarOutputStream jos) throws IOException {
        if (!this.callback.transformEntry(entry.getName(), inputStream, jos)) {
            jos.putNextEntry(new JarEntry(entry.getName()));
            copy(inputStream, jos);
            jos.closeEntry();
        }
    }

    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
        }
    }

    /**
     * Callback interface used to transform entries in a JAR file.
     * 
     * @see JarTransformer
     */
    public static interface JarTransformerCallback {

        /**
         * Transform the entry with the supplied name.
         * <p/>
         * Entry content can be read from the supplied {@link InputStream} and transformed contents can be written to
         * the supplied {@link OutputStream}.
         * <p/>
         * Implementations <strong>must</strong> return <code>true</code> if the entry was transformed or deleted.
         * Otherwise, <code>false</code> must be returned. No content should be written when not performing a
         * transformation.
         * <p/>
         * Implementations transforming an entry must add the entry to the output stream and close the added entry.
         * Implementations deleting an entry must not add the entry to the output stream.
         * 
         * @param entryName the name of the entry being transformed
         * @param is the entry content
         * @param os the output destination
         * @return <code>true</code> if the entry was transformed, otherwise <code>false</code>
         * @throws IOException if transformation fails
         */
        boolean transformEntry(String entryName, InputStream is, JarOutputStream os) throws IOException;
    }
}
