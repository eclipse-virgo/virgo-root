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

package org.eclipse.virgo.kernel.services.concurrent.management;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceStatistics;
import org.eclipse.virgo.kernel.services.concurrent.management.JmxExecutorServiceExporter;
import org.junit.Test;



/**
 */
public class JmxExecutorServiceExporterTests {

    @Test
    public void testExportAndDestroy() throws Exception {
        JmxExecutorServiceExporter exporter = new JmxExecutorServiceExporter("domain");
        ExecutorServiceStatistics stats = new DummyStatistics();
        exporter.export(stats);
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectInstance instance = getInstance(server);
        assertNotNull(instance);
        
        exporter.destroy();
        try {
            getInstance(server);
            fail("Instance should've been unregistered");
        } catch (Exception e) {
        }
        
    }

    private ObjectInstance getInstance(MBeanServer server) throws InstanceNotFoundException, MalformedObjectNameException {
        return server.getObjectInstance(new ObjectName("domain:type=Executor Service,name=dummy"));
    }
    
    private static class DummyStatistics implements ExecutorServiceStatistics {

        /** 
         * {@inheritDoc}
         */
        public int getActiveCount() {
            return 0;
        }

        /** 
         * {@inheritDoc}
         */
        public long getAverageExecutionTime() {
            return 0;
        }

        /** 
         * {@inheritDoc}
         */
        public long getExecutionTime() {
            return 0;
        }

        /** 
         * {@inheritDoc}
         */
        public int getLargestPoolSize() {
            return 0;
        }

        /** 
         * {@inheritDoc}
         */
        public int getMaximumPoolSize() {
            return 0;
        }

        /** 
         * {@inheritDoc}
         */
        public String getPoolName() {
            return "dummy";
        }

        /** 
         * {@inheritDoc}
         */
        public int getPoolSize() {
            return 0;
        }
        
    }
}
