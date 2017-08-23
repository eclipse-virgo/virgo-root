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

package org.eclipse.virgo.medic.log.impl.logback;

import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.impl.logback.JoranLoggerContextConfigurer;
import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurationFailedException;
import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurer;
import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;


public class JoranLoggerContextConfigurerTests {

    private final LoggerContextConfigurer configurer = new JoranLoggerContextConfigurer();

    @Test(expected = LoggerContextConfigurationFailedException.class)
    public void malformedXML() throws LoggerContextConfigurationFailedException {
        LoggingConfiguration configuration = new StubLoggingConfiguration("dslkjgw", "the-config");
        configurer.applyConfiguration(configuration, new LoggerContext());
    }

    @Test(expected = LoggerContextConfigurationFailedException.class)
    public void malformedConfiguration() throws LoggerContextConfigurationFailedException {
        LoggingConfiguration configuration = new StubLoggingConfiguration("<configuration><appender/></configuration>", "the-config");
        configurer.applyConfiguration(configuration, new LoggerContext());
    }

    @Test
    public void validConfiguration() throws LoggerContextConfigurationFailedException {
        LoggingConfiguration configuration = new StubLoggingConfiguration("<configuration><logger name=\"abc\"></logger></configuration>",
            "the-config");
        LoggerContext loggerContext = new LoggerContext();
        configurer.applyConfiguration(configuration, loggerContext);
        assertNotNull(loggerContext.getLogger("abc"));
    }

    private static final class StubLoggingConfiguration implements LoggingConfiguration {

        private final String configuration;

        private final String name;

        private StubLoggingConfiguration(String configuration, String name) {
            this.configuration = configuration;
            this.name = name;
        }

        public String getConfiguration() {
            return this.configuration;
        }

        public String getName() {
            return this.name;
        }
    }
}
