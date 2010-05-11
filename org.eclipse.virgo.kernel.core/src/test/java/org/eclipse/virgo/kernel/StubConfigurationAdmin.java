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

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


public class StubConfigurationAdmin implements ConfigurationAdmin {
    
    private final Map<String, Configuration> configurations = new HashMap<String, Configuration>();

    public Configuration createFactoryConfiguration(String arg0) throws IOException {
        return createFactoryConfiguration(arg0, null);
    }

    public Configuration createFactoryConfiguration(String arg0, String arg1) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Configuration getConfiguration(String arg0) throws IOException {
        return getConfiguration(arg0, null);
    }

    public Configuration getConfiguration(String pid, String location) throws IOException {
        Configuration configuration = this.configurations.get(pid);
        if (configuration == null) {
            configuration = new StubConfiguration();
            configurations.put(pid, configuration);
        }        
        return configuration;
    }

    public Configuration[] listConfigurations(String arg0) throws IOException, InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }
}
