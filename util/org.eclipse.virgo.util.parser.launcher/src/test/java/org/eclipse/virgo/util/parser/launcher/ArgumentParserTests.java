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

package org.eclipse.virgo.util.parser.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

public class ArgumentParserTests {

    private ArgumentParser parser = new ArgumentParser();

    @Test
    public void testParseRelativeFileBundleEntry() {
        String commandLine = "-Bsrc/test/resources/test-bundle@start";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertTrue(bd.isAutoStart());
        assertEquals(new File("src/test/resources/test-bundle").toURI(), bd.getURI());
    }
    
    @Test
    public void testParseRelativeFileBundleEntryNotStarting() {
        String commandLine = "-Bsrc/test/resources/test-bundle";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertFalse(bd.isAutoStart());
        assertEquals(new File("src/test/resources/test-bundle").toURI(), bd.getURI());
    }
    
    @Test
    public void testGradleCachePlaceholderSubstitution() {
        String commandLine = "-B%gradle.cache%/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertFalse(bd.isAutoStart());
        assertEquals(new File(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar").toURI(), bd.getURI());
    }

    @Test(expected=ParseException.class)
    public void testParseRelativeFileBundleEntryNotExists() {
        String commandLine = "-B/src/test/resources/test-bundleoeuoeu@start";
        parse(commandLine);
    }

    @Test
    public void testParseAbsoluteFileBundleEntry() {
        File f = new File("src/test/resources/test-bundle");

        String commandLine = "-B" + f.getAbsolutePath() + "@start";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertTrue(bd.isAutoStart());
        assertEquals(new File("src/test/resources/test-bundle").toURI(), bd.getURI());
    }

    @Test
    public void testParseFileURIDeclaration() {
        File f = new File("src/test/resources/test-bundle");

        String commandLine = "-B" + f.getAbsoluteFile().toURI() + "@start";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertTrue(bd.isAutoStart());
        assertEquals(new File("src/test/resources/test-bundle").getAbsoluteFile(), new File(bd.getURI()));
    }
    
    @Test
    public void testParseHttpDeclaration() throws URISyntaxException {
        URI uri = new URI("http://www.springsource.org/");
        
        String commandLine = "-B" + uri + "@start";
        LaunchCommand command = parse(commandLine);

        BundleEntry[] bundleDeclarations = command.getBundleEntries();
        assertNotNull(bundleDeclarations);
        assertEquals(1, bundleDeclarations.length);

        BundleEntry bd = bundleDeclarations[0];
        assertTrue(bd.isAutoStart());
        assertEquals(uri, bd.getURI());
    }

    @Test(expected = ParseException.class)
    public void testTooManyBundleDelimiters() {
        String commandLine = "-Bsrc/test/resources/test-bundle@start@stop";
        parse(commandLine);
    }

    @Test(expected = ParseException.class)
    public void testIncorrectBundleCommand() {
        String commandLine = "-Bsrc/test/resources/test-bundle@stop";
        parse(commandLine);
    }
    
    @Test(expected=ParseException.class)
    public void testPropertyWithExtraEquals() {
        String commandLine = "-Fprop=val=extra";
        parse(commandLine);
    }
    
    @Test
    public void testDeclaredProperty() {
        String commandLine = "-Fprop=val";
        LaunchCommand command = parse(commandLine);
        
        assertEquals("val", command.getDeclaredProperties().get("prop"));
    }

    @Test
    public void testPropertyWithNoEquals() {
        String commandLine = "-Fprop";
        LaunchCommand command = parse(commandLine);
        assertEquals("", command.getDeclaredProperties().get("prop"));
    }
    
    @Test
    public void testParseConfigFile() {
        String commandLine = "-config src/test/resources/config.properties";
        LaunchCommand command = parse(commandLine);
        
        Properties configProperties = command.getConfigProperties();
        
        assertNotNull(configProperties);
        assertEquals("bar", configProperties.getProperty("foo"));
    }
    
    @Test(expected=ParseException.class)
    public void testParseConfigFileInvalidFile() {
        String commandLine = "-config src/test/resources/.properties";
        parse(commandLine);
    }
    
    @Test(expected=ParseException.class)
    public void testParseEmptyConfig() {
        String commandLine = "-config";
        parse(commandLine);
    }
    
    @Test
    public void testParseBundles() {
    	String bundles = "src/test/resources/test-bundle,http://test@start";
    	BundleEntry[] entries = this.parser.parseBundleEntries(bundles);
    	
    	assertNotNull(entries);
    	assertEquals(2, entries.length);
    }
    
    @Test
    public void testUnrecognizedArguments() {
    	String commandLine = "-Bfile:foo.jar -Fa=b -Cbar:foo=baz -plan web";
    	LaunchCommand command = parse(commandLine);
    	
    	List<String> unrecognizedArgs = command.getUnrecognizedArguments();
    	
    	assertEquals(3, unrecognizedArgs.size());
    	assertEquals("-Cbar:foo=baz", unrecognizedArgs.get(0));
    	assertEquals("-plan", unrecognizedArgs.get(1));
    	assertEquals("web", unrecognizedArgs.get(2));
    }
    
    private LaunchCommand parse(String commandLine) {
        String[] args = commandLine.split(" ");
        return this.parser.parse(args);
    }
}
