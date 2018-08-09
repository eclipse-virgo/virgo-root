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

package org.eclipse.virgo.kernel.equinox.extensions.hooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.launch.Equinox;
import org.eclipse.virgo.kernel.equinox.extensions.EquinoxLauncherConfiguration;
import org.eclipse.virgo.kernel.equinox.extensions.ExtendedEquinoxLauncher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;


public class PluggableClassLoadingHookTests {

    private Equinox osgi;

    private BundleContext context;

    @Before
    public void setUp() throws BundleException {
        EquinoxLauncherConfiguration config = new EquinoxLauncherConfiguration();
        config.setConfigPath(new File("build/config").toURI());
        config.setInstallPath(new File("build/install").toURI());

        this.osgi = ExtendedEquinoxLauncher.launch(config);
        this.context = osgi.getBundleContext();
    }
    
    @After
    public void after() throws BundleException {
        if(this.osgi != null) {
            this.osgi.stop();
        }
    }

    @Test
    public void testAddClassLoaderCreator() throws Exception {
        final List<BaseData> baseDatas = new ArrayList<>();
        
        ClassLoaderCreator creator = new ClassLoaderCreator() {

            public BaseClassLoader createClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, BundleProtectionDomain domain, BaseData data,
                String[] bundleclasspath) {
                baseDatas.add(data);
                return null;
            }

        };
        
        PluggableClassLoadingHook.getInstance().setClassLoaderCreator(creator);
        this.context.registerService(ClassLoaderCreator.class.getName(), creator, null);
        
        Bundle b = this.context.installBundle(new File("src/test/resources/hooks/classloading/bundle").toURI().toString());
        try {
            b.loadClass("foo");
            fail("shouldn't be able to load foo!");
        } catch (ClassNotFoundException e) {
            // expected
            assertEquals(1, baseDatas.size());
            assertEquals("hooks.classloading", baseDatas.get(0).getSymbolicName());
        }
    }
    
    @Test
    public void testBundleClassLoaderParent() {
        PluggableClassLoadingHook hook = PluggableClassLoadingHook.getInstance();
        
        assertNull(hook.getBundleClassLoaderParent());
        
        ClassLoader classLoader = getClass().getClassLoader();
        hook.setBundleClassLoaderParent(classLoader);
        assertEquals(classLoader, hook.getBundleClassLoaderParent());
    }
}
