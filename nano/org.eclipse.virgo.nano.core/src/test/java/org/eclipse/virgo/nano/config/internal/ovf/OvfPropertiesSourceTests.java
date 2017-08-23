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

package org.eclipse.virgo.nano.config.internal.ovf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import org.eclipse.virgo.nano.config.internal.ovf.OvfPropertiesSource;
import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 */
public class OvfPropertiesSourceTests {

    @Test
    public void testReadValidFile() {
        MockEventLogger logger = new MockEventLogger();

        StubBundleContext context = new StubBundleContext();
        context.addProperty(OvfPropertiesSource.FRAMEWORK_PROPERTY_OVF, "src/test/resources/ovf/valid.xml");

        OvfPropertiesSource source = new OvfPropertiesSource(context, logger);
        Map<String, Properties> properties = source.getConfigurationProperties();
        assertNotNull(properties);

        Properties one = properties.get("one");
        assertNotNull(one);
        assertEquals("bar", one.getProperty("foo"));
        assertEquals("baz", one.getProperty("bar"));

        Properties two = properties.get("two");
        assertNotNull(two);
        assertEquals("quux", two.getProperty("baz"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadFileWithInvalidProperty() {
        MockEventLogger logger = new MockEventLogger();

        StubBundleContext context = new StubBundleContext();
        context.addProperty(OvfPropertiesSource.FRAMEWORK_PROPERTY_OVF, "src/test/resources/ovf/invalid.xml");

        OvfPropertiesSource source = new OvfPropertiesSource(context, logger);
        source.getConfigurationProperties();
    }

    @Test
    public void testReadNonExistentFile() {
        MockEventLogger logger = new MockEventLogger();

        StubBundleContext context = new StubBundleContext();
        context.addProperty(OvfPropertiesSource.FRAMEWORK_PROPERTY_OVF, "src/test/resources/ovf/nonexistent.xml");

        OvfPropertiesSource source = new OvfPropertiesSource(context, logger);
        source.getConfigurationProperties();

        List<LoggedEvent> loggedEvents = logger.getLoggedEvents();
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);
        assertEquals(KernelLogEvents.OVF_CONFIGURATION_FILE_DOES_NOT_EXIST.getEventCode(), loggedEvent.getCode());
    }
}
