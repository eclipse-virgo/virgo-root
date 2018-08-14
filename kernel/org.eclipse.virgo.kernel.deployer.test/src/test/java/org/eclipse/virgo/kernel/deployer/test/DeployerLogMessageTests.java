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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.test.framework.ConfigLocation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


@Ignore
@ConfigLocation("META-INF/no.heap.dump.test.config.properties")
public class DeployerLogMessageTests extends AbstractParTests {

    private static final File LOG_FILE = new File("build/serviceability/eventlog/eventlog.log");

    private int existingLines;

    @Before
    public void countExistingLogLines() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE));
        String line = reader.readLine();
        existingLines = 0;
        while (line != null) {
            existingLines++;
            line = reader.readLine();
        }
        reader.close();
    }

    private List<String> findLogMessages(String logCode) throws IOException {
        List<String> logMessages = new ArrayList<String>();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {

        }

        BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE));
        String line;
        int lines = 0;
        while ((line = reader.readLine()) != null) {
            lines++;
            if (lines > existingLines) {
                int index = line.indexOf(logCode);
                if (index > -1) {
                    logMessages.add(line.substring(index));
                }
            }
        }
        reader.close();
        return logMessages;
    }

    @Test(expected = DeploymentException.class)
    public void unsatisfiablePackageImport() throws Throwable {
        try {
            deploy(new File("src/test/resources/deployer-log-message-tests/missing-package.par"));
        } finally {
            List<String> logMessages = findLogMessages("<" + DeployerLogEvents.INSTALL_FAILURE.getEventCode() + ">");
            assertTrue(logMessages.size() == 1);
        }
    }

    @Test(expected = DeploymentException.class)
    public void unsatisfiableBundleImport() throws Throwable {
        try {
            deploy(new File("src/test/resources/deployer-log-message-tests/missing-bundle.par"));
        } finally {
            List<String> logMessages = findLogMessages("<" + DeployerLogEvents.INSTALL_FAILURE.getEventCode() + ">");
            assertTrue(logMessages.size() == 1);
        }
    }

    @Test(expected = DeploymentException.class)
    public void unsatisfiableLibraryImport() throws Throwable {
        try {
            deploy(new File("src/test/resources/deployer-log-message-tests/missing-library.par"));
        } finally {
            List<String> logMessages = findLogMessages("<" + DeployerLogEvents.INSTALL_FAILURE.getEventCode() + ">");
            assertTrue(logMessages.size() == 1);
        }
    }

    @Test(expected = DeploymentException.class)
    public void malformedApplicationContext() throws Throwable {
        try {
            deploy(new File("src/test/resources/deployer-log-message-tests/malformed-application-context.par"));
        } finally {
            List<String> logMessages = findLogMessages("<" + DeployerLogEvents.CONFIG_FILE_ERROR.getEventCode() + ">");
            assertEquals(1, logMessages.size());
            assertTrue(logMessages.get(0).contains("my.bundle"));
        }
    }

    @Test(expected = DeploymentException.class)
    public void classNotFoundException() throws Throwable {
        try {
            deploy(new File("src/test/resources/deployer-log-message-tests/ClassNotFoundException.jar"));
        } finally {
            List<String> logMessages = findLogMessages("<AG0000E>");
            assertTrue(logMessages.size() >= 1);
            assertTrue(logMessages.get(0).contains("com.does.not.exist.NothingToSeeHere"));
        }
    }
}
