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

package org.eclipse.virgo.medic.dump.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;

// Medic integration tests do not run in the Eclipse IDE.

@RunWith(OsgiTestRunner.class)
public class DumpIntegrationTests {
	
	private final BundleContext bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
	
	@Before
	public void deleteDumps() {
		File dumpsDir = new File("build", "dumps");
		if (dumpsDir.exists()) {
			deleteRecursively(dumpsDir);
		}
	}
	
	private static void deleteRecursively(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File fileInDir : files) {
					deleteRecursively(fileInDir);
				}
			}
		} 
		assertTrue(file.delete());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void dumpGeneratorAvailableFromServiceRegistry() {
		ServiceReference<DumpGenerator> serviceReference = (ServiceReference<DumpGenerator>)this.bundleContext.getServiceReference(DumpGenerator.class.getName());
		assertNotNull(serviceReference);
	}
	
	@Test
	public void dumpDirectoryConfiguration() throws IOException, InterruptedException {
		Configuration configuration = getConfiguration("org.eclipse.virgo.medic");
		assertNotNull(configuration);
		
		Dictionary<String, String> properties = new Hashtable<String, String>();		
		properties.put("dump.root.directory", "build/dumps/1");
		
		configuration.update(properties);
		
		Thread.sleep(2000);
		
		ServiceReference<?> serviceReference = this.bundleContext.getServiceReference(DumpGenerator.class.getName());
		DumpGenerator dumpGenerator = (DumpGenerator)this.bundleContext.getService(serviceReference);
		dumpGenerator.generateDump("bleurgh");
		
		File file = new File("build/dumps/1");
		assertTrue(file.exists());
		assertNotNull(file.list());
		assertEquals(1, file.list().length);

        assertDumpContributionsNumberOf(file.listFiles()[0], 3);               
        assertDumpContributionsEssential(file.listFiles()[0], "summary.txt", "thread.txt");               
        assertDumpContributionsAllowable(file.listFiles()[0], "heap.out", "heap.err", "summary.txt", "thread.txt");               

		
		properties.put("dump.root.directory", "build/dumps/2");
		configuration.update(properties);
		
		Thread.sleep(2000);
		
		dumpGenerator.generateDump("bleurgh");
		
		file = new File("build/dumps/2");
		assertTrue(file.exists());
		assertNotNull(file.list());
		assertEquals(1, file.list().length);

		assertDumpContributionsNumberOf(file.listFiles()[0], 3);               
		assertDumpContributionsEssential(file.listFiles()[0], "summary.txt", "thread.txt");               
		assertDumpContributionsAllowable(file.listFiles()[0], "heap.out", "heap.err", "summary.txt", "thread.txt");               

	}
	
	@Test
	public void exclusionConfiguration() throws IOException, InterruptedException {
		Configuration configuration = getConfiguration("org.eclipse.virgo.medic");
		assertNotNull(configuration);
		
		Dictionary<String, String> properties = new Hashtable<String, String>();		
		properties.put("dump.root.directory", "build/dumps/1");
		properties.put("dump.exclusions.bleurgh", "heap");
		
		configuration.update(properties);
		
		Thread.sleep(2000);
		
		ServiceReference<?> serviceReference = this.bundleContext.getServiceReference(DumpGenerator.class.getName());
		DumpGenerator dumpGenerator = (DumpGenerator)this.bundleContext.getService(serviceReference);
		dumpGenerator.generateDump("bleurgh");
		
		File file = new File("build/dumps/1");
		assertTrue(file.exists());
		assertNotNull(file.list());
		assertEquals(1, file.list().length);

		assertDumpContributionsNumberOf(file.listFiles()[0], 2);               
		assertDumpContributionsEssential(file.listFiles()[0], "summary.txt", "thread.txt");               

	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void logDumpEnabled() throws IOException, InterruptedException {
		Configuration configuration = getConfiguration("org.eclipse.virgo.medic");
		assertNotNull(configuration);
				
		Dictionary<String, String> properties = new Hashtable<String, String>();				
		properties.put("dump.root.directory", "build/dumps/1");
		properties.put("log.dump.level", "ERROR");
		
		configuration.update(properties);
		
		Thread.sleep(2000);
		
		LoggerFactory.getLogger(getClass()).info("Test");
		
		ServiceReference<DumpGenerator> serviceReference = (ServiceReference<DumpGenerator>)this.bundleContext.getServiceReference(DumpGenerator.class.getName());
		DumpGenerator dumpGenerator = (DumpGenerator)this.bundleContext.getService(serviceReference);
		dumpGenerator.generateDump("bleurgh");
		
		File file = new File("build/dumps/1");
		assertTrue(file.exists());
		assertNotNull(file.list());
		assertEquals(1, file.list().length);

		assertDumpContributionsNumberOf(file.listFiles()[0], 3);               
        assertDumpContributionsEssential(file.listFiles()[0], "summary.txt", "thread.txt");               
        assertDumpContributionsAllowable(file.listFiles()[0], "heap.out", "heap.err", "summary.txt", "thread.txt");               
	}
	
	private Configuration getConfiguration(String pid) throws IOException {
		ConfigurationAdmin configurationAdmin = getConfigurationAdmin();
		assertNotNull(configurationAdmin);
		
		return configurationAdmin.getConfiguration(pid);
	}
	
	@SuppressWarnings("unchecked")
	private ConfigurationAdmin getConfigurationAdmin() {
		ServiceReference<ConfigurationAdmin> serviceReference = (ServiceReference<ConfigurationAdmin>)this.bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
		assertNotNull(serviceReference);
		
		return (ConfigurationAdmin) this.bundleContext.getService(serviceReference);
	}
	
    private static void assertDumpContributionsNumberOf(File dumpDirectory, int numberAllowed) {
        assertTrue("No dump directory found or not a directory.", dumpDirectory.exists() && dumpDirectory.isDirectory());
        File[] files = dumpDirectory.listFiles();
        assertEquals("Found '" + Arrays.toString(files) + " which is not the right number of files.", numberAllowed, files.length);
    }

    private static void assertDumpContributionsEssential(File dumpDirectory, String... contributions) {
        assertTrue("No dump directory found or not a directory.", dumpDirectory.exists() && dumpDirectory.isDirectory());
        List<String> dumpDirectoryList = Arrays.asList(dumpDirectory.list());
        for (String fileName : contributions) {
            assertTrue("The file " + fileName + " was not contributed", dumpDirectoryList.contains(fileName));
        }
    }

    private static void assertDumpContributionsAllowable(File dumpDirectory, String... contributions) {
        assertTrue("No dump directory found or not a directory.", dumpDirectory.exists() && dumpDirectory.isDirectory());
        List<String> contributionsList = Arrays.asList(contributions);
        for (String fileName : dumpDirectory.list()) {
            assertTrue("The file " + fileName + " was not expected", contributionsList.contains(fileName));
        }
    }
}
