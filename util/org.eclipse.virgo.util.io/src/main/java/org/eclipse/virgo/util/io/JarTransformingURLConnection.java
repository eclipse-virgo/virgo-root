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
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of {@link URLConnection} that transforms JAR files as they are read.
 * <p/>
 * A {@link URL} is used to source the real connection for the JAR data, and a {@link JarTransformer} is used to
 * customize the exact transformations being performed.
 * 
 * @see JarTransformer
 */
public final class JarTransformingURLConnection extends URLConnection {

    private final JarTransformer transformer;
    
    private final boolean ensureManifestIsPresent;

    /**
     * Creates a new <code>JarTransformingURLConnection</code> that will provide content from the JAR identified by
     * <code>url</code> transformed by <code>transformer</code>.
     * 
     * @param url the {@link URL} of the JAR file.
     * @param transformer the <code>JarTransformer</code> to apply as content is being read.
     */
    public JarTransformingURLConnection(URL url, JarTransformer transformer) {
    	this(url, transformer, false);
    }
    
    /**
     * Creates a new <code>JarTransformingURLConnection</code> that will provide content from the JAR identified by
     * <code>url</code> transformed by <code>transformer</code> and that will optionally ensure that a manifest is
     * provided, creating one if necessary.
     * 
     * @param url the {@link URL} of the JAR file.
     * @param transformer the <code>JarTransformer</code> to apply as content is being read.
     * @param ensureManifestIsPresent <code>true</code> if the presence of a MANIFEST.MF should be ensured.
     */
    public JarTransformingURLConnection(URL url, JarTransformer transformer, boolean ensureManifestIsPresent) {
        super(url);
        this.transformer = transformer;
        this.ensureManifestIsPresent = ensureManifestIsPresent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream rawInputStream = url.openStream();
        try {
            this.transformer.transform(rawInputStream, baos, this.ensureManifestIsPresent);
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            baos.close();
            try {
                rawInputStream.close();
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public void connect() throws IOException {
    }

}
