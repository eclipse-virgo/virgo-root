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

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.virgo.util.common.CaseInsensitiveMap;
import org.eclipse.virgo.util.common.MapToDictionaryAdapter;
import org.eclipse.virgo.util.osgi.manifest.BundleActivationPolicy;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.Parseable;
import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.StandardHeaderParser;
import org.eclipse.virgo.util.parser.manifest.ManifestContents;
import org.eclipse.virgo.util.parser.manifest.ManifestParser;
import org.eclipse.virgo.util.parser.manifest.ManifestProblem;
import org.eclipse.virgo.util.parser.manifest.ManifestProblemKind;
import org.eclipse.virgo.util.parser.manifest.RecoveringManifestParser;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;


/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not thread-safe.
 */
public class StandardBundleManifest implements BundleManifest {

    private static final String MANIFEST_VERSION = "Manifest-Version";
    
    static final String MANIFEST_VERSION_VALUE = "1.0";

    private final CaseInsensitiveMap<String> contents = new CaseInsensitiveMap<String>();

    private final CaseInsensitiveMap<Parseable> headers;

    public StandardBundleManifest(ParserLogger logger) {
        this(logger, (Map<String, String>) new Hashtable<String, String>());
    }

    public StandardBundleManifest(ParserLogger logger, Map<String, String> contents) {
        this.contents.putAll(contents);
        if (!this.contents.containsKey(MANIFEST_VERSION)) {
            this.contents.put(MANIFEST_VERSION, MANIFEST_VERSION_VALUE);
        }
        this.headers = initializeHeaders(this.contents, new StandardHeaderParser(logger));
    }

    public StandardBundleManifest(ParserLogger logger, Dictionary<String, String> contents) {
        this(logger, dictionaryToMap(contents));
    }

