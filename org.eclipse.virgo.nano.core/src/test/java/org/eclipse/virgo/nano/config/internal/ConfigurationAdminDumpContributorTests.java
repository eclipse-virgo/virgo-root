/* Copyright (c) 2010 Olivier Girardot
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Olivier Girardot - initial contribution
 */

package org.eclipse.virgo.nano.config.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.eclipse.virgo.util.io.FileSystemUtils.deleteRecursively;
import static org.eclipse.virgo.util.io.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.test.stubs.service.cm.StubConfiguration;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * This class is for testing {@link ConfigurationAdminDumpContributor} class, 
 * an implementation of the {@link DumpContributor} interface.
 */
public class ConfigurationAdminDumpContributorTests {

	private static final String TEST_OUTPUT_FILE_NAME = "Test-dump.out";
	private static final String NORMAL_OUTPUT_FILE_NAME = "configurationAdmin.properties";
	private static final String DC_NAME = "configurationAdmin";

	private final File testDumpOutputDir = new File(new File("build"), "ConfigurationAdminDumpTests");
    private File testDumpOutputFile;

    @Before
    public void setupTestDir() {
        deleteRecursively(this.testDumpOutputDir);
        this.testDumpOutputDir.mkdir();
        this.testDumpOutputFile = new File(new File("build"), TEST_OUTPUT_FILE_NAME);
    }

    @After
    public void deleteTestDir() {
        deleteRecursively(this.testDumpOutputDir);
    }
	
	@Test
	public void testGetName() {
		DumpContributor configAdminDC = new ConfigurationAdminDumpContributor(
				new StubConfigurationAdmin());
		assertEquals(DC_NAME, configAdminDC.getName());
	}

	@Test
	public void testContribute() throws DumpContributionFailedException, IOException, InvalidSyntaxException {
		Dump mockDump = createMock(Dump.class);

		StubConfiguration testConfig = new StubConfiguration("test");
		testConfig.addProperty("service.key", "value");

		FileWriter dumpFileWriter = new FileWriter(testDumpOutputFile);
		expect(mockDump.createFileWriter(NORMAL_OUTPUT_FILE_NAME)).andReturn(dumpFileWriter);
		
		ConfigurationAdmin mockAdmin = createMock(ConfigurationAdmin.class);
		expect(mockAdmin.listConfigurations(null)).andReturn(new Configuration[]{testConfig});

		replay(mockDump, mockAdmin);

		DumpContributor configAdminDC = new ConfigurationAdminDumpContributor(mockAdmin);
		configAdminDC.contribute(mockDump);

		verify(mockAdmin, mockDump);
		
		assertWriterClosed(dumpFileWriter);
		assertCorrectContribution(this.testDumpOutputFile);
	}
	
	private static void assertCorrectContribution(File dumpFile) throws IOException {
        String sb = getFileAsString(dumpFile);
        
        assertTrue("Unexpected dump contribution output.",
              sb.equals("########\n# test #\n########\n\nservice.pid:\ttest\nservice.key:\tvalue\n\n\n") 
           || sb.equals("########\n# test #\n########\n\nservice.key:\tvalue\nservice.pid:\ttest\n\n\n")
              );
	}

    private static String getFileAsString(File dumpFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(dumpFile));
            while (in.ready())
                sb.append(in.readLine()).append("\n");
        } finally {
            closeQuietly(in);
        }
        return sb.toString();
    }
	
	private static void assertWriterClosed(Writer writer) {
	    try {
	        writer.flush();
	        fail("Writer not already flushed, therefore not already closed.");
	    } catch (IOException expectedAndIgnored) {
	    }
	}
}