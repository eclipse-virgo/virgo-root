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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.virgo.medic.log.LoggingConfiguration;

class StandardLoggingConfiguration implements LoggingConfiguration {

    private final String configuration;

    private final String name;

    StandardLoggingConfiguration(URL configuration, String name) throws IOException {
        this(readConfiguration(configuration), name);
    }

    StandardLoggingConfiguration(File configuration, String name) throws IOException {
        this(readConfiguration(configuration), name);
    }

    private StandardLoggingConfiguration(String configuration, String name) {
        this.configuration = configuration;
        this.name = name;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public String getName() {
        return this.name;
    }

    private static String readConfiguration(URL configURL) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configURL.openStream(), UTF_8))) {
            return readConfiguration(reader);
        }
    }

    private static String readConfiguration(File configFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), UTF_8))) {
            return readConfiguration(reader);
        }
    }

    private static String readConfiguration(BufferedReader reader) throws IOException {
        StringBuilder configurationBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            configurationBuilder.append(line);
        }
        return configurationBuilder.toString();
    }
}