    private static Map<String, String> dictionaryToMap(Dictionary<String, String> contents) {
        Map<String, String> map = new HashMap<String, String>(contents.size());
        Enumeration<String> keys = contents.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, contents.get(key));
        }
        return map;
    }

    public StandardBundleManifest(ParserLogger logger, Reader reader) throws IOException {
        this(logger, parseContents(reader));
    }
    
    public StandardBundleManifest(ParserLogger logger, ManifestContents manifestContents) {
        this(logger, manifestContents.getMainAttributes());
    }
   
    private static ManifestContents parseContents(Reader reader) throws IOException {
        ManifestParser parser = new RecoveringManifestParser();
        parser.setTerminateAfterMainSection(true);
        ManifestContents contents = parser.parse(reader);
        if (parser.foundProblems()) {
            List<ManifestProblem> problems = parser.getProblems();
            for (ManifestProblem problem : problems) {
                if (!problem.getKind().equals(ManifestProblemKind.ILLEGAL_NAME_CHAR) && !problem.getKind().equals(ManifestProblemKind.VALUE_TOO_LONG)) {
                    throw new IOException(problems.toString());
                }
            }            
        }
        return contents;
    }

    private static CaseInsensitiveMap<Parseable> initializeHeaders(Map<String, String> contents, HeaderParser parser) {
        CaseInsensitiveMap<Parseable> headers = createHeadersMap(parser);
        for (Map.Entry<String, Parseable> entry : headers.entrySet()) {
            String value = contents.get(entry.getKey());
            if (value != null) {
                entry.getValue().resetFromParseString(value);
            }
        }
        return headers;
    }

    private static CaseInsensitiveMap<Parseable> createHeadersMap(HeaderParser parser) {
        CaseInsensitiveMap<Parseable> headers = new CaseInsensitiveMap<Parseable>();
        headers.put(Constants.BUNDLE_ACTIVATIONPOLICY, new StandardBundleActivationPolicy(parser));
        headers.put(Constants.BUNDLE_SYMBOLICNAME, new StandardBundleSymbolicName(parser));
        headers.put(Constants.DYNAMICIMPORT_PACKAGE, new StandardDynamicImportPackage(parser));
        headers.put(Constants.EXPORT_PACKAGE, new StandardExportPackage(parser));
        headers.put(Constants.FRAGMENT_HOST, new StandardFragmentHost(parser));
        headers.put(IMPORT_BUNDLE, new StandardImportBundle(parser));
        headers.put(IMPORT_LIBRARY, new StandardImportLibrary(parser));
        headers.put(Constants.IMPORT_PACKAGE, new StandardImportPackage(parser));
        headers.put(Constants.REQUIRE_BUNDLE, new StandardRequireBundle(parser));
        return headers;
    }

    /**
     * {@inheritDoc}
     */
    public BundleActivationPolicy getBundleActivationPolicy() {
        return (BundleActivationPolicy) this.headers.get(Constants.BUNDLE_ACTIVATIONPOLICY);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getBundleClasspath() {
        return HeaderUtils.toList(Constants.BUNDLE_CLASSPATH, this.contents);
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleDescription() {
        return this.contents.get(Constants.BUNDLE_DESCRIPTION);
    }

    /**
     * {@inheritDoc}
     */
    public int getBundleManifestVersion() {
        int bundleManifestVersion = 1;
        String value = this.contents.get(Constants.BUNDLE_MANIFESTVERSION);
        if (value != null) {
            bundleManifestVersion = Integer.parseInt(value);
        }
        return bundleManifestVersion;
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleName() {
        return this.contents.get(Constants.BUNDLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public BundleSymbolicName getBundleSymbolicName() {
        return (BundleSymbolicName) this.headers.get(Constants.BUNDLE_SYMBOLICNAME);
    }

    /**
     * {@inheritDoc}
     */
    public URL getBundleUpdateLocation() {
        URL updateLocation = null;
        String value = this.contents.get(Constants.BUNDLE_UPDATELOCATION);
        if (value != null) {
            try {
                updateLocation = new URL(value);
            } catch (MalformedURLException murle) {
                // Should never happen
            }
        }
        return updateLocation;
    }

    /**
     * {@inheritDoc}
     */
    public DynamicImportPackage getDynamicImportPackage() {
        return (DynamicImportPackage) this.headers.get(Constants.DYNAMICIMPORT_PACKAGE);
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackage getExportPackage() {
        return (ExportPackage) this.headers.get(Constants.EXPORT_PACKAGE);
    }

    /**
     * {@inheritDoc}
     */
    public FragmentHost getFragmentHost() {
        return (FragmentHost) this.headers.get(Constants.FRAGMENT_HOST);
    }

    /**
     * {@inheritDoc}
     */
    public ImportBundle getImportBundle() {
        return (ImportBundle) this.headers.get(BundleManifest.IMPORT_BUNDLE);
    }

    /**
     * {@inheritDoc}
     */
    public ImportLibrary getImportLibrary() {
        return (ImportLibrary) this.headers.get(BundleManifest.IMPORT_LIBRARY);
    }

    /**
     * {@inheritDoc}
     */
    public ImportPackage getImportPackage() {
        return (ImportPackage) this.headers.get(Constants.IMPORT_PACKAGE);
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleScope() {
        return this.contents.get(MODULE_SCOPE);
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleType() {
        return this.contents.get(MODULE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public RequireBundle getRequireBundle() {
        return (RequireBundle) this.headers.get(Constants.REQUIRE_BUNDLE);
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleDescription(String bundleDescription) {
        if (bundleDescription == null) {
            this.contents.remove(Constants.BUNDLE_DESCRIPTION);
        } else {
            this.contents.put(Constants.BUNDLE_DESCRIPTION, bundleDescription);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleManifestVersion(int bundleManifestVersion) {
        this.contents.put(Constants.BUNDLE_MANIFESTVERSION, Integer.toString(bundleManifestVersion));
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleName(String bundleName) {
        if (bundleName == null) {
            this.contents.remove(Constants.BUNDLE_NAME);
        } else {
            this.contents.put(Constants.BUNDLE_NAME, bundleName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleUpdateLocation(URL bundleUpdateLocation) {
        if (bundleUpdateLocation == null) {
            this.contents.remove(Constants.BUNDLE_UPDATELOCATION);
        } else {
            this.contents.put(Constants.BUNDLE_UPDATELOCATION, bundleUpdateLocation.toExternalForm());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setModuleScope(String moduleScope) {
        if (moduleScope == null) {
            this.contents.remove(MODULE_SCOPE);
        } else {
            this.contents.put(MODULE_SCOPE, moduleScope);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setModuleType(String moduleType) {
        if (moduleType == null) {
            this.contents.remove(MODULE_TYPE);
        } else {
            this.contents.put(MODULE_TYPE, moduleType);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Dictionary<String, String> toDictionary() {
        synchroniseContentsWithHeaders();

        return new MapToDictionaryAdapter(new CaseInsensitiveMap<String>(this.contents));
    }

    /**
     * {@inheritDoc}
     */
    public Version getBundleVersion() {
        String value = this.contents.get(Constants.BUNDLE_VERSION);
        if (value != null) {
            return new Version(value);
        } else {
            return Version.emptyVersion;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleVersion(Version bundleVersion) {
        if (bundleVersion != null) {
            this.contents.put(Constants.BUNDLE_VERSION, bundleVersion.toString());
        } else {
            this.contents.remove(Constants.BUNDLE_VERSION);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(String name) {
        synchroniseContentsWithHeaders();
        return this.contents.get(name);
    }

    private void synchroniseContentsWithHeaders() {
        for (Map.Entry<String, String> content : this.contents.entrySet()) {
            Parseable header = this.headers.get(content.getKey());
            if (header != null) {
                String headerValue = header.toParseString();
                if (headerValue != null) {
                    this.contents.put(content.getKey(), headerValue);
                } else {
                    this.contents.remove(content.getKey());
                }
            }
        }
        for (Map.Entry<String, Parseable> header : this.headers.entrySet()) {
            if (this.contents.get(header.getKey()) == null) {
                String headerValue = header.getValue().toParseString();
                if (headerValue != null) {
                    this.contents.put(header.getKey(), headerValue);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setHeader(String name, String value) {
        if (value == null) {
            this.contents.remove(name);
        } else {
            this.contents.put(name, value);
        }

        Parseable header = this.headers.get(name);
        if (header != null) {
            header.resetFromParseString(value);
        }
    }

    public void write(Writer writer) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();

        Dictionary<String, String> dictionary = toDictionary();

        Enumeration<String> keys = dictionary.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            attributes.putValue(key, dictionary.get(key));
        }

        OutputStream writerOutputStream = new WriterOutputStream(writer);

        try {
            manifest.write(writerOutputStream);
        } finally {
            writerOutputStream.close();
        }
    }

    public static class WriterOutputStream extends OutputStream {

        private final Writer writer;

        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void write(int b) throws IOException {
            this.writer.write(b);
        }

        @Override
        public void close() throws IOException {
            this.writer.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringWriter outputManifest = new StringWriter();
        try {
            write(outputManifest);
        } catch (Exception e) {
            // No Exceptions here
        }
        return outputManifest.toString();
    }
}
