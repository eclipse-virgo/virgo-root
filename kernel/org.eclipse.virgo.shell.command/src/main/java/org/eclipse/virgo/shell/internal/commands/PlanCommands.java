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

import java.util.List;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.CompositeInstallArtifactCommandFormatter;

@Command("plan")
final class PlanCommands extends AbstractInstallArtifactBasedCommands<ManageableCompositeArtifact> {

    private static final String TYPE = "plan";

    private static final String GLOBAL_REGION_NAME = "global";

    public PlanCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator, RegionDigraph regionDigraph) {
        super(TYPE, objectNameCreator, new CompositeInstallArtifactCommandFormatter(), ManageableCompositeArtifact.class, regionDigraph);
    }

    @Command("examine")
    public List<String> examine(String name, String version) {
        return examine(name, version, GLOBAL_REGION_NAME);
    }

    @Command("start")
    public List<String> start(String name, String version) {
        return start(name, version, GLOBAL_REGION_NAME);
    }

    @Command("stop")
    public List<String> stop(String name, String version) {
        return stop(name, version, GLOBAL_REGION_NAME);
    }

    @Command("refresh")
    public List<String> refresh(String name, String version) {
        return refresh(name, version, GLOBAL_REGION_NAME);
    }

    @Command("uninstall")
    public List<String> uninstall(String name, String version) {
        return uninstall(name, version, GLOBAL_REGION_NAME);
    }

}
