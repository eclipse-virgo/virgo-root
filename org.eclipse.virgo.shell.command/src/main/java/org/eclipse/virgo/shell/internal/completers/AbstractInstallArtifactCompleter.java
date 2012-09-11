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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.CommandCompleter;

class AbstractInstallArtifactCompleter implements CommandCompleter {

    private static final String SUBCOMMAND_LIST = "list";

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final String type;

    private final RuntimeArtifactModelObjectNameCreator objectNameCreator;

    public AbstractInstallArtifactCompleter(String type, RuntimeArtifactModelObjectNameCreator objectNameCreator) {
        this.type = type;
        this.objectNameCreator = objectNameCreator;
    }

    public final List<String> getCompletionCandidates(String subcommand, String... tokens) {
        Set<String> candidates;

        if (SUBCOMMAND_LIST.equals(subcommand)) {
            candidates = Collections.<String> emptySet();
        } else if (tokens.length == 2) {
            candidates = versions(tokens[0], tokens[1]);
        } else if (tokens.length == 1) {
            candidates = names(tokens[0]);
        } else {
            candidates = Collections.<String> emptySet();
        }

        filter(candidates, subcommand, tokens);
        List<String> candidateList = new ArrayList<String>(candidates);
        Collections.sort(candidateList);
        return candidateList;
    }

    /**
     * To be over ridden by sub-classes that want to filter the completions to be offered back to the user.
     * 
     * @param candidates  
     * @param subcommand 
     * @param tokens 
     */
    protected void filter(Set<String> candidates, String subcommand, String... tokens) {
    }

    private Set<String> versions(String name, String version) {
        Set<String> candidates = new HashSet<String>();

        Set<ObjectName> objectNames = this.server.queryNames(this.objectNameCreator.createArtifactVersionsQuery(this.type, name), null);
        for (ObjectName objectName : objectNames) {
            String candidateVersion = this.objectNameCreator.getVersion(objectName);
            if (candidateVersion.startsWith(version)) {
                candidates.add(candidateVersion);
            }
        }

        return candidates;
    }

    private Set<String> names(String name) {
        Set<String> candidates = new HashSet<String>();

        Set<ObjectName> objectNames = this.server.queryNames(this.objectNameCreator.createArtifactsOfTypeQuery(this.type), null);
        for (ObjectName objectName : objectNames) {
            String candidateName = this.objectNameCreator.getName(objectName);
            if (candidateName.startsWith(name)) {
                candidates.add(candidateName);
            }
        }

        return candidates;
    }
}
