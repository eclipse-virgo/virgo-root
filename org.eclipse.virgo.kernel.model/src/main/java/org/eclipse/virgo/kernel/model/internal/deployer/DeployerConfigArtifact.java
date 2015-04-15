/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.deployer;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.install.artifact.ConfigInstallArtifact;
import org.osgi.framework.BundleContext;


/**
 * {@link DeployerConfigArtifact} is a {@link DeployerArtifact} that understands
 * a configuration install artifact's properties.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class DeployerConfigArtifact extends DeployerArtifact {

    private final ConfigInstallArtifact configInstallArtifact;

    public DeployerConfigArtifact(BundleContext bundleContext, ConfigInstallArtifact configInstallArtifact, Region region) {
        super(bundleContext, configInstallArtifact, region);
        this.configInstallArtifact = configInstallArtifact;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>(super.getProperties());
        try {
            Properties props = this.configInstallArtifact.getProperties();
            Enumeration<Object> keys = props.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                if (key instanceof String) {
                    Object value = props.get(key);
                    if (value instanceof String) {
                        properties.put((String)key, (String)value);
                    }
                }
            }
        } catch (IOException ignored) {
            // Default to superclass behaviour
        }
        return properties;
    }

   
}
