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

package org.eclipse.virgo.medic.dump.impl.summary;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.impl.StubDump;
import org.eclipse.virgo.medic.dump.impl.summary.SummaryDumpContributor;
import org.junit.Test;


public class SummaryDumpContributorTests {

    private final DumpContributor dumpContributor = new SummaryDumpContributor();

    private final File dumpDirectory = new File("build");

    private final DateFormat dateFormat = DateFormat.getDateInstance();

    private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);

    @Test
    public void dumpWithoutThrowable() throws DumpContributionFailedException, IOException {

        String cause = "failure";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> context = new HashMap<String, Object>();

        Dump dump = new StubDump(cause, timestamp, context, new Throwable[0], dumpDirectory);

        this.dumpContributor.contribute(dump);

        File dumpFile = new File(dumpDirectory, "summary.txt");
        assertTrue(dumpFile.exists());
        assertDumpContents(dumpFile, timestamp, cause);
    }

    @Test
    public void dumpWithThrowable() throws DumpContributionFailedException, IOException {

        String cause = "failure";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> context = new HashMap<String, Object>();

        NullPointerException npe = new NullPointerException();
        npe.fillInStackTrace();

        Dump dump = new StubDump(cause, timestamp, context, new Throwable[] { npe }, dumpDirectory);

        this.dumpContributor.contribute(dump);

        File dumpFile = new File(dumpDirectory, "summary.txt");
        assertTrue(dumpFile.exists());
        assertDumpContents(dumpFile, timestamp, cause, npe);
    }

    private void assertDumpContents(File dumpFile, long timestamp, String cause, Throwable... throwables) throws IOException {
    	List<String> lines = new ArrayList<String>();
    	try (BufferedReader input = new BufferedReader(new FileReader(dumpFile))) {
    		String line;
    		while ((line = input.readLine()) != null) {
    			lines.add(line);
    		}
		}
        assertDatePresent(lines, timestamp);
        assertTimePresent(lines, timestamp);
        assertCausePresent(lines, cause);
        assertThrowablesPresent(lines, throwables);
    }

    private void assertDatePresent(List<String> lines, long timestamp) {
        String expectedDate = this.dateFormat.format(new Date(timestamp));

        for (String line : lines) {
            if (line.contains(expectedDate)) {
                return;
            }
        }

        fail(String.format("The date '%s' was not found in the dump", expectedDate));
    }

    private void assertTimePresent(List<String> lines, long timestamp) {
        String expectedTime = this.timeFormat.format(new Date(timestamp));

        for (String line : lines) {
            if (line.contains(expectedTime)) {
                return;
            }
        }

        fail(String.format("The time '%s' was not found in the dump", expectedTime));
    }

    private void assertCausePresent(List<String> lines, String cause) {
        String expectedCause = "Cause: " + cause;
        for (String line : lines) {
            if (line.contains(expectedCause)) {
                return;
            }
        }
        fail(String.format("The expected cause entry '%s' was not found in the dump", expectedCause));
    }

    private void assertThrowablesPresent(List<String> lines, Throwable... throwables) {
        String expectedException;
        if (throwables.length == 0) {
            expectedException = "Exception: None";
        } else {
            expectedException = "Exception:";
        }

        for (String line : lines) {
            if (line.equals(expectedException)) {
                return;
            }
        }
        fail(String.format("The expected exception entry '%s' was not found in the dump", expectedException));
    }
}
