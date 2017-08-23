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

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.osgi.framework.Bundle;


public final class CompositeConfigurationLocator implements ConfigurationLocator {

    private final List<ConfigurationLocator> configurationLocators;

    public CompositeConfigurationLocator(ConfigurationLocator... configurationLocators) {
        this.configurationLocators = Arrays.asList(configurationLocators);
    }

    public LoggingConfiguration locateConfiguration(Bundle bundle) {
        for (ConfigurationLocator configurationLocator : this.configurationLocators) {
            LoggingConfiguration configuration = configurationLocator.locateConfiguration(bundle);
            if (configuration != null) {
                return configuration;
            }
        }
        return null;
    }
}
