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

package org.eclipse.virgo.shell.internal.help;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.shell.internal.help.HelpAccessor;
import org.eclipse.virgo.shell.internal.help.SimpleFileHelpAccessor;
import org.junit.Test;

/**
 * Tests for {@link SimpleFileHelpAccessor}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
public class SimpleFileHelpAccessorTests {

    private static final String HELP_ACCESSOR_TEST_FILE_NAME = "org.eclipse.virgo.shell.internal.help.SimpleFileHelpAccessorTests.help";

    private static final URL HELP_ACCESSOR_TEST_HELP_FILE_URL;
    static {
        URL url = null;
        try {
            url = new File("src/test/resources/helpAccessorTests.help").toURI().toURL();
        } catch (MalformedURLException mue) {
            assertTrue("Cannot define URL in test", false);
        }
        HELP_ACCESSOR_TEST_HELP_FILE_URL = url;
    }

    private static class AccessorTestClass extends SimpleFileHelpAccessor {

        protected URL helpResourceUrl(Class<?> clazz, String fileResourceName) {
            assertEquals("File resource name not correctly formed from classname", HELP_ACCESSOR_TEST_FILE_NAME, fileResourceName);
            return HELP_ACCESSOR_TEST_HELP_FILE_URL;
        }
    }

    private static class NoHelpAccessorTestClass extends SimpleFileHelpAccessor {

        protected URL helpResourceUrl(Class<?> clazz, String fileResourceName) {
            return null;
        }
    }

    private HelpAccessor haTest = new AccessorTestClass();

    @Test
    public void readHelpFileSummary() throws Exception {
        String helpSummary = haTest.getSummaryHelp(SimpleFileHelpAccessorTests.class);
        assertEquals("First line not read correctly", "First line of help text", helpSummary);
    }

    @Test
    public void readHelpFileDetails() throws Exception {
        List<String> lines = haTest.getDetailedHelp(SimpleFileHelpAccessorTests.class);
        assertEquals("Detail not read correctly", Arrays.asList("Line 1", "  Line 2"), lines);
    }

    @Test
    public void summaryWithNoHelpFile() throws Exception {
        HelpAccessor missingHelp = new NoHelpAccessorTestClass();
        assertNull(missingHelp.getSummaryHelp(Object.class));
    }

    @Test
    public void detailWithNoHelpFile() throws Exception {
        HelpAccessor missingHelp = new NoHelpAccessorTestClass();
        assertTrue(missingHelp.getDetailedHelp(Object.class).isEmpty());
    }
}
