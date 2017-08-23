package org.eclipse.virgo.medic.management;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.impl.config.ConfigurationChangeListener;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.junit.Before;
import org.junit.Test;

public class FileSystemDumpInspectorTests {
    
	private FileSystemDumpInspector fileSystemDumpInspector;
	
	private DumpGenerator dumpGenerator;
    
	@Before
	public void setup() {
		this.dumpGenerator = createMock(DumpGenerator.class);
		this.fileSystemDumpInspector = new FileSystemDumpInspector(dumpGenerator, new ConfigurationProvider() {
			
			@Override
			public boolean removeChangeListener(ConfigurationChangeListener listener) {
				return false;
			}
			
			@Override
			public Dictionary<String, Object> getConfiguration() {
				Dictionary<String, Object> props = new Hashtable<String, Object>();
				props.put(KEY_DUMP_ROOT_DIRECTORY, "src/test/resources/testDumps/serviceability/dump");
				return props;
			}
			
			@Override
			public void addChangeListener(ConfigurationChangeListener listener) {
			}
		});
	}
	
	@Test
	public void testGetDumps() throws IOException {
		String[] dumps = fileSystemDumpInspector.getDumps();
		assertTrue(dumps.length == 1);
		assertEquals("Unexpected dump found" + dumps[0], "testDump", dumps[0]);
	}
	
	@Test
	public void testGetDumpEntries() throws IOException {
		String[][] dumpEntries = fileSystemDumpInspector.getDumpEntries("testDump");
		assertTrue(dumpEntries.length == 1);
		assertArrayEquals(new String[]{"testDumpItem.txt", "DumpInspector/getDumpEntry/testDump/testDumpItem.txt"}, dumpEntries[0]);
	}
	
	@Test
	public void testGetDumpEntriesNotThere() throws IOException {
		String[][] dumpEntries = fileSystemDumpInspector.getDumpEntries("notHere");
		assertTrue(dumpEntries.length == 0);
	}
	
	@Test
	public void testGetDumpEntry() {
		String[] dumpEntry = fileSystemDumpInspector.getDumpEntry("testDump", "testDumpItem.txt");
		assertTrue(dumpEntry.length == 2);
		assertEquals("Unexpected dump found" + dumpEntry[0], "foo", dumpEntry[0]);
		assertEquals("Unexpected dump found" + dumpEntry[1], "bar", dumpEntry[1]);
	}
	
	@Test
	public void testGetDumpEntryNotThere() {
		String[] dumpEntry = fileSystemDumpInspector.getDumpEntry("testDump", "notHere");
		assertTrue(dumpEntry.length == 0);
	}
	
	@Test
	public void testCreateDump(){
		this.dumpGenerator.generateDump("Generated via JMX");
		expectLastCall().once();
		replay(this.dumpGenerator);
		fileSystemDumpInspector.createDump();
		verify(this.dumpGenerator);
	}
	
	@Test
	public void testDeleteDump() {
		File deleteMe = new File("src/test/resources/testDumps/serviceability/dump/deleteMe");
		deleteMe.mkdir();
		assertTrue(deleteMe.exists() && deleteMe.isDirectory());
		fileSystemDumpInspector.deleteDump("deleteMe");
		assertFalse(deleteMe.exists());
	}
    
}
