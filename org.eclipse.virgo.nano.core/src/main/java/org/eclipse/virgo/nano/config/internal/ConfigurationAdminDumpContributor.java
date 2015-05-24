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

import static org.eclipse.virgo.util.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;

public class ConfigurationAdminDumpContributor implements DumpContributor {

    private static final String PROPERTY_PATTERN = "%s:\t%s\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConfigurationAdmin configurationAdmin;

    public ConfigurationAdminDumpContributor(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public void contribute(Dump dump) throws DumpContributionFailedException {
        StringBuilder sb = new StringBuilder();

        try {
            for (Configuration configuration : configurationAdmin.listConfigurations(null)) {
                appendHeader(sb, configuration.getPid());
                appendProperties(sb, configuration.getProperties());
                appendFooter(sb);
            }
        } catch (Exception e) {
            logger.warn("Could not enumerate existing configurations");
        }

        Writer out = null;
        try {
            out = dump.createFileWriter("configurationAdmin.properties");
            out.write(sb.toString());
        } catch (IOException e) {
            logger.warn("Could not write configurationAdmin dump");
        } finally {
            closeQuietly(out);
        }
    }

    public String getName() {
        return "configurationAdmin";
    }

    private void appendHeader(StringBuilder sb, String pid) {
        for (int i = 0; i < pid.length() + 4; i++) {
            sb.append("#");
        }
        sb.append("\n# ").append(pid).append(" #\n");
        for (int i = 0; i < pid.length() + 4; i++) {
            sb.append("#");
        }
        sb.append("\n\n");
    }

    private void appendProperties(StringBuilder sb, Dictionary<String, Object> properties) {
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            appendProperty(sb, key, properties.get(key));
        }
    }

    private void appendProperty(StringBuilder sb, Object key, Object value) {
        sb.append(String.format(PROPERTY_PATTERN, key, value));
    }

    private void appendFooter(StringBuilder sb) {
        sb.append("\n\n");
    }

}
