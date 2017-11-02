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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.internal.location.EquinoxLocations;
import org.eclipse.osgi.launch.Equinox;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.ExtensionsHookConfigurator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.FrameworkWiring;


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
        Map<String, String> configuration = populateConfiguration(config);        
        Equinox equinox = new Equinox(configuration);

        equinox.start();

        // install framework extension org.eclipse.osgi.compatibility.state
        equinox.getBundleContext().installBundle(
            "file:///" + new File(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored"
                + "/org.eclipse.osgi.compatibility.state/1.0.1.v20140709-1414/369f04450efbcb56a620a5f792bf477cfbfc5903"
                + "/org.eclipse.osgi.compatibility.state-1.0.1.v20140709-1414.jar").getAbsolutePath());

        // ...and refresh the System bundle to pick up the framework extension
        equinox.adapt(FrameworkWiring.class).resolveBundles(Collections.singletonList(equinox.getBundleContext().getBundle()));

        equinox.getBundleContext().registerService(Framework.class.getName(), equinox, null);

        return equinox;
    }

    private static Map<String, String> populateConfiguration(EquinoxLauncherConfiguration config) {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.putAll(config.getFrameworkProperties());

        // TODO - remove once all deprecated PlatformAdmin/State is reworked
        configuration.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT,
            "OSGi/Minimum-1.0,OSGi/Minimum-1.1,OSGi/Minimum-1.2,CDC-1.1/Foundation-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.5,JavaSE-1.6,JavaSE-1.7");

        mergeListProperty(configuration, HookRegistry.PROP_HOOK_CONFIGURATORS_INCLUDE, ExtensionsHookConfigurator.class.getName());

        configuration.put(PROP_OSGI_PARENT_CLASSLOADER, "fwk");
        configuration.put(PROP_CONTEXT_BOOTDELEGATION, "false");
        configuration.put(PROP_COMPATIBILITY_BOOTDELEGATION, "false");
        if (config.getProfilePath() != null) {
            configuration.put(EquinoxConfiguration.PROP_OSGI_JAVA_PROFILE, config.getProfilePath().toString());
        }
        configuration.put(EquinoxConfiguration.PROP_OSGI_JAVA_PROFILE_BOOTDELEGATION, EquinoxConfiguration.PROP_OSGI_BOOTDELEGATION_OVERRIDE);
        configuration.put(EquinoxLocations.PROP_CONFIG_AREA, config.getConfigPath().toString());
        configuration.put(EquinoxLocations.PROP_INSTALL_AREA, config.getInstallPath().toString());
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
