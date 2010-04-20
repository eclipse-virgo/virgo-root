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

package org.eclipse.virgo.util.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.launch.EquinoxFactory;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public abstract class AbstractEquinoxLaunchingTests {
    
    protected Framework framework;
    
    protected BundleContext bundleContext;
    
    private static final String PROP_CONTEXT_BOOTDELEGATION = "osgi.context.bootdelegation";
    
    private static final String PROP_COMPATIBILITY_BOOTDELEGATION = "osgi.compatibility.bootdelegation";
    
    private static final String PROP_OSGI_PARENT_CLASSLOADER = "osgi.parentClassloader";
    
    @Before
    public void launchEquinox() throws Exception {
        FrameworkFactory frameworkFactory = new EquinoxFactory();
        
        Map<String, String> config = new HashMap<String, String>();
        config.put("osgi.clean", "true");
        config.put("osgi.configuration.area", new File("target").getAbsolutePath());
        config.put("osgi.install.area", new File("target").getAbsolutePath());
        
        String systemPackages = getSystemPackages();
        
        if (systemPackages != null && systemPackages.length() > 0) {
            config.put("org.osgi.framework.system.packages", systemPackages);
        }
                        
        config.put("org.osgi.framework.bootdelegation", "com_cenqua_clover,com.cenqua.*");
        
        config.put(PROP_OSGI_PARENT_CLASSLOADER, "fwk");
        config.put(PROP_CONTEXT_BOOTDELEGATION, "false");
        config.put(PROP_COMPATIBILITY_BOOTDELEGATION, "false");
        
        this.framework = frameworkFactory.newFramework(config);
        this.framework.start();
        
        this.bundleContext = this.framework.getBundleContext();
    }
    
    @After
    public void shutdownEquinox() throws Exception {
        if (framework != null) {
            framework.stop();
        }
    }
    
    protected String getSystemPackages() {
        return null;
    }
}
