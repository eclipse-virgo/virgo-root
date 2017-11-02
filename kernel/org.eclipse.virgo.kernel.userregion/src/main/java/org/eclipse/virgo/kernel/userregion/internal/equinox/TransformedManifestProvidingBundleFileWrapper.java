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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Stack;
import java.util.jar.JarFile;

import org.eclipse.osgi.container.Module;
import org.eclipse.osgi.storage.bundlefile.BundleEntry;
import org.eclipse.osgi.storage.bundlefile.BundleFile;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.BundleFileWrapper;
import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
import org.eclipse.virgo.kernel.osgi.framework.ManifestTransformer;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 * A <code>BundleFileWrapper</code> implementation that wraps {@link BundleFile BundleFiles} and replaces the manifest in the
 * <code>BundleFile</code> will one that has been transformed in memory.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class TransformedManifestProvidingBundleFileWrapper implements BundleFileWrapper {
    
    private final ImportExpander importExpander;
    
    private final ThreadLocal<Stack<ManifestTransformer>> manifestTransformer;
    
    public TransformedManifestProvidingBundleFileWrapper(ImportExpander importExpander) {
        this.manifestTransformer = new ManifestTransformerStackThreadLocal();
        this.importExpander = importExpander;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleFile wrapBundleFile(BundleFile bundleFile) {        
        return new TransformedManifestProvidingBundleFile(bundleFile, this.importExpander);
    }
    
    public void pushManifestTransformer(ManifestTransformer manifestTransformer) {
        this.manifestTransformer.get().push(manifestTransformer);
    }
    
    public void popManifestTransformer() {
        this.manifestTransformer.get().pop();
    }
    
    private static final class ManifestTransformerStackThreadLocal extends ThreadLocal<Stack<ManifestTransformer>> {

        @Override
        public Stack<ManifestTransformer> initialValue() {
            return new Stack<ManifestTransformer>();
        }
    }

    /**
     * A concrete extension of {@link BundleFile} that intercepts attempts to access a bundle's manifest
     * a returns a transform copy of the manifest that has, e.g. expanded any Import-Library and Import-Bundle
     * headers into the corresponding Import-Package header entries.
     * <p>
     * <strong>Concurrent Semantics</strong><br />
     * As thread-safe as the encapsulated <code>BundleFile</code> instance.
     * 
     */
    private class TransformedManifestProvidingBundleFile extends BundleFile {

        private final BundleFile bundleFile;
        
        private final ImportExpander importExpander;
        
        private volatile BundleEntry manifestEntry;
        
        private final Object monitor = new Object();

        private TransformedManifestProvidingBundleFile(BundleFile bundleFile, ImportExpander importExpander) {
            super(bundleFile.getBaseFile());
            this.bundleFile = bundleFile;
            this.importExpander = importExpander;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            this.bundleFile.close();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsDir(String dir) {
            return this.bundleFile.containsDir(dir);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BundleEntry getEntry(String path) { 
            if (path.equals(JarFile.MANIFEST_NAME)) {
                synchronized (monitor) {
                    if (this.manifestEntry == null) {
                        BundleEntry entry = this.bundleFile.getEntry(path);
                        
                        Stack<ManifestTransformer> manifestTransformers = TransformedManifestProvidingBundleFileWrapper.this.manifestTransformer.get();
                        
                        ManifestTransformer manifestTransformer;
                        
                        if (!manifestTransformers.isEmpty()) {
                            manifestTransformer = manifestTransformers.peek();
                        } else {
                            manifestTransformer = null;
                        }
                                              
                        BundleManifest originalManifest;
                        
                        if (entry != null) {
                            try (InputStreamReader manifestReader = new InputStreamReader(entry.getInputStream(), UTF_8)) {
                                originalManifest = BundleManifestFactory.createBundleManifest(manifestReader);
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        } else {
                            originalManifest = BundleManifestFactory.createBundleManifest();
                        } 
                        
                        BundleManifest transformedManifest = originalManifest;
                        
                        if (manifestTransformer != null) {
                            transformedManifest = manifestTransformer.transform(originalManifest);                                                        
                        }
                        
                        try {
                            this.importExpander.expandImports(Collections.singletonList(transformedManifest));
                        } catch (UnableToSatisfyDependenciesException utsde) {
                            throw new RuntimeException(utsde);
                        }
                        
                        try {
                            this.manifestEntry = new TransformedManifestBundleEntry(transformedManifest);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }                
                    }
                }
                
                return this.manifestEntry;
            } else {
                return this.bundleFile.getEntry(path);
            }
        }

        @Override
        public Enumeration<String> getEntryPaths(String path) {
            return this.bundleFile.getEntryPaths(path);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Enumeration<String> getEntryPaths(String path, boolean recurse) {
            return this.bundleFile.getEntryPaths(path, recurse);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public File getFile(String path, boolean nativeCode) {
            return this.bundleFile.getFile(path, nativeCode);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void open() throws IOException {
            this.bundleFile.open();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public File getBaseFile() {
            return this.bundleFile.getBaseFile();
        }

        @Override
        public URL getResourceURL(String path, Module hostModule, int index) {
            return this.bundleFile.getResourceURL(path, hostModule, index);
        }
    }
    
    private static class TransformedManifestBundleEntry extends BundleEntry {
        
        private final byte[] manifestBytes;
        
        private final long time;
        
        private TransformedManifestBundleEntry(BundleManifest transformedManifest) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformedManifest.write(new OutputStreamWriter(baos, UTF_8));
            
            this.manifestBytes = baos.toByteArray();
            
            time = System.currentTimeMillis();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public URL getFileURL() {
            throw new UnsupportedOperationException();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream() throws IOException {                  
            return new ByteArrayInputStream(this.manifestBytes); 
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public URL getLocalURL() {
            throw new UnsupportedOperationException();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "MANIFEST.MF";
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public long getSize() {
            return this.manifestBytes.length;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public long getTime() {
            return this.time;
        }       
    }
}
