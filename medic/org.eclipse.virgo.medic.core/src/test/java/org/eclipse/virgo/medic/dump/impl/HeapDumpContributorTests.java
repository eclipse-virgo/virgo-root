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

package org.eclipse.virgo.medic.dump.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.impl.heap.HeapDumpContributor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HeapDumpContributorTests {

    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    private static final String HOTSPOT_DIAGNOSTIC_MBEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    private final File dumpDirectory = new File("build");

    @Before
    @After
    public void cleanupHeadDumps() {
        File heapDumpFile = new File("build", "heap.out");
        if (heapDumpFile.exists()) {
            assertTrue(heapDumpFile.delete());
        }
    }

    @Test
    public void dumpHeap() throws DumpContributionFailedException {
        DumpContributor contributor = new HeapDumpContributor();

        String cause = "failure";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> context = new HashMap<String, Object>();

        Dump dump = new StubDump(cause, timestamp, context, new Throwable[0], dumpDirectory);

        contributor.contribute(dump);

        boolean diagnostMbeanAvailable;

        try {
            Class<?> diagnosticMbeanClass = Class.forName("com.sun.management.HotSpotDiagnosticMXBean", true,
                HeapDumpContributor.class.getClassLoader());
            ManagementFactory.newPlatformMXBeanProxy(MBEAN_SERVER, HOTSPOT_DIAGNOSTIC_MBEAN_NAME, diagnosticMbeanClass);
            diagnostMbeanAvailable = true;
        } catch (Exception e) {
            diagnostMbeanAvailable = false;
        }

        if (!diagnostMbeanAvailable) {
            try {
                Class.forName("sun.management.ManagementFactory").getMethod("getDiagnosticMXBean");
                diagnostMbeanAvailable = true;
            } catch (Exception e) {
                diagnostMbeanAvailable = false;
            }
        }

        assertEquals(diagnostMbeanAvailable, new File(this.dumpDirectory, "heap.out").exists());
    }
}
