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

package org.eclipse.virgo.kernel.shell.internal.commands;

import java.util.Arrays;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.shell.Command;
import org.eclipse.virgo.kernel.shell.internal.formatting.ConfigInstallArtifactCommandFormatter;

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

    private static final String TYPE = "configuration";

    private static final String STATE_ACTIVE = "ACTIVE";

    public ConfigCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator, ConfigurationAdmin configurationAdmin) {
        super(TYPE, objectNameCreator, new ConfigInstallArtifactCommandFormatter(configurationAdmin), ManageableArtifact.class, null);
    }

    @Command("examine")
    public List<String> examine(String name) {
        return examine(name, EMPTY_VERSION_STRING);
    }

    @Override
    public List<String> examine(String name, String versionString) {
        ManageableArtifact artifact;

        try {
            artifact = getArtifactRetriever().getArtifact(name, convertToVersion(versionString));
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        } catch (InstanceNotFoundException e) {
            return getDoesNotExistMessage(TYPE, name, versionString);
        }

        if (STATE_ACTIVE.equals(artifact.getState())) {
            return super.examine(name, versionString);
        }
        return Arrays.asList(UNABLE_TO_EXAMINE_CONFIGURATION_IN_NON_ACTIVE_STATE);
    }

    @Command("start")
    public List<String> start(String name) {
        return start(name, EMPTY_VERSION_STRING);
    }

    @Command("stop")
    public List<String> stop(String name) {
        return stop(name, EMPTY_VERSION_STRING);
    }

    @Command("refresh")
    public List<String> refresh(String name) {
        return refresh(name, EMPTY_VERSION_STRING);
    }

    @Command("uninstall")
    public List<String> uninstall(String name) {
        return uninstall(name, EMPTY_VERSION_STRING);
    }
}
