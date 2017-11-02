/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.security.openejb.classloading.hook;

import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;

//Applicable only for org.apache.openejb.core
public class OpenEjbEmptyProtectionDomainClassloadingHook extends ClassLoaderHook implements HookConfigurator {

    // Equinox implicitly creates a ProtectionDomain for each bundle with all permissions.
    // Openejb does security checks related to security manager with its own protection domain which is not the app
    // protection domain in OSGi case
    @Override
    public ModuleClassLoader createClassLoader(ClassLoader parent, EquinoxConfiguration configuration, BundleLoader delegate, Generation generation) {
        ProtectionDomain processedProtectionDomain = generation.getDomain();

        if (processedProtectionDomain == null && generation.getRevision().getSymbolicName().equals("org.apache.openejb.core")) {
            PermissionCollection emptyPermissionCollection = (new AllPermission()).newPermissionCollection();
            processedProtectionDomain = new ProtectionDomain(null, emptyPermissionCollection);
        }
        return new EquinoxClassLoader(parent, configuration, delegate, generation);
    }

    @Override
    public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries, String cp, ClasspathManager hostmanager, Generation sourceGeneration) {
        return false;
    }

    @Override
    public void addHooks(HookRegistry hookRegistry) {
        hookRegistry.addClassLoaderHook(this);
    }
}
