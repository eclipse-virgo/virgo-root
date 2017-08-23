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

import java.io.File;

import org.junit.Test;

import org.eclipse.virgo.medic.log.ConfigurationPublicationFailedException;
import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
import org.eclipse.virgo.medic.log.impl.config.StandardLoggingConfigurationPublisher;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class StandardLoggingConfigurationPublisherTests {

    private final StubBundleContext bundleContext = new StubBundleContext();

    private final LoggingConfigurationPublisher publisher = new StandardLoggingConfigurationPublisher(this.bundleContext);

    @Test(expected = ConfigurationPublicationFailedException.class)
    public void failedPublication() throws ConfigurationPublicationFailedException {
        publisher.publishConfiguration(new File("does/not/exist"), "foo");
    }

    @Test
    public void publication() throws ConfigurationPublicationFailedException {
        this.publisher.publishConfiguration(new File("src/test/resources/logback.xml"), "foo");
    }
}
