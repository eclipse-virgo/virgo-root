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

package org.eclipse.virgo.medic.dump.impl.heap;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.MBeanServer;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;

public final class HeapDumpContributor implements DumpContributor {

    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    private static final String HOTSPOT_DIAGNOSTIC_MBEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    private final Method heapDumpMethod;

    private final Object diagnosticMBean;

    public HeapDumpContributor() {

        Method heapDumpMethod = null;
        Object diagnosticMBean = null;

        // Java 7 strategy
        try {
            Class<?> diagnosticMbeanClass = Class.forName("com.sun.management.HotSpotDiagnosticMXBean", true,
                HeapDumpContributor.class.getClassLoader());
            diagnosticMBean = ManagementFactory.newPlatformMXBeanProxy(MBEAN_SERVER, HOTSPOT_DIAGNOSTIC_MBEAN_NAME, diagnosticMbeanClass);
        } catch (Exception e) {
            // Ignore
        }

        if (diagnosticMBean == null) {
            // Java 6 strategy
            try {
                Class<?> managementFactoryClass = Class.forName("sun.management.ManagementFactory", true, HeapDumpContributor.class.getClassLoader());
                Method method = managementFactoryClass.getMethod("getDiagnosticMXBean");
                diagnosticMBean = method.invoke(null);
            } catch (Exception e) {
                heapDumpMethod = null;
                diagnosticMBean = null;
            }
        }

        if (diagnosticMBean != null) {
            try {
                heapDumpMethod = diagnosticMBean.getClass().getMethod("dumpHeap", String.class, boolean.class);
            } catch (Exception e) {
                heapDumpMethod = null;
                diagnosticMBean = null;
            }
        }

        this.heapDumpMethod = heapDumpMethod;
        this.diagnosticMBean = diagnosticMBean;
    }

    public void contribute(Dump dump) throws DumpContributionFailedException {
        try {
            if (this.heapDumpMethod != null && this.diagnosticMBean != null) {
                heapDumpMethod.invoke(this.diagnosticMBean, dump.createFile("heap.out").getAbsolutePath(), true);
            } else {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(dump.createFileWriter("heap.err"));
                    writer.println("Diagnostic MXBean is not available. Heap dump cannot be generated.");
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw new DumpContributionFailedException("Failed to generate heap dump contribution", e);
        } catch (IllegalArgumentException e) {
            throw new DumpContributionFailedException("Failed to generate heap dump contribution", e);
        } catch (IllegalAccessException e) {
            throw new DumpContributionFailedException("Failed to generate heap dump contribution", e);
        }
    }

    public String getName() {
        return "heap";
    }
}
