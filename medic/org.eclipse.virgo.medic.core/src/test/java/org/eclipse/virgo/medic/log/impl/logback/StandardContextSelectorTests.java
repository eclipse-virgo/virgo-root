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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;


import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.impl.CallingBundleResolver;
import org.eclipse.virgo.medic.log.impl.config.ConfigurationLocator;
import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurationFailedException;
import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurer;
import org.eclipse.virgo.medic.log.impl.logback.StandardContextSelectorDelegate;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

public class StandardContextSelectorTests {

    private CallingBundleResolver loggingCallerLocator = createMock(CallingBundleResolver.class);

    private ConfigurationLocator configurationLocator = createMock(ConfigurationLocator.class);

    private StubBundle bundle = new StubBundle();

    private LoggerContextConfigurer loggerContextConfigurer = createMock(LoggerContextConfigurer.class);

    private StandardContextSelectorDelegate contextSelectorDelegate = new StandardContextSelectorDelegate(this.loggingCallerLocator,
        this.configurationLocator, this.bundle, this.loggerContextConfigurer);

    @Test
    public void loggerContextWithLocatedConfiguration() throws LoggerContextConfigurationFailedException {
        LoggingConfiguration loggingConfiguration = createMock(LoggingConfiguration.class);
        expect(loggingConfiguration.getName()).andReturn("the-configuration").atLeastOnce();
        expect(this.loggingCallerLocator.getCallingBundle()).andReturn(this.bundle).times(2);
        expect(this.configurationLocator.locateConfiguration(this.bundle)).andReturn(loggingConfiguration).times(1);
        this.loggerContextConfigurer.applyConfiguration(eq(loggingConfiguration), isA(LoggerContext.class));
        replay(this.configurationLocator, this.loggingCallerLocator, this.loggerContextConfigurer, loggingConfiguration);

        LoggerContext loggerContext = this.contextSelectorDelegate.getLoggerContext();
        assertEquals("the-configuration", loggerContext.getName());

        LoggerContext reusedLoggerContext = this.contextSelectorDelegate.getLoggerContext();
        assertEquals("the-configuration", reusedLoggerContext.getName());

        assertSame(loggerContext, reusedLoggerContext);

        List<String> contextNames = this.contextSelectorDelegate.getContextNames();
        assertEquals(1, contextNames.size());
        assertEquals("the-configuration", contextNames.get(0));

        loggerContext = this.contextSelectorDelegate.getLoggerContext("the-configuration");
        assertNotNull(loggerContext);
        assertEquals("the-configuration", loggerContext.getName());

        verify(this.configurationLocator, this.loggingCallerLocator, this.loggerContextConfigurer, loggingConfiguration);
    }

    @Test
    public void loggerContextWithNoLocatedConfiguration() {
        expect(this.loggingCallerLocator.getCallingBundle()).andReturn(this.bundle);
        expect(this.configurationLocator.locateConfiguration(this.bundle)).andReturn(null);
        replay(this.configurationLocator, this.loggingCallerLocator);

        LoggerContext loggerContext = this.contextSelectorDelegate.getLoggerContext();
        assertNull(loggerContext);
        verify(this.configurationLocator, this.loggingCallerLocator);
    }
}
