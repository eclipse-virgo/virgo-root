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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.BundleActivationPolicy;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.DummyParserLogger;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public class StandardBundleManifestTests {

    private File resources = new File("src/test/resources/org/eclipse/virgo/util/osgi/manifest/internal");

    @Test
    public void constructionFromReader() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "basic.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("bar", bundleManifest.toDictionary().get("Foo"));
        }
    }

    @Test
    public void constructFromManifestWithMissingVersion() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "badManifest.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals(StandardBundleManifest.MANIFEST_VERSION_VALUE, bundleManifest.toDictionary().get("Manifest-Version"));
        }
    }

    @Test
    public void constructionFromManifestWithVersion() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "goodManifest.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("2.0", bundleManifest.toDictionary().get("Manifest-Version"));
        }
    }

    @Test
    public void constructionFromDictionary() {
        Dictionary<String, String> dictionary = new Hashtable<String, String>();
        dictionary.put("Bundle-SymbolicName", "com.foo.bar");

        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), dictionary);
        assertEquals("com.foo.bar", bundleManifest.getBundleSymbolicName().getSymbolicName());
    }

    @Test
    public void bundleActivationPolicy() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals(BundleActivationPolicy.Policy.LAZY, bundleManifest.getBundleActivationPolicy().getActivationPolicy());
        }
    }

    @Test
    public void bundleClasspath() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            List<String> bundleClasspath = bundleManifest.getBundleClasspath();
            assertEquals(3, bundleClasspath.size());
            assertEquals(Arrays.asList(".", "a.jar", "b.jar"), bundleClasspath);
            bundleClasspath.add("baz.jar");
            assertEquals(".,a.jar,b.jar,baz.jar", bundleManifest.toDictionary().get(Constants.BUNDLE_CLASSPATH));
            assertEquals(4, bundleManifest.getBundleClasspath().size());
            bundleClasspath.clear();
            assertNull(bundleManifest.toDictionary().get(Constants.BUNDLE_CLASSPATH));
        }
    }

    @Test
    public void bundleClasspathCreation() {
        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger());
        assertNotNull(bundleManifest.getBundleClasspath());
        List<String> bundleClasspath = bundleManifest.getBundleClasspath();
        assertEquals(0, bundleManifest.getBundleClasspath().size());
        assertNull(bundleManifest.toDictionary().get(Constants.BUNDLE_CLASSPATH));
        bundleClasspath.add("foo.jar");
        bundleClasspath.add("bar.jar");
        assertEquals(2, bundleManifest.getBundleClasspath().size());
        assertEquals("foo.jar,bar.jar", bundleManifest.toDictionary().get(Constants.BUNDLE_CLASSPATH));
        bundleClasspath.clear();
        assertNull(bundleManifest.toDictionary().get(Constants.BUNDLE_CLASSPATH));
    }

    @Test
    public void bundleDescription() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("The description", bundleManifest.getBundleDescription());

            String bundleDescription = "New description";
            bundleManifest.setBundleDescription(bundleDescription);
            assertEquals(bundleDescription, bundleManifest.getBundleDescription());

            bundleManifest.setBundleDescription(null);
            assertNull(bundleManifest.getBundleDescription());

            bundleManifest.setHeader(Constants.BUNDLE_DESCRIPTION, bundleDescription);
            assertEquals(bundleDescription, bundleManifest.getBundleDescription());
        }
    }

    @Test
    public void bundleManifestVersion() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            StandardBundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals(2, bundleManifest.getBundleManifestVersion());
            assertEquals("2", bundleManifest.getHeader(Constants.BUNDLE_MANIFESTVERSION));

            bundleManifest.setHeader(Constants.BUNDLE_MANIFESTVERSION, null);
            assertEquals(1, bundleManifest.getBundleManifestVersion());
            assertNull(bundleManifest.getHeader(Constants.BUNDLE_MANIFESTVERSION));

            bundleManifest.setBundleManifestVersion(2);
            assertEquals(2, bundleManifest.getBundleManifestVersion());
            assertEquals("2", bundleManifest.getHeader(Constants.BUNDLE_MANIFESTVERSION));

            bundleManifest.setHeader(Constants.BUNDLE_MANIFESTVERSION, "1");
            assertEquals(1, bundleManifest.getBundleManifestVersion());
            assertEquals("1", bundleManifest.getHeader(Constants.BUNDLE_MANIFESTVERSION));

            StringWriter writer = new StringWriter();
            bundleManifest.write(writer);
        }
    }

    @Test
    public void bundleName() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("The name", bundleManifest.getBundleName());
            assertEquals("The name", bundleManifest.getHeader(Constants.BUNDLE_NAME));

            bundleManifest.setBundleName("New name");
            assertEquals("New name", bundleManifest.getBundleName());
            assertEquals("New name", bundleManifest.getHeader(Constants.BUNDLE_NAME));

            bundleManifest.setBundleName(null);
            assertNull(bundleManifest.getBundleName());
            assertNull(bundleManifest.getHeader(Constants.BUNDLE_NAME));

            bundleManifest.setHeader(Constants.BUNDLE_NAME, "New name");
            assertEquals("New name", bundleManifest.getBundleName());
            assertEquals("New name", bundleManifest.getHeader(Constants.BUNDLE_NAME));
        }
    }

    @Test
    public void bundleSymbolicName() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("com.foo.bar", bundleManifest.getBundleSymbolicName().getSymbolicName());
        }
    }

    @Test
    public void bundleUpdateLocation() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals(new URL("http://update.com"), bundleManifest.getBundleUpdateLocation());

            bundleManifest.setHeader(Constants.BUNDLE_UPDATELOCATION, null);
            assertNull(bundleManifest.getBundleUpdateLocation());

            URL newUpdateLocation = new URL("http://new.update.location");
            bundleManifest.setBundleUpdateLocation(newUpdateLocation);
            assertEquals(newUpdateLocation, bundleManifest.getBundleUpdateLocation());

            bundleManifest.setBundleUpdateLocation(null);
            assertNull(bundleManifest.getBundleUpdateLocation());
        }
    }

    @Test
    public void dynamicImportPackage() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            DynamicImportPackage dynamicImportPackage = bundleManifest.getDynamicImportPackage();
            assertEquals(1, dynamicImportPackage.getDynamicallyImportedPackages().size());
            assertEquals("com.foo.*", dynamicImportPackage.getDynamicallyImportedPackages().get(0).getPackageName());
        }
    }

    @Test
    public void exportPackage() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            ExportPackage exportPackage = bundleManifest.getExportPackage();
            assertEquals(1, exportPackage.getExportedPackages().size());
            assertEquals("com.bar", exportPackage.getExportedPackages().get(0).getPackageName());
        }
    }

    @Test
    public void fragmentHost() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("com.foo.host", bundleManifest.getFragmentHost().getBundleSymbolicName());
        }
    }

    @Test
    public void importBundle() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            ImportBundle importBundle = bundleManifest.getImportBundle();
            assertEquals(1, importBundle.getImportedBundles().size());
            assertEquals("com.baz", importBundle.getImportedBundles().get(0).getBundleSymbolicName());
        }
    }

    @Test
    public void importLibrary() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            ImportLibrary importLibrary = bundleManifest.getImportLibrary();
            assertEquals(1, importLibrary.getImportedLibraries().size());
            assertEquals("com.lib", importLibrary.getImportedLibraries().get(0).getLibrarySymbolicName());
        }
    }

    @Test
    public void importPackage() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            ImportPackage importPackage = bundleManifest.getImportPackage();
            assertEquals(1, importPackage.getImportedPackages().size());
            assertEquals("com.pkg", importPackage.getImportedPackages().get(0).getPackageName());
        }
    }

    @Test
    public void moduleScope() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("the.scope", bundleManifest.getModuleScope());

            bundleManifest.setModuleScope("new.scope");
            assertEquals("new.scope", bundleManifest.getModuleScope());

            bundleManifest.setModuleScope(null);
            assertNull(bundleManifest.getModuleScope());
        }
    }

    @Test
    public void moduleType() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("web", bundleManifest.getModuleType());

            bundleManifest.setModuleType("new type");
            assertEquals("new type", bundleManifest.getModuleType());

            bundleManifest.setModuleType(null);
            assertNull(bundleManifest.getModuleType());
        }
    }

    @Test
    public void requireBundle() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            RequireBundle requireBundle = bundleManifest.getRequireBundle();
            assertEquals(1, requireBundle.getRequiredBundles().size());
            assertEquals("com.req", requireBundle.getRequiredBundles().get(0).getBundleSymbolicName());

            bundleManifest.setHeader(Constants.REQUIRE_BUNDLE, null);
            assertEquals(0, requireBundle.getRequiredBundles().size());
        }
    }

    @Test
    public void bundleVersion() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals(new Version(1, 2, 3), bundleManifest.getBundleVersion());
            assertEquals("1.2.3", bundleManifest.getHeader(Constants.BUNDLE_VERSION));

            bundleManifest.setBundleVersion(new Version(3, 2, 1));
            assertEquals(new Version(3, 2, 1), bundleManifest.getBundleVersion());
            assertEquals("3.2.1", bundleManifest.getHeader(Constants.BUNDLE_VERSION));

            bundleManifest.setBundleVersion(null);
            assertEquals(Version.emptyVersion, bundleManifest.getBundleVersion());

            assertNull(bundleManifest.getHeader(Constants.BUNDLE_VERSION));
        }
    }

    @Test
    public void writerReaderRoundTrip() throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "all-headers.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            StringWriter writer = new StringWriter();
            bundleManifest.write(writer);

            String one = writer.toString();

            bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(one));
            writer = new StringWriter();
            bundleManifest.write(writer);

            assertEquals(one, writer.toString());
        }
    }

    @Test
    public void caseInsensitivity() {
        Map<String, String> contents = new HashMap<String, String>();
        contents.put("BunDLE-SymBOLICnAME", "foo");
        contents.put("Bundle-Name", "the bundle");
        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), contents);
        assertEquals("foo", bundleManifest.getBundleSymbolicName().getSymbolicName());
        assertEquals("the bundle", bundleManifest.getHeader("BundlE-NAMe"));
    }

    @Test
    public void preservationOfHeaderNameCase() {
        Map<String, String> contents = new HashMap<String, String>();
        contents.put("BunDLE-SymBOLICnAME", "foo");
        contents.put("Bundle-Name", "the bundle");
        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), contents);

        Dictionary<String, String> dictionary = bundleManifest.toDictionary();
        Enumeration<String> keys = dictionary.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!"BunDLE-SymBOLICnAME".equals(key) && !"Bundle-Name".equals(key) && !"Manifest-Version".equals(key)) {
                fail("Unexpected key " + key);
            }
        }
    }

    @Test
    public void copeWithNullHeaderStringsDuringSynchronisation() {
        Map<String, String> contents = new HashMap<String, String>();
        contents.put(Constants.BUNDLE_SYMBOLICNAME, "foo");

        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), contents);
        assertEquals("foo", bundleManifest.getHeader(Constants.BUNDLE_SYMBOLICNAME));

        bundleManifest.getBundleSymbolicName().setSymbolicName(null);

        assertNull(bundleManifest.getHeader(Constants.BUNDLE_SYMBOLICNAME));
    }

    @Test
    public void toDictionary() {
        Map<String, String> contents = new HashMap<String, String>();
        contents.put(Constants.BUNDLE_VERSION, "1.0");
        contents.put(Constants.FRAGMENT_HOST, "host");

        BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), contents);

        bundleManifest.getBundleSymbolicName().setSymbolicName("foo");
        bundleManifest.getFragmentHost().setBundleSymbolicName("bar");

        Dictionary<String, String> dictionary = bundleManifest.toDictionary();
        assertEquals("foo", dictionary.get(Constants.BUNDLE_SYMBOLICNAME));
        assertEquals("bar", dictionary.get(Constants.FRAGMENT_HOST));
    }

    @Test
    public void extremelyLargeManifest() throws FileNotFoundException, IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(new File(resources, "verylarge.mf")), UTF_8)) {
            BundleManifest bundleManifest = new StandardBundleManifest(new DummyParserLogger(), fileReader);
            assertEquals("very.large.manifest", bundleManifest.getBundleSymbolicName().getSymbolicName());
        }
    }
}
