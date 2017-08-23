/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.persistence.openejb.classloading.hook;

import java.io.File;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppLoaderClasspathExtenderClassLoadingHookTests {

    private static final String TESTAPP = "testapp";

    private static final String SOME_FAKE_FILE = "some.fake.file";

    private static final String PERSISTENCE_INTEGRATION_JAR = "org.apache.openejb.jpa.integration.jar";

    private static final String PERSISTENCE_INTEGRATION_JAR_NAME = "org.apache.openejb.jpa.integration";

    private static final String PERSISTENCE_INTEGRATION_JAR_PROP_NAME = "persistence.integration.jar.name";

    private static final String CONFIGURATION_DIR = "configuration";

    private static final String FILE_SCHEME = "file:";

    private static final String CONFIG_AREA = "osgi.configuration.area";

    private static final String LIB_DIR = "lib";

    private static final String PERSISTENCE_DIR = "persistence";

    private static final String WEB_CONTEXTPATH_HEADER = "Web-ContextPath";

    private static final String PERSISTENCE_INTEGRATION_JAR1 = "org.apache.openejb.jpa.integration.jar_v1.1";

    @Before
    public void setUp() {
        System.setProperty(PERSISTENCE_INTEGRATION_JAR_PROP_NAME, PERSISTENCE_INTEGRATION_JAR_NAME);
    }

    @After
    public void cleanUp() {
        if (System.getProperty(CONFIG_AREA) != null) {
            System.getProperties().remove(CONFIG_AREA);
        }
        File configDir = new File(new File("."), CONFIGURATION_DIR);
        if (configDir.exists()) {
            configDir.delete();
        }

        File libDir = new File(new File("."), LIB_DIR);
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        File jpaIntegrationFile = new File(libPersistenceDir, PERSISTENCE_INTEGRATION_JAR);

        if (jpaIntegrationFile.exists()) {
            jpaIntegrationFile.delete();
        }

        File jpaIntegrationFile1 = new File(libDir, PERSISTENCE_INTEGRATION_JAR1);
        if (jpaIntegrationFile1.exists()) {
            jpaIntegrationFile1.delete();
        }

        if (libPersistenceDir.exists()) {
            libPersistenceDir.delete();
        }

        if (libDir.exists()) {
            libDir.delete();
        }
    }

    @Test
    public void testAddClassPathEntryPositive() throws IOException {
        File jpaIntegrationFile = prepare();
        ArrayList<ClasspathEntry> cpEntries = new ArrayList<ClasspathEntry>();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        manifest.put(WEB_CONTEXTPATH_HEADER, TESTAPP);
        data.setManifest(manifest);
        ClasspathManager classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        boolean result = hook.addClassPathEntry(cpEntries, "", classpathmanager, data, new ProtectionDomain(null, null));
        Assert.assertTrue("Classpath entry should be added successfully but it is not", result);
        Assert.assertTrue("There should be added only one classapth entry, but they are " + cpEntries.size(), cpEntries.size() == 1);
        ClasspathEntry entry = cpEntries.get(0);
        String classpath = ((BundleFileStub) entry.getBundleFile()).getClassPath();
        Assert.assertEquals("Classpath entry created with wrong path: ", jpaIntegrationFile.getAbsolutePath(), classpath);
    }

    @Test
    public void testAddClassPathEntryNegativeNotAppBundle() {
        ArrayList<ClasspathEntry> cpEntries = new ArrayList<ClasspathEntry>();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        data.setManifest(manifest);
        ClasspathManager classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        boolean result = hook.addClassPathEntry(cpEntries, "", classpathmanager, data, new ProtectionDomain(null, null));
        Assert.assertFalse("Classpath entry should not be added because this is not an application bundle but it is", result);
    }

    @Test
    public void testAddClassPathEntryNegativeAlreadyAdded() {
        ArrayList<ClasspathEntry> cpEntries = new ArrayList<ClasspathEntry>();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        data.setManifest(manifest);
        ClasspathManager classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        File libDir = new File(new File("."), LIB_DIR);
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        String classpath = new File(libPersistenceDir, PERSISTENCE_INTEGRATION_JAR).getAbsolutePath();
        ClasspathEntry entry = new ClasspathEntry(new BundleFileStub(classpath), null);
        cpEntries.add(entry);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        boolean result = hook.addClassPathEntry(cpEntries, "", classpathmanager, data, new ProtectionDomain(null, null));
        Assert.assertFalse("Classpath entry should not be added because it is already added", result);
    }

    @Test
    public void testAddClassPathEntryNegativeExceptionThrown() {
        ArrayList<ClasspathEntry> cpEntries = new ArrayList<ClasspathEntry>();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        manifest.put(WEB_CONTEXTPATH_HEADER, TESTAPP);
        data.setManifest(manifest);
        ClasspathManager classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        boolean result = hook.addClassPathEntry(cpEntries, "", classpathmanager, data, new ProtectionDomain(null, null));
        Assert.assertFalse("Classpath entry should not be added because the persistence integration jar is not found", result);
    }

    private File prepare() throws IOException {
        File configDir = new File(new File("."), CONFIGURATION_DIR);
        configDir.mkdir();
        File libDir = new File(new File("."), LIB_DIR);
        libDir.mkdir();
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        libPersistenceDir.mkdir();
        File jpaIntegrationFile = new File(libPersistenceDir, PERSISTENCE_INTEGRATION_JAR);
        jpaIntegrationFile.createNewFile();
        String configurationFilePath = FILE_SCHEME + configDir.getAbsolutePath();
        System.setProperty(CONFIG_AREA, configurationFilePath);
        return jpaIntegrationFile;
    }

    @Test
    public void testShouldAddPositive() {
        boolean result = checkForFile(SOME_FAKE_FILE);
        Assert.assertTrue("Check should succeed since the classpath entries list does not contain one for the jpa integration jar", result);
    }

    @Test
    public void testShouldAddNegative() {
        boolean result = checkForFile(PERSISTENCE_INTEGRATION_JAR);
        Assert.assertFalse("Check should fail since the classpath entries list already contains one for the jpa integration jar", result);
    }

    private boolean checkForFile(String filename) {
        ArrayList<ClasspathEntry> cpEntries = new ArrayList<ClasspathEntry>();
        File libDir = new File(new File("."), LIB_DIR);
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        String classpath = new File(libPersistenceDir, filename).getAbsolutePath();
        ClasspathEntry entry = new ClasspathEntry(new BundleFileStub(classpath), null);
        cpEntries.add(entry);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        return hook.shouldAdd(cpEntries);
    }

    @Test
    public void testIsAppBundlePositive() {
        boolean result = checkForApp(TESTAPP, false);
        Assert.assertTrue("Check should succeed since the manifest contains Web-ContextPath header", result);
    }

    @Test
    public void testIsAppBundleNegative() {
        boolean result = checkForApp(null, false);
        Assert.assertFalse("Check should fail since the manifest does not contain Web-ContextPath header", result);
    }

    @Test
    public void testIsAppBundleThrowsException() {
        boolean result = checkForApp(TESTAPP, true);
        Assert.assertFalse("Check should fail because exception is thrown", result);
    }

    private boolean checkForApp(String contextpathHeaderValue, boolean shouldThrowException) {
        BaseDataStub baseData = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        if (contextpathHeaderValue != null) {
            manifest.put(WEB_CONTEXTPATH_HEADER, contextpathHeaderValue);
        }
        baseData.setManifest(manifest);
        baseData.setShouldThrowException(shouldThrowException);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        return hook.isAppBundle(baseData);
    }

    @Test
    public void testNormalizeActual() {
        String filepath = FILE_SCHEME + SOME_FAKE_FILE;
        checkNormalize(filepath);
    }

    @Test
    public void testNormalizeIdentity() {
        checkNormalize(SOME_FAKE_FILE);
    }

    private void checkNormalize(String filepath) {
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        String normalizedFilepath = hook.normalize(filepath);
        Assert.assertEquals("Normalized path should be [" + SOME_FAKE_FILE + "] but it is [" + normalizedFilepath + "] instead", SOME_FAKE_FILE,
            normalizedFilepath);
    }

    @Test
    public void testFindPersistenceIntegrationJarPositive() throws IOException {
        File jpaIntegrationFile = prepare();
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        try {
            hook.findPersistenceIntegrationJar();
            Assert.assertEquals("Jpa integration jar not discovered correctly", hook.persistenceIntegrationJar.getAbsolutePath(),
                jpaIntegrationFile.getAbsolutePath());
        } catch (ClasspathExtenderClassLoadingHookException e) {
            Assert.fail("No exception should be thrown here");
        }
    }

    @Test
    public void testFindPersistenceIntegrationJarNoConfigurationPropertyDefined() {
        checkForException("Property [" + CONFIG_AREA + "] is missing");
    }

    @Test
    public void testFindPersistenceIntegrationJarMissingLibFolder() {
        File configDir = new File(new File("."), CONFIGURATION_DIR);
        String configurationFilePath = FILE_SCHEME + configDir.getAbsolutePath();
        System.setProperty(CONFIG_AREA, configurationFilePath);
        checkForException("lib folder is missing");
    }

    @Test
    public void testFindPersistenceIntegrationJarMissingIntegrationJar() {
        File configDir = new File(new File("."), CONFIGURATION_DIR);
        configDir.mkdir();
        File libDir = new File(new File("."), LIB_DIR);
        libDir.mkdir();
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        libPersistenceDir.mkdir();
        String configurationFilePath = FILE_SCHEME + configDir.getAbsolutePath();
        System.setProperty(CONFIG_AREA, configurationFilePath);
        checkForException("No file with name starting with [" + PERSISTENCE_INTEGRATION_JAR_NAME + "] was found in lib/persistence folder");
    }

    @Test
    public void testFindPersistenceIntegrationJarMultipleIntegrationJars() throws IOException {
        File jpaIntegrationFile = prepare();
        File libDir = new File(new File("."), LIB_DIR);
        File libPersistenceDir = new File(libDir, PERSISTENCE_DIR);
        File jpaIntegrationFile1 = new File(libPersistenceDir, PERSISTENCE_INTEGRATION_JAR1);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        try {
            hook.findPersistenceIntegrationJar();
            String actualPath = hook.persistenceIntegrationJar.getAbsolutePath();
            Assert.assertTrue("Jpa integration jar not discovered correctly",
                actualPath.equals(jpaIntegrationFile.getAbsolutePath()) || actualPath.equals(jpaIntegrationFile1.getAbsolutePath()));
        } catch (ClasspathExtenderClassLoadingHookException e) {
            Assert.fail("No exception should be thrown here");
        }
    }

    private void checkForException(String exceptionMessage) {
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        try {
            hook.findPersistenceIntegrationJar();
            Assert.fail("This code should not be reached - an exception is expected");
        } catch (ClasspathExtenderClassLoadingHookException e) {
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test
    public void determinePersistenceIntegrationPathPositive() throws IOException {
        File jpaIntegrationFile = prepare();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        manifest.put(WEB_CONTEXTPATH_HEADER, TESTAPP);
        data.setManifest(manifest);
        ClasspathManager classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        ClasspathEntry entry;
        try {
            entry = hook.determinePersistenceIntegrationPath(classpathmanager, data, new ProtectionDomain(null, null));
            String classpath = ((BundleFileStub) entry.getBundleFile()).getClassPath();
            Assert.assertEquals("Classpath entry created with wrong path: ", jpaIntegrationFile.getAbsolutePath(), classpath);
        } catch (ClasspathExtenderClassLoadingHookException e) {
            Assert.fail("No exception should be thrown here");
        }
    }

    @Test
    public void determinePersistenceIntegrationPathThrowsException() throws IOException {
        prepare();
        BaseClassLoader classloader = EasyMock.createMock(BaseClassLoader.class);
        BaseDataStub data = new BaseDataStub(0, null);
        Dictionary<String, String> manifest = new Hashtable<String, String>();
        manifest.put(WEB_CONTEXTPATH_HEADER, TESTAPP);
        data.setManifest(manifest);
        ClasspathManagerStub classpathmanager = new ClasspathManagerStub(data, new String[] {}, classloader);
        classpathmanager.setShouldReturnNull(true);
        AppLoaderClasspathExtenderClassLoadingHook hook = new AppLoaderClasspathExtenderClassLoadingHook();
        try {
            hook.determinePersistenceIntegrationPath(classpathmanager, data, new ProtectionDomain(null, null));
            Assert.fail("This code should not be reached - an exception is expected");
        } catch (ClasspathExtenderClassLoadingHookException e) {
            Assert.assertEquals("Failed to create classpath entry for file [" + PERSISTENCE_INTEGRATION_JAR_NAME + "]", e.getMessage());
        }
    }

}
