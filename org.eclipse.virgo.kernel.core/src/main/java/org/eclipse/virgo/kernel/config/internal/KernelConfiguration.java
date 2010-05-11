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

package org.eclipse.virgo.kernel.config.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;

public final class KernelConfiguration {

    static final String PROPERTY_KERNEL_CONFIG = "org.eclipse.virgo.kernel.config";

    static final String PROPERTY_KERNEL_HOME = "org.eclipse.virgo.kernel.home";

    static final String PROPERTY_KERNEL_DOMAIN = "org.eclipse.virgo.kernel.domain";

    private static final String DEFAULT_WORK_DIRECTORY_NAME = "work";

    private static final String DEFAULT_CONFIG_DIRECTORY_NAME = "config";

    private static final String DEFAULT_KERNEL_DOMAIN = "org.eclipse.virgo.kernel";

    private final File homeDirectory;

    private final File[] configDirectories;

    private final File workDirectory;

    private final String domain;

    public KernelConfiguration(BundleContext context) {
        this.homeDirectory = readHomeDirectory(context);
        this.configDirectories = readConfigDirectories(context);
        this.workDirectory = new File(this.homeDirectory, DEFAULT_WORK_DIRECTORY_NAME);
        this.domain = readDomain(context);
    }

    public File getHomeDirectory() {
        return homeDirectory;
    }

    public File[] getConfigDirectories() {
        return configDirectories.clone();
    }

    public File getWorkDirectory() {
        return workDirectory;
    }

    public String getDomain() {
        return domain;
    }

    private static File readHomeDirectory(BundleContext context) {
        String kernelHomeProperty = readFrameworkProperty(PROPERTY_KERNEL_HOME, context);
        if (!hasText(kernelHomeProperty)) {
            throw new IllegalStateException(PROPERTY_KERNEL_HOME + " property must be specified, and must not be empty");
        } else {
            return new File(kernelHomeProperty);
        }
    }

    private static File[] readConfigDirectories(BundleContext context) {
        String kernelConfigProperty = readFrameworkProperty(PROPERTY_KERNEL_CONFIG, context);
        List<File> configDirectories = new ArrayList<File>();

        if (hasText(kernelConfigProperty)) {
            parseKernelConfigProperty(kernelConfigProperty, configDirectories);
        }

        if (configDirectories.isEmpty()) {
            configDirectories.add(new File(DEFAULT_CONFIG_DIRECTORY_NAME));
        }

        return configDirectories.toArray(new File[configDirectories.size()]);
    }

    private static void parseKernelConfigProperty(String kernelConfigProperty, List<File> configDirectories) {
        String[] components = kernelConfigProperty.split(",");
        for (String component : components) {
            File configDir = new File(component.trim());
            if (!configDir.isAbsolute()) {
                configDir = new File(component.trim());
            }
            configDirectories.add(configDir);
        }
    }

    private static String readDomain(BundleContext context) {
        String kernelDomainProperty = readFrameworkProperty(PROPERTY_KERNEL_DOMAIN, context);
        if (!hasText(kernelDomainProperty)) {
            kernelDomainProperty = DEFAULT_KERNEL_DOMAIN;
        }
        return kernelDomainProperty;
    }

    private static String readFrameworkProperty(String propertyKey, BundleContext context) {
        return context.getProperty(propertyKey);
    }

    private static boolean hasText(String string) {
        return (string != null && !string.trim().isEmpty());
    }

}
