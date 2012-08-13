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

package org.eclipse.virgo.kernel.equinox.extensions;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 */
public final class EquinoxLauncherConfiguration {

    private final Map<String, String> frameworkProperties = new HashMap<String, String>();
    
    private URI profilePath;

    private URI installPath;

    private URI configPath;

    private boolean clean = false;

    public URI getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(URI profilePath) {
        this.profilePath = profilePath;
    }

    public URI getInstallPath() {
        return installPath;
    }

    public void setInstallPath(URI installPath) {
        this.installPath = installPath;
    }

    public URI getConfigPath() {
        return configPath;
    }

    public void setConfigPath(URI configPath) {
        this.configPath = configPath;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void setFrameworkProperty(String key, String value) {
        this.frameworkProperties.put(key, value);
    }
    
    public Map<String, String> getFrameworkProperties() {
        return this.frameworkProperties;
    }
}
