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

package org.eclipse.virgo.nano.config.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Implementation of {@link PropertiesSource} that loads all the configuration supplied by the kernel user.
 * 
 * <p/>
 * 
 * User configuration is loaded from all properties files found in the directories listed in the
 * {@link KernelConfiguration#getConfigDirectories() configuration directories}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class UserConfigurationPropertiesSource implements PropertiesSource {

    private static final String PROPERTIES_FILE_SUFFIX = ".properties";

    private static final FilenameFilter PROPERTIES_FILENAME_FILTER = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(PROPERTIES_FILE_SUFFIX);
        }
    };

    private final File[] kernelConfigDirectories;

    /**
     * Creates a new <code>UserConfigurationPropertiesSource</code> that loads config files from the supplied user
     * config directories.
     * 
     * @param kernelConfigDirectories the directories containing the users kernel config files.
     */
    public UserConfigurationPropertiesSource(File[] kernelConfigDirectories) {
        this.kernelConfigDirectories = kernelConfigDirectories;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Properties> getConfigurationProperties() {
        Map<String, Properties> result = new TreeMap<String, Properties>();
        for (File dir : this.kernelConfigDirectories) {
            File[] configFiles = getPropertiesFiles(dir);
            for (File file : configFiles) {
                Properties properties = readPropertiesFromFile(file);

                String pid = computePid(file, properties, result.keySet());

                // Last pid encountered wins; no merging is performed
                result.put(pid, properties);
            }
        }
        return result;
    }

    private static File[] getPropertiesFiles(File directory) {
        if (directory.isDirectory()) {
            return FileSystemUtils.listFiles(directory, PROPERTIES_FILENAME_FILTER);
        }
        return new File[0];
    }

    private Properties readPropertiesFromFile(File file) {
        if (!file.exists()) {
            return null;
        }
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            props.load(is);
        } catch (IOException ioe) {
            // silently ignored
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
        return props;
    }

    private static String createPid(final File file) {
        return trimExtension(file.getName());
    }

    private static String trimExtension(final String name) {
        int lpDot = name.lastIndexOf('.');
        return lpDot == -1 ? name : name.substring(0, lpDot);
    }

    private String computePid(File file, Properties properties, Set<String> existingPids) {

        // check for factory pid in properties
        String pid = properties.getProperty(ConfigurationAdmin.SERVICE_FACTORYPID);
        if (StringUtils.hasText(pid)) {
            // need something unique - so multiple factory pids can be deployed
            return pid + "-" + createPid(file);
        }
        // account for service.pid as a property in the file
        pid = properties.getProperty(Constants.SERVICE_PID);
        if (StringUtils.hasText(pid)) {
            return pid;
        }
        return createPid(file);
    }
}
