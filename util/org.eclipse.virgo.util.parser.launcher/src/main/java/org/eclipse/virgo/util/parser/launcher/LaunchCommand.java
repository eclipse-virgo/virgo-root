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

package org.eclipse.virgo.util.parser.launcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Simple value object encapsulating the command line arguments passed by the user.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe.
 */
public final class LaunchCommand {

    private final List<BundleEntry> bundleEntries = new ArrayList<BundleEntry>();

    private final Map<String, String> declaredProperties = new HashMap<String, String>();

    private Properties configProperties;
    
    private final List<String> unrecognizedArguments = new ArrayList<String>();

    public BundleEntry[] getBundleEntries() {
        return this.bundleEntries.toArray(new BundleEntry[this.bundleEntries.size()]);
    }

    public Map<String, String> getDeclaredProperties() {
        return new HashMap<String, String>(this.declaredProperties);
    }

    public Properties getConfigProperties() {
        return configProperties;
    }

    public List<String> getUnrecognizedArguments() {
    	return new ArrayList<String>(this.unrecognizedArguments);
    }
    
    void declareBundle(URI uri, boolean autoStart) {
    	this.bundleEntries.add(new BundleEntry(uri, autoStart));
    }
    
    void declareProperty(String key, String value) {
    	this.declaredProperties.put(key, value);
    }
    
    void setConfigProperties(Properties configPath) {
        this.configProperties = configPath;
    }
    
    void declareUnrecognizedArgument(String additionalArgument) {
    	this.unrecognizedArguments.add(additionalArgument);
    }    
}
