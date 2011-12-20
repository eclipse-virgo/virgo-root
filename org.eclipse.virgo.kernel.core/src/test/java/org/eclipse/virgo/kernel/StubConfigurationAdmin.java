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

package org.eclipse.virgo.kernel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class StubConfigurationAdmin implements ConfigurationAdmin {

    private final AtomicInteger factoryConfigurationCounter = new AtomicInteger(0);
    private final Map<String, Configuration> configurations = new HashMap<String, Configuration>();

    public Configuration createFactoryConfiguration(String arg0) throws IOException {
        return createFactoryConfiguration(arg0, null);
    }

    public Configuration createFactoryConfiguration(String arg0, String arg1) throws IOException {
        final String pid = arg0 + "-" + System.currentTimeMillis() + "-" + factoryConfigurationCounter.incrementAndGet();
        return getConfiguration(pid, arg1, arg0);
    }

    public Configuration getConfiguration(String arg0) throws IOException {
        return getConfiguration(arg0, null);
    }

    public Configuration getConfiguration(String pid, String location) throws IOException {
        return getConfiguration(pid, location, null);
    }

    public Configuration[] listConfigurations(String arg0) throws IOException, InvalidSyntaxException {
        if (arg0 == null) {
            return configurations.values().toArray(new Configuration[configurations.values().size()]);
        }
        throw new UnsupportedOperationException("only support 'null' filter in stub");
    }

    private Configuration getConfiguration(String pid, String location, String factoryPid) {
        Configuration configuration = this.configurations.get(pid);
        if (configuration == null) {
            configuration = new StubConfiguration(pid, factoryPid);
            configurations.put(pid, configuration);
        }
        return configuration;
    }
}
