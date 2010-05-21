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

package org.eclipse.virgo.apps.admin.web.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;

/**
 * <p>
 * AdminConsoleUtil is a simple class that provides access to various server properties
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * AdminConsoleUtil is threadsafe and immutable.
 * 
 */
public final class AdminConsoleUtil {

    private final String version;

    public AdminConsoleUtil() {
        this.version = this.readServerVersion("lib/.version");
    }

    public String getServerVersion() {
        return this.version;
    }

    public String getOperatingSystem() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return String.format("%s(%s) - %s", os.getName(), os.getArch(), os.getVersion());
    }

    public String getVMDesc() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        return String.format("%s(%s) - %s", rt.getVmVendor(), rt.getVmName(), rt.getVmVersion());
    }

    public String getJavaDesc() {
        String vendor = System.getProperty("java.vendor");
        String version = System.getProperty("java.version");
        return String.format("%s - %s", vendor, version);
    }

    public String getUserTimeZone() {
        String timeZone = System.getProperty("user.timezone");
        if (timeZone == null || "".equals(timeZone)) {
            timeZone = "Unavaliable";
        }
        return timeZone;
    }

    private final String readServerVersion(String path) {
        String readVersion;
        File versionFile = new File(path);
        Properties versions = new Properties();
        InputStream stream = null;
        try {
            stream = new FileInputStream(versionFile);
            versions.load(stream);
            readVersion = versions.getProperty("virgo.server.version");
            stream.close();
        } catch (IOException e) {
            readVersion = "";
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e1) {
                // no-op
            }
        }
        return readVersion;
    }
}
