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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.shell.CommandCompleter;

public class InstallCompleter implements CommandCompleter {

    private static final String FILE_PREFIX = "file:";

    private static final String FILE_PATH_CANDIDATE_FORMAT = FILE_PREFIX + "%s%s";

    private static final String REPOSITORY_PREFIX = "repository:";

    private static final String REPOSITORY_TYPE_NAME_VERSION_FORMAT = REPOSITORY_PREFIX + "%s/%s/%s";

    private static final String REPOSITORY_TYPE_NAME_FORMAT = REPOSITORY_PREFIX + "%s/%s/";

    private static final String REPOSITORY_TYPE_FORMAT = REPOSITORY_PREFIX + "%s/";

    private static final String[] SCHEMES = { FILE_PREFIX, REPOSITORY_PREFIX };

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName repositoryQuery;

    private final ObjectName hostedRepositoryQuery;

    public InstallCompleter() throws MalformedObjectNameException, NullPointerException {
        this.repositoryQuery = new ObjectName("org.eclipse.virgo.kernel:type=Repository,*");
        this.hostedRepositoryQuery = new ObjectName("org.eclipse.virgo.server:type=HostedRepository,*");
    }

    public List<String> getCompletionCandidates(String subcommand, String... arguments) {
        Set<String> candidates;
        if (arguments.length == 0) {
            if (subcommand.startsWith(REPOSITORY_PREFIX)) {
                candidates = repository(subcommand);
            } else if (subcommand.startsWith(FILE_PREFIX)) {
                candidates = file(subcommand);
            } else {
                candidates = new HashSet<String>();
                for (String scheme : SCHEMES) {
                    if (scheme.startsWith(subcommand)) {
                        candidates.add(scheme);
                    }
                }
            }
        } else {
            // We do not complete anything after the 'subcommand' argument
            candidates = new HashSet<String>();
        }

        List<String> candidateList = new ArrayList<String>(candidates);
        Collections.sort(candidateList);
        return candidateList;
    }

    private Set<String> file(String subcommand) {
        List<String> candidates = new ArrayList<String>();

        String path = subcommand.substring(FILE_PREFIX.length());
        int completionIndex = 0; //new FileNameCompletor().complete(path, path.length(), candidates);
        
        Set<String> candidateSet = new HashSet<String>(candidates.size());

        if (completionIndex >= 0) {
            String completablePath = path.substring(0, completionIndex);
    
            for (String candidate : candidates) {
                candidateSet.add(String.format(FILE_PATH_CANDIDATE_FORMAT, completablePath, candidate));
            }
        }

        return candidateSet;
    }

    private Set<String> repository(String subcommand) {
        List<String> uriParts = getUriParts(subcommand);

        if (uriParts.size() == 1) {
            return type(uriParts.get(0));
        } else if (uriParts.size() == 2) {
            return name(uriParts.get(0), uriParts.get(1));
        } else if (uriParts.size() == 3) {
            return version(uriParts.get(0), uriParts.get(1), uriParts.get(2));
        }
        return Collections.emptySet();
    }

    private List<String> getUriParts(String subcommand) {
        List<String> uriParts = new ArrayList<String>(Arrays.asList(subcommand.substring(REPOSITORY_PREFIX.length()).split("/")));
        if (subcommand.endsWith("/")) {
            uriParts.add("");
        }
        return uriParts;
    }

    private Set<String> type(String type) {
        Set<String> types = new HashSet<String>();

        for (RepositoryInfo repository : getRepositories()) {
            for (ArtifactDescriptorSummary artifact : repository.getAllArtifactDescriptorSummaries()) {
                if (artifact.getType().startsWith(type)) {
                    types.add(String.format(REPOSITORY_TYPE_FORMAT, artifact.getType()));
                }
            }
        }

        return types;
    }

    private Set<String> name(String type, String name) {
        Set<String> names = new HashSet<String>();

        for (RepositoryInfo repository : getRepositories()) {
            for (ArtifactDescriptorSummary artifact : repository.getAllArtifactDescriptorSummaries()) {
                if (artifact.getType().equals(type) && artifact.getName().startsWith(name)) {
                    names.add(String.format(REPOSITORY_TYPE_NAME_FORMAT, artifact.getType(), artifact.getName()));
                }
            }
        }

        return names;
    }

    private Set<String> version(String type, String name, String version) {
        Set<String> versions = new HashSet<String>();

        for (RepositoryInfo repository : getRepositories()) {
            for (ArtifactDescriptorSummary artifact : repository.getAllArtifactDescriptorSummaries()) {
                if (artifact.getType().equals(type) && artifact.getName().equals(name) && artifact.getVersion().startsWith(version)) {
                    versions.add(String.format(REPOSITORY_TYPE_NAME_VERSION_FORMAT, artifact.getType(), artifact.getName(), artifact.getVersion()));
                }
            }
        }

        return versions;
    }

    private Set<RepositoryInfo> getRepositories() {
        Set<String> hostedRepositoryNames = new HashSet<String>();
        for (ObjectName objectName : this.server.queryNames(this.hostedRepositoryQuery, null)) {
            hostedRepositoryNames.add(objectName.getKeyProperty("name"));
        }

        Set<RepositoryInfo> repositories = new HashSet<RepositoryInfo>();
        for (ObjectName objectName : this.server.queryNames(this.repositoryQuery, null)) {
            String name = objectName.getKeyProperty("name");
            if (!hostedRepositoryNames.contains(name)) {
                repositories.add(JMX.newMXBeanProxy(this.server, objectName, RepositoryInfo.class));
            }
        }
        return repositories;
    }
}
