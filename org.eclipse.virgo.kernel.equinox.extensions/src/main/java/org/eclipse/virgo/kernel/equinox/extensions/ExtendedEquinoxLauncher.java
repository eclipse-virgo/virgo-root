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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.launch.Equinox;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.ExtensionsHookConfigurator;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;


/**
 */
public final class ExtendedEquinoxLauncher {

    private static final String PROP_CONTEXT_BOOTDELEGATION = "osgi.context.bootdelegation";
    private static final String PROP_COMPATIBILITY_BOOTDELEGATION = "osgi.compatibility.bootdelegation";

    /**
     * Equinox-specific property to control clean startup.
     */
    private static final String PROP_OSGI_CLEAN = "osgi.clean";

    /**
     * Equinox-specific property for setting the parent <code>ClassLoader</code>.
     */
    private static final String PROP_OSGI_PARENT_CLASSLOADER = "osgi.parentClassloader";

    public static Equinox launch(EquinoxLauncherConfiguration config) throws BundleException {
        try {
            Field f = FrameworkProperties.class.getDeclaredField("properties");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception e) {
            System.out.println("Unable to reset Equinox FrameworkProperties");
            e.printStackTrace(System.out);
        }
        
        Map<String, String> configuration = populateConfiguration(config);        
        Equinox equinox = new Equinox(configuration);      
        equinox.start();
        equinox.getBundleContext().registerService(Framework.class.getName(), equinox, null);

        return equinox;
    }

    private static Map<String, String> populateConfiguration(EquinoxLauncherConfiguration config) {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.putAll(config.getFrameworkProperties());

        mergeListProperty(configuration, HookRegistry.PROP_HOOK_CONFIGURATORS_INCLUDE, ExtensionsHookConfigurator.class.getName());

        configuration.put(PROP_OSGI_PARENT_CLASSLOADER, "fwk");
        configuration.put(PROP_CONTEXT_BOOTDELEGATION, "false");
        configuration.put(PROP_COMPATIBILITY_BOOTDELEGATION, "false");
        if (config.getProfilePath() != null) {
            configuration.put(Constants.OSGI_JAVA_PROFILE, config.getProfilePath().toString());
        }
        configuration.put(Constants.OSGI_JAVA_PROFILE_BOOTDELEGATION, Constants.OSGI_BOOTDELEGATION_OVERRIDE);
        configuration.put(LocationManager.PROP_CONFIG_AREA, config.getConfigPath().toString());
        configuration.put(LocationManager.PROP_INSTALL_AREA, config.getInstallPath().toString());
        configuration.put(PROP_OSGI_CLEAN, Boolean.toString(config.isClean()));
        return configuration;
    }
    
    private static void mergeListProperty(Map<String, String> properties, String key, String value) {
        String existingValue = properties.get(key);
        if (existingValue != null) {
            properties.put(key, existingValue + "," + value);
        } else {
            properties.put(key, value);
        }
    }
}
