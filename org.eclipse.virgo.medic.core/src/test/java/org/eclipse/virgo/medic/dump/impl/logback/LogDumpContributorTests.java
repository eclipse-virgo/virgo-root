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

package org.eclipse.virgo.medic.dump.impl.logback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.impl.logback.LogDumpContributor;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import ch.qos.logback.classic.Logger;


public class LogDumpContributorTests {

    @Test
    public void emptyLog() throws DumpContributionFailedException {
    	LogDumpContributor contributor = getContributor();
        StubDump dump = getDump();
        contributor.contribute(dump);

        File logFile = dump.createFile("log.log");
        assertTrue(logFile.exists());
        assertEquals(0, logFile.length());
    }

    @Test
    public void nonEmptyLog() throws DumpContributionFailedException {
    	LogDumpContributor contributor = getContributor();
        StubDump dump = getDump();
        
        driveLogging(contributor);        

        contributor.contribute(dump);

        File logFile = dump.createFile("log.log");
        assertTrue(logFile.exists());
        assertEquals(1, countLines(logFile));
    }

    @Test
    public void wrappedLog() throws DumpContributionFailedException {
    	LogDumpContributor contributor = getContributor();
        StubDump dump = getDump();
        driveLogging(contributor);
        driveLogging(contributor);
        driveLogging(contributor);

        contributor.contribute(dump);

        File logFile = dump.createFile("log.log");
        assertTrue(logFile.exists());
        assertEquals(2, countLines(logFile));
    }

    private LogDumpContributor getContributor() {
    	LogDumpContributor contributor = new LogDumpContributor(new StaticConfigurationProvider());
    	return contributor;
     }

    private StubDump getDump() {
        File dumpRoot = new File("target/serviceability/dump");
        dumpRoot.mkdirs();
        return new StubDump("testCause", new Date().getTime(), Collections.<String, Object> emptyMap(), new Throwable[0], dumpRoot);
    }

    private int countLines(File logFile) {
        BufferedReader reader = null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(logFile));
            while (reader.readLine() != null) {
                count++;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        return count;
    }
    
    private static final class StaticConfigurationProvider implements ConfigurationProvider {
		@SuppressWarnings("unchecked")
		public Dictionary getConfiguration() {
			Hashtable configuration = new Hashtable();
			configuration.put(ConfigurationProvider.KEY_LOG_DUMP_LEVEL, "debug");
			configuration.put(ConfigurationProvider.KEY_LOG_DUMP_BUFFERSIZE, "2");
			return configuration;
		}    	
    }        
    
    private final void driveLogging(LogDumpContributor loggingContributor) {
    	loggingContributor.onLogging((Logger)LoggerFactory.getLogger("test"), "com.foo.bar", null, Level.INFO, "test", null, null);
    }
}
