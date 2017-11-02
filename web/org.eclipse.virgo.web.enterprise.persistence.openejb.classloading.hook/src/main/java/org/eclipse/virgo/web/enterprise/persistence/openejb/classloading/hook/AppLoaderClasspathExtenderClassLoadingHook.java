/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.persistence.openejb.classloading.hook;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.osgi.storage.bundlefile.BundleEntry;

public class AppLoaderClasspathExtenderClassLoadingHook extends ClassLoaderHook implements HookConfigurator {

    File persistenceIntegrationJar = null;

    private static final String FILE_SCHEME = "file:";

    private static final String WEB_CONTEXTPATH_HEADER = "Web-ContextPath";

    private static final Logger logger = Logger.getLogger(
        "com.sap.core.service.accessor.persistence.classloading.hook.AppLoaderClasspathExtenderClassLoadingHook");

    private static final String PERSISTENCE_INTEGRATION_JAR_PROP_NAME = "persistence.integration.jar.name";

    private static final String PERSISTENCE_INTEGRATION_JAR = System.getProperty(PERSISTENCE_INTEGRATION_JAR_PROP_NAME);

    private static final String CONFIG_AREA = "osgi.configuration.area";

    private static final String LIB_DIR = "lib";

    private static final String PERSISTENCE_DIR = "persistence";

    @Override
    public void addHooks(HookRegistry registry) {
        registry.addClassLoaderHook(this);
    }

    @Override
    public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries, String cp, ClasspathManager hostmanager, Generation sourceGeneration) {
        if (isAppBundle(sourceGeneration) && (shouldAdd(cpEntries))) {
            ClasspathEntry persistenceIntegrationClasspathEntry = null;
            try {
                persistenceIntegrationClasspathEntry = determinePersistenceIntegrationPath(hostmanager, sourceGeneration);
            } catch (ClasspathExtenderClassLoadingHookException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if (persistenceIntegrationClasspathEntry != null) {
                cpEntries.add(persistenceIntegrationClasspathEntry);
                return true;
            }
        }
        return false;
    }

    boolean shouldAdd(ArrayList<ClasspathEntry> cpEntries) {
        for (ClasspathEntry cpEntry : cpEntries) {
            if (cpEntry.getBundleFile().getBaseFile().getName().startsWith(PERSISTENCE_INTEGRATION_JAR)) {
                return false;
            }
        }
        return true;
    }

    ClasspathEntry determinePersistenceIntegrationPath(ClasspathManager hostmanager, Generation sourcedata)
        throws ClasspathExtenderClassLoadingHookException {
        if (persistenceIntegrationJar == null) {
            findPersistenceIntegrationJar();
        }

        ClasspathEntry cp = hostmanager.getExternalClassPath(persistenceIntegrationJar.getAbsolutePath(), sourcedata);
        if (cp == null) {
            throw new ClasspathExtenderClassLoadingHookException("Failed to create classpath entry for file [" + PERSISTENCE_INTEGRATION_JAR + "]");
        }

        return cp;
    }

    void findPersistenceIntegrationJar() throws ClasspathExtenderClassLoadingHookException {
        String configurationPath = System.getProperty(CONFIG_AREA);
        if (configurationPath == null) {
            throw new ClasspathExtenderClassLoadingHookException("Property [" + CONFIG_AREA + "] is missing");
        }
        File configurationFile = new File(normalize(configurationPath));
        File lib = new File(configurationFile.getParentFile(), LIB_DIR);
        if (!lib.exists()) {
            throw new ClasspathExtenderClassLoadingHookException("lib folder is missing");
        }

        File persistenceLibDir = new File(lib, PERSISTENCE_DIR);
        if (!persistenceLibDir.exists()) {
            throw new ClasspathExtenderClassLoadingHookException("lib/persistence folder is missing");
        }

        String[] libs = persistenceLibDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.startsWith(PERSISTENCE_INTEGRATION_JAR)) {
                    return true;
                }
                return false;
            }
        });

        if (libs.length == 0) {
            throw new ClasspathExtenderClassLoadingHookException(
                "No file with name starting with [" + PERSISTENCE_INTEGRATION_JAR + "] was found in lib/persistence folder");
        }

        if (libs.length > 1) {
            logger.log(Level.SEVERE, "Found " + libs.length + " files with name starting with [" + PERSISTENCE_INTEGRATION_JAR
                + "] was found in lib/persistence folder (one expected); choosing [" + libs[0] + "]");
        }

        persistenceIntegrationJar = new File(persistenceLibDir, libs[0]);

    }

    String normalize(String filePath) {
        if (filePath.startsWith(FILE_SCHEME)) {
            return filePath.substring(FILE_SCHEME.length());
        }
        return filePath;
    }

    boolean isAppBundle(Generation sourcedata) {
        // TODO - review this change
        if (sourcedata.getHeaders().get(WEB_CONTEXTPATH_HEADER) != null) {
            return true;
        }
        return false;
    }

    @Override
    public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager classpathManager) {
        return null;
    }

}
