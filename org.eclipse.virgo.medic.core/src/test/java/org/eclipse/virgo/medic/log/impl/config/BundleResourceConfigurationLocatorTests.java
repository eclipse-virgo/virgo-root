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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.impl.config.BundleResourceConfigurationLocator;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

public class BundleResourceConfigurationLocatorTests {

    private final BundleResourceConfigurationLocator locator = new BundleResourceConfigurationLocator();

    private final StubBundle bundle = new StubBundle("foo", new Version(1, 2, 3));

    @Test
    public void configFromBundle() throws MalformedURLException {
        this.bundle.addResource("logback.xml", new URL("file:src/test/resources/logback.xml"));
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertNotNull(configuration);
        assertEquals("foo_1.2.3", configuration.getName());
    }

    @Test
    public void defaultConfigFromBundle() throws MalformedURLException {
        this.bundle.addResource("logback-default.xml", new URL("file:src/test/resources/logback.xml"));
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertNotNull(configuration);
        assertEquals("foo_1.2.3", configuration.getName());
    }

    @Test
    public void noConfigInBundle() {
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertNull(configuration);
    }
}
