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

import java.io.IOException;
import java.net.URL;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.osgi.framework.Bundle;


public final class BundleResourceConfigurationLocator implements ConfigurationLocator {

    public LoggingConfiguration locateConfiguration(Bundle bundle) {
        if (bundle != null) {
            URL logBackConfigurationEntry = bundle.getResource("logback.xml");

            if (logBackConfigurationEntry == null) {
                logBackConfigurationEntry = bundle.getResource("logback-default.xml");
            }

            if (logBackConfigurationEntry != null) {
                try {
                    return new StandardLoggingConfiguration(logBackConfigurationEntry, createContextName(bundle));
                } catch (IOException ioe) {
                    // TODO Exception handling
                }
            }
        }

        return null;
    }

    private static String createContextName(Bundle bundle) {
        return bundle.getSymbolicName() + "_" + bundle.getVersion();
    }
}
