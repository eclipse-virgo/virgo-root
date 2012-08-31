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

package org.eclipse.virgo.shell.internal.formatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;

public final class ConfigInstallArtifactCommandFormatter extends AbstractInstallArtifactCommandFormatter<ManageableArtifact> {

    private final ConfigurationAdmin configurationAdmin;

    public ConfigInstallArtifactCommandFormatter(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public List<String> formatExamine(ManageableArtifact artifact) {
        List<String> lines = new ArrayList<String>();

        Configuration configuration;
        try {
            configuration = this.configurationAdmin.getConfiguration(artifact.getName(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String factoryPid = configuration.getFactoryPid();
        String bundleLocation = configuration.getBundleLocation();

        lines.add(String.format("Factory pid:     %s", factoryPid == null ? "" : factoryPid));
        lines.add(String.format("Bundle Location: %s", bundleLocation == null ? "" : bundleLocation));

        lines.addAll(formatProperties(configuration));

        return lines;
    }

    private List<String> formatProperties(Configuration configuration) {
        List<String> lines = new ArrayList<String>();
        List<String> propertyKeys = new ArrayList<String>();

        Dictionary<String, Object> properties = configuration.getProperties();
        if (properties != null) {
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                propertyKeys.add(keys.nextElement());
            }
            Collections.sort(propertyKeys);

            lines.add("");
            lines.add(String.format("Properties:"));
            for (String propertyKey : propertyKeys) {
                lines.add(String.format("    %s:", propertyKey));

                List<String> values = PropertyFormatter.formatPropertyValue(properties.get(propertyKey), MAX_LINE_LENGTH);
                for (String value : values) {
                    lines.add(String.format("        %s", value));
                }
            }
        }

        return lines;
    }

}
