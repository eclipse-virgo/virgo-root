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

package org.eclipse.virgo.nano.config.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of {@link PropertiesSource} that exposes the {@link KernelConfiguration} as {@link Properties}.
 * <p />
 * Ideally the properties exposed from instances of this object should not be overridden by properties from another
 * source.
 * <p/>
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class KernelConfigurationPropertiesSource implements PropertiesSource {

    static final String PROPERTY_WORK_DIRECTORY = "work.directory";

    static final String PROPERTY_HOME_DIRECTORY = "home.directory";

    static final String PROPERTY_DOMAIN = "domain";

    static final String KERNEL_CONFIGURATION_PID = "org.eclipse.virgo.kernel";

    static final String PROPERTY_KERNEL_STARTUP_WAIT_LIMIT = "org.eclipse.virgo.kernel.startup.wait.limit";

    private final KernelConfiguration kernelConfiguration;

    public KernelConfigurationPropertiesSource(KernelConfiguration kernelConfiguration) {
        this.kernelConfiguration = kernelConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Properties> getConfigurationProperties() {
        return Collections.singletonMap(KERNEL_CONFIGURATION_PID, createProperties());
    }

    private Properties createProperties() {
        Properties properties = new Properties();
        properties.put(PROPERTY_DOMAIN, this.kernelConfiguration.getDomain());
        properties.put(PROPERTY_HOME_DIRECTORY, this.kernelConfiguration.getHomeDirectory().getAbsolutePath());
        properties.put(PROPERTY_WORK_DIRECTORY, this.kernelConfiguration.getWorkDirectory().getAbsolutePath());
        properties.put(PROPERTY_KERNEL_STARTUP_WAIT_LIMIT, Integer.toString(this.kernelConfiguration.getStartupWaitLimit()));
        return properties;
    }

}
