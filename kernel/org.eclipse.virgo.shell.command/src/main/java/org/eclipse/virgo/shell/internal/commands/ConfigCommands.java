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

import java.util.Arrays;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.ConfigInstallArtifactCommandFormatter;

/**
 * Commands for config artifacts.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
@Command("config")
final class ConfigCommands extends AbstractInstallArtifactBasedCommands<ManageableArtifact> {

    private static final String UNABLE_TO_EXAMINE_CONFIGURATION_IN_NON_ACTIVE_STATE = String.format("Unable to examine configuration in non-active state");

    private static final String EMPTY_VERSION_STRING = Version.emptyVersion.toString();
    
    private static final String GLOBAL_REGION_NAME = "global";

    private static final String TYPE = "configuration";

    private static final String STATE_ACTIVE = "ACTIVE";

    public ConfigCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator, ConfigurationAdmin configurationAdmin, RegionDigraph regionDigraph) {
        super(TYPE, objectNameCreator, new ConfigInstallArtifactCommandFormatter(configurationAdmin), ManageableArtifact.class, regionDigraph);
    }

    @Command("examine")
    public List<String> examine(String name) {
        return examine(name, EMPTY_VERSION_STRING, GLOBAL_REGION_NAME);
    }

    @Command("examine")
    public List<String> examine(String name, String version) {
        return examine(name, version, GLOBAL_REGION_NAME);
    }

    @Override
    public List<String> examine(String name, String versionString, String regionName) {
        ManageableArtifact artifact;

        try {
            artifact = getArtifactRetriever().getArtifact(name, convertToVersion(versionString), convertToRegion(regionName));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(TYPE, name, versionString, regionName);
        }

        if (STATE_ACTIVE.equals(artifact.getState())) {
            return super.examine(name, versionString, regionName);
        }
        return Arrays.asList(UNABLE_TO_EXAMINE_CONFIGURATION_IN_NON_ACTIVE_STATE);
    }

    @Command("start")
    public List<String> start(String name) {
        return start(name, EMPTY_VERSION_STRING, GLOBAL_REGION_NAME);
    }

    @Command("start")
    public List<String> start(String name, String version) {
        return start(name, version, GLOBAL_REGION_NAME);
    }

    @Command("stop")
    public List<String> stop(String name) {
        return stop(name, EMPTY_VERSION_STRING, GLOBAL_REGION_NAME);
    }

    @Command("stop")
    public List<String> stop(String name, String version) {
        return stop(name, version, GLOBAL_REGION_NAME);
    }

    @Command("refresh")
    public List<String> refresh(String name) {
        return refresh(name, EMPTY_VERSION_STRING, GLOBAL_REGION_NAME);
    }

    @Command("refresh")
    public List<String> refresh(String name, String version) {
        return refresh(name, version, GLOBAL_REGION_NAME);
    }

    @Command("uninstall")
    public List<String> uninstall(String name) {
        return uninstall(name, EMPTY_VERSION_STRING, GLOBAL_REGION_NAME);
    }
    
    @Command("uninstall")
    public List<String> uninstall(String name, String version) {
        return uninstall(name, version, GLOBAL_REGION_NAME);
    }
    
}
