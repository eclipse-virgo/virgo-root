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

package org.eclipse.virgo.kernel.concurrent.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.kernel.userregion.internal.equinox.ManifestUtils;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

@Ignore
public class ApplicationContextDependencyMonitorIntegrationTests extends AbstractKernelIntegrationTest {

    @Test 
    public void testDependencyMonitoring() throws Exception {
        QuasiFramework framework = OsgiFrameworkUtils.getService(this.context, QuasiFrameworkFactory.class).getService().create();
        File file = new File("src/test/resources/QuickConsumer.jar");
        BundleManifest manifest;
        try (Reader manifestReader = ManifestUtils.manifestReaderFromJar(file)) {
            manifest = BundleManifestFactory.createBundleManifest(manifestReader);
        }
        QuasiBundle quasiQuickConsumer = framework.install(file.toURI(), manifest);
        framework.resolve();
        framework.commit();
        
        Bundle quickConsumer = quasiQuickConsumer.getBundle();
        
        ServiceReference<BundleStarter> serviceReference = this.kernelContext.getServiceReference(BundleStarter.class);
        BundleStarter bundleStarter = this.kernelContext.getService(serviceReference);
        
        final CountDownLatch latch = new CountDownLatch(1);
        
        bundleStarter.start(quickConsumer, new AbortableSignal() {
            public void signalFailure(Throwable cause) {
                cause.printStackTrace();
            }

            public void signalSuccessfulCompletion() {
                latch.countDown();
            }

			public void signalAborted() {
				new RuntimeException("Start aborted").printStackTrace();
			}
        });
                       
        assertTrue(latch.await(20, TimeUnit.SECONDS));
       
        // We need to sleep for a little while to give the
        // log output sufficient time to make it out onto disk
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        File loggingOutput = new File("./target/serviceability/eventlog/eventlog.log");
        Assert.assertTrue("The log file " + loggingOutput.getAbsolutePath() + " does not exist.", loggingOutput.exists());

        BufferedReader reader = new BufferedReader(new FileReader(loggingOutput));

        String line = reader.readLine();
        List<String> logEntries = new ArrayList<>();

        while (line != null) {
            logEntries.add(line);
            line = reader.readLine();
        }
        
        reader.close();

        List<String> expectedResults = new ArrayList<>(10);
        expectedResults.add("KE0100W");
        expectedResults.add("KE0101I");

        for (String er : expectedResults) {
            boolean entryFound = false;
            for (String logEntry : logEntries) {
                if (logEntry.contains(er)) {
                    entryFound = true;
                }
            }
            Assert.assertTrue("An entry containing " + er + " was not found in logging file " + loggingOutput.getAbsolutePath() + ".", entryFound);
        }
    }
    
}
