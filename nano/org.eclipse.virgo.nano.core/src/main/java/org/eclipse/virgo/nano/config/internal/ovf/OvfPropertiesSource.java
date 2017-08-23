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

package org.eclipse.virgo.nano.config.internal.ovf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.virgo.nano.diagnostics.KernelLogEvents.OVF_CONFIGURATION_FILE_DOES_NOT_EXIST;
import static org.eclipse.virgo.nano.diagnostics.KernelLogEvents.OVF_READ_ERROR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.config.internal.PropertiesSource;
import org.osgi.framework.BundleContext;

/**
 * Implementation of {@link PropertiesSource} that reads properties from an OVF document.
 * <p />
 * The path to the OVF document is specified using the <code>org.eclipse.virgo.nano.config.ovf</code> framework
 * property.
 * <p/>
 * In order for a property in the OVF document to be exported to OVF its key should have the following format: <br/>
 * 
 * <pre>
 * cm:&lt;pid&gt;:&lt;property-name&gt;
 * </pre>
 * 
 * Any property keys not starting with the <code>cm:</code> prefix are not exported to config admin. Further, any
 * <code>cm:</code> properties not having both and PID and and name portion will cause an exception.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class OvfPropertiesSource implements PropertiesSource {

    static final String FRAMEWORK_PROPERTY_OVF = "org.eclipse.virgo.nano.config.ovf";

    private static final String PROPERTY_PREFIX = "cm:";

    private static final String PROPERTY_DELIMITER = ":";

    private final BundleContext bundleContext;

    private final EventLogger eventLogger;

    public OvfPropertiesSource(BundleContext bundleContext, EventLogger eventLogger) {
        this.bundleContext = bundleContext;
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Properties> getConfigurationProperties() {
        Properties sourceProperties = readSourceProperties();
        if (sourceProperties != null) {
            return translateProperties(sourceProperties);
        } else {
            return null;
        }
    }

    private Map<String, Properties> translateProperties(Properties sourceProperties) {
        Map<String, Properties> result = new HashMap<String, Properties>();
        Set<String> propertyNames = sourceProperties.stringPropertyNames();
        for (String propertyName : propertyNames) {
            ConfigAdminProperty prop = tryReadConfigAdminProperty(propertyName);
            if(prop != null) {
                Properties p = result.get(prop.pid);
                if(p == null) {
                    p = new Properties();
                    result.put(prop.pid, p);
                }
                p.setProperty(prop.key, sourceProperties.getProperty(propertyName));
            }
        }
        return result;
    }

    /**
     * Attempts to convert a property name into a valid configuration admin property based on the format laid out in
     * {@link OvfPropertiesSource}.
     * 
     * @param propertyName the unparsed property name
     * @return a {@link ConfigAdminProperty} or <code>null</code> if the property is not prefixed with <code>cm:</code>
     */
    private ConfigAdminProperty tryReadConfigAdminProperty(String propertyName) {
        ConfigAdminProperty result = null;
        if(propertyName.startsWith(PROPERTY_PREFIX)) {
            String parsed = propertyName.substring(PROPERTY_PREFIX.length());
            String[] components = parsed.split(PROPERTY_DELIMITER);
            if(components.length != 2) {
                throw new IllegalArgumentException("Invalid configuration admin property '" + propertyName + "' found in OVF.");
            } else {
                result = new ConfigAdminProperty();
                result.pid = components[0];
                result.key = components[1];
            }
        }
        return result;
    }

    private Properties readSourceProperties() {
        Properties result = null;
        File ovfFile = determineOvfFile();
        if (ovfFile != null) {
            if (!ovfFile.exists()) {
                this.eventLogger.log(OVF_CONFIGURATION_FILE_DOES_NOT_EXIST, ovfFile.getAbsolutePath());
            } else {
                result = readOvfFile(ovfFile);
            }

        }
        return result;
    }

    private File determineOvfFile() {
        File result = null;

        String path = this.bundleContext.getProperty(FRAMEWORK_PROPERTY_OVF);
        if (path != null) {
            result = new File(path);
        }

        return result;
    }

    private Properties readOvfFile(File ovfFile) {
        Properties result = null;
        try (Reader reader = new InputStreamReader(new FileInputStream(ovfFile), UTF_8)) {
            OvfEnvironmentPropertiesReader ovfReader = new OvfEnvironmentPropertiesReader();
            result = ovfReader.readProperties(reader);
        } catch (IOException ex) {
            this.eventLogger.log(OVF_READ_ERROR, ex, ovfFile.getAbsolutePath());
        }
        return result;
    }

    private static class ConfigAdminProperty {

        String pid;

        String key;
    }
}
