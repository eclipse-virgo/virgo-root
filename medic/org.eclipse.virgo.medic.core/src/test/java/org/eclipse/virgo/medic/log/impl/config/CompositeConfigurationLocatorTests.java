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

package org.eclipse.virgo.medic.log.impl.config;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.impl.config.CompositeConfigurationLocator;
import org.eclipse.virgo.medic.log.impl.config.ConfigurationLocator;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

public class CompositeConfigurationLocatorTests {

    @Test
    public void configurationFromFirstLocator() {
        ConfigurationLocator locator1 = createMock(ConfigurationLocator.class);
        ConfigurationLocator locator2 = createMock(ConfigurationLocator.class);

        ConfigurationLocator compositeLocator = new CompositeConfigurationLocator(locator1, locator2);

        StubBundle bundle = new StubBundle();

        LoggingConfiguration loggingConfiguration = createMock(LoggingConfiguration.class);

        expect(locator1.locateConfiguration(bundle)).andReturn(loggingConfiguration);
        replay(locator1, locator2);

        assertEquals(loggingConfiguration, compositeLocator.locateConfiguration(bundle));

        verify(locator1, locator2);
    }

    @Test
    public void configurationFromSubsequentLocator() {
        ConfigurationLocator locator1 = createMock(ConfigurationLocator.class);
        ConfigurationLocator locator2 = createMock(ConfigurationLocator.class);

        ConfigurationLocator compositeLocator = new CompositeConfigurationLocator(locator1, locator2);

        StubBundle bundle = new StubBundle();

        LoggingConfiguration loggingConfiguration = createMock(LoggingConfiguration.class);

        expect(locator1.locateConfiguration(bundle)).andReturn(null);
        expect(locator2.locateConfiguration(bundle)).andReturn(loggingConfiguration);
        replay(locator1, locator2);

        assertEquals(loggingConfiguration, compositeLocator.locateConfiguration(bundle));

        verify(locator1, locator2);
    }
}
