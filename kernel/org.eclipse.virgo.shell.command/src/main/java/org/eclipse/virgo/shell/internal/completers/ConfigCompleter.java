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

package org.eclipse.virgo.shell.internal.completers;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.Version;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.internal.util.ArtifactRetriever;

final class ConfigCompleter extends AbstractInstallArtifactCompleter {

    private static final String TYPE = "configuration";

    private static final String COMMAND_EXAMINE = "examine";

    private static final String STATE_ACTIVE = "ACTIVE";
    
    private final Region globalRegion;

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final RuntimeArtifactModelObjectNameCreator objectNameCreator;

    private final ArtifactRetriever<ManageableArtifact> artifactRetriever;

    public ConfigCompleter(RuntimeArtifactModelObjectNameCreator objectNameCreator, RegionDigraph regionDigraph) {
        super(TYPE, objectNameCreator);
        this.objectNameCreator = objectNameCreator;
        this.globalRegion = regionDigraph.getRegion("global");
        this.artifactRetriever = new ArtifactRetriever<ManageableArtifact>(TYPE, objectNameCreator, ManageableArtifact.class);
    }

    @Override
    protected void filter(Set<String> candidates, String subcommand, String... tokens) {
        if (COMMAND_EXAMINE.equals(subcommand)) {
            if (tokens.length == 2) {
                filterVersions(tokens[0], candidates);
            } else if (tokens.length == 1) {
                filterNames(candidates);
            }
        }
    }

    private void filterVersions(String name, Set<String> candidates) {
        for (Iterator<String> i = candidates.iterator(); i.hasNext();) {
            try {
                ManageableArtifact artifact = this.artifactRetriever.getArtifact(name, new Version(i.next()), globalRegion);
                if (!STATE_ACTIVE.equals(artifact.getState())) {
                    i.remove();
                }
            } catch (InstanceNotFoundException e) {
                // Swallow to allow others to proceed
            }
        }

    }

    private void filterNames(Set<String> candidates) {
        for (Iterator<String> i = candidates.iterator(); i.hasNext();) {
            ObjectName createArtifactVersionsQuery = this.objectNameCreator.createArtifactVersionsQuery(TYPE, i.next());
			Set<ObjectName> objectNames = this.server.queryNames(createArtifactVersionsQuery, null);
            boolean hasActive = false;
            for (ObjectName objectName : objectNames) {
                try {
                    ManageableArtifact artifact = this.artifactRetriever.getArtifact(objectName);
                    if (STATE_ACTIVE.equals(artifact.getState())) {
                        hasActive = true;
                        break;
                    }
                } catch (InstanceNotFoundException e) {
                    // Swallow to allow others to proceed
                }
            }
            if (!hasActive) {
                i.remove();
            }
        }

    }

}
