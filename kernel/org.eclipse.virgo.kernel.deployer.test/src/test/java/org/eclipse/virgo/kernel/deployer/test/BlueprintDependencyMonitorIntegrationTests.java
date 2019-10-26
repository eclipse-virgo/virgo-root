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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("TODO - Check why the eventlog doesn't contain the blueprint messages...")
public class BlueprintDependencyMonitorIntegrationTests extends AbstractDeployerIntegrationTest {
    
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
        List<String> logMessages = new ArrayList<>();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
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

    @Test
    public void testBlueprintDependencyMonitoring() throws Exception {
        this.deployer.deploy(new File("src/test/resources/QuickConsumerBlueprint.jar").toURI());
        
        // We need to sleep for a little while to give the
        // log output sufficient time to make it out onto disk
        try {
            Thread.sleep(21000);
        } catch (InterruptedException ignored) {
        }
        
        assertEquals("One KE0100W message was expected", 1, findLogMessages("<KE0100W>").size());
        assertEquals("One KE0101I message was expected", 1, findLogMessages("<KE0101I>").size());
    }

}
