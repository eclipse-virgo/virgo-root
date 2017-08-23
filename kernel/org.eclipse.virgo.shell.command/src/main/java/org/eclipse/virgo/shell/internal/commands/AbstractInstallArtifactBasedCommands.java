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

package org.eclipse.virgo.shell.internal.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.InstallArtifactCommandFormatter;
import org.eclipse.virgo.shell.internal.util.ArtifactRetriever;

/**
 * An abstract class that handles the methods that are delegated to an install artifact.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
abstract class AbstractInstallArtifactBasedCommands<T extends ManageableArtifact> {

    private static final String NO_ARTIFACT_FOR_NAME_AND_VERSION = "No %s with name '%s' and version '%s' in Region '%s' was found";

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final String type;

    private final RuntimeArtifactModelObjectNameCreator objectNameCreator;

    private final InstallArtifactCommandFormatter<T> formatter;

    private final ArtifactRetriever<T> artifactRetriever;

    private final RegionDigraph regionDigraph;

    public AbstractInstallArtifactBasedCommands(String type, RuntimeArtifactModelObjectNameCreator objectNameCreator, InstallArtifactCommandFormatter<T> formatter, Class<T> artifactType, RegionDigraph regionDigraph) {
        this.type = type;
        this.objectNameCreator = objectNameCreator;
        this.formatter = formatter;
        this.artifactRetriever = new ArtifactRetriever<T>(type, objectNameCreator, artifactType);
        this.regionDigraph = regionDigraph;
    }

    @Command("list")
    public List<String> list() {
        Set<ObjectName> objectNames = this.server.queryNames(this.objectNameCreator.createArtifactsOfTypeQuery(this.type), null);
        List<T> artifacts = new ArrayList<T>(objectNames.size());
        for (ObjectName objectName : objectNames) {
            try {
                artifacts.add(this.artifactRetriever.getArtifact(objectName));
            } catch (InstanceNotFoundException e) {
                // Swallow to allow other to proceed
            }
        }

        return this.formatter.formatList(artifacts);
    }

    @Command("examine")
    public List<String> examine(String name, String versionString, String regionName) {
        Version convertToVersion = convertToVersion(versionString);
		Region convertToRegion = convertToRegion(regionName);
        try {
			return this.formatter.formatExamine(this.artifactRetriever.getArtifact(name, convertToVersion, convertToRegion));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException infe) {
            return Arrays.asList(infe.getMessage());
        }
    }

    protected List<String> getDoesNotExistMessage(String type, String name, String version, String regionName) {
        return Arrays.asList(String.format(NO_ARTIFACT_FOR_NAME_AND_VERSION, type, name, version, regionName));
    }

    @Command("start")
    public List<String> start(String name, String version, String regionName) {
        try {
            this.artifactRetriever.getArtifact(name, convertToVersion(version), convertToRegion(regionName)).start();
            return Arrays.asList(String.format("%s %s:%s started successfully", this.type, name, version));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(this.type, name, version, regionName);
        } catch (Exception e) {
            return Arrays.asList(String.format("%s %s:%s start failed", this.type, name, version), "", "", formatException(e));
        }
    }

    @Command("stop")
    public List<String> stop(String name, String version, String regionName) {
        try {
            this.artifactRetriever.getArtifact(name, convertToVersion(version), convertToRegion(regionName)).stop();
            return Arrays.asList(String.format("%s %s:%s stopped successfully", this.type, name, version));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(this.type, name, version, regionName);
        } catch (Exception e) {
            return Arrays.asList(String.format("%s %s:%s stop failed", this.type, name, version), "", "", formatException(e));
        }
    }

    @Command("refresh")
    public List<String> refresh(String name, String version, String regionName) {
        try {
            if (this.artifactRetriever.getArtifact(name, convertToVersion(version), convertToRegion(regionName)).refresh()) {
                return Arrays.asList(String.format("%s %s:%s refreshed successfully", this.type, name, version));
            } else {
                return Arrays.asList(String.format("%s %s:%s not refreshed, no changes made", this.type, name, version));
            }
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(this.type, name, version, regionName);
        } catch (Exception e) {
            return Arrays.asList(String.format("%s %s:%s refresh failed", this.type, name, version), "", "", formatException(e));
        }
    }

    @Command("uninstall")
    public List<String> uninstall(String name, String version, String regionName) {
        try {
            this.artifactRetriever.getArtifact(name, convertToVersion(version), convertToRegion(regionName)).uninstall();
            return Arrays.asList(String.format("%s %s%s uninstalled successfully", this.type, name, version));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(this.type, name, version, regionName);
        } catch (Exception e) {
            return Arrays.asList(String.format("%s %s:%s uninstall failed", this.type, name, version), "", "", formatException(e));
        }
    }

    protected final ArtifactRetriever<T> getArtifactRetriever() {
        return this.artifactRetriever;
    }

    private String formatException(Exception e) {
        StringWriter formattedException = new StringWriter();
        PrintWriter writer = new PrintWriter(formattedException);
        e.printStackTrace(writer);

        return formattedException.toString();
    }

    static Version convertToVersion(String versionString) {
        try {
            return new Version(versionString);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(String.format("'%s' is not a valid version", versionString));
        }
    }

    protected final Region convertToRegion(String regionName) {
        try {
            return this.regionDigraph.getRegion(regionName);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(String.format("'%s' is not a valid Region name", regionName));
        }
    }
}
