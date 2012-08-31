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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.shell.CommandCompleter;

public class PackageCompleter implements CommandCompleter {

    private static final String SUBCOMMAND_LIST = "list";

	private QuasiFrameworkFactory quasiFrameworkFactory;

    public PackageCompleter(QuasiFrameworkFactory quasiFrameworkFactory) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
    }

    public List<String> getCompletionCandidates(String subcommand, String... tokens) {
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

        List<String> candidateList = new ArrayList<String>(candidates);
        Collections.sort(candidateList);
        return candidateList;
    }

    private Set<String> versions(String name, String version) {
        Set<String> versions = new HashSet<String>();

        for (QuasiExportPackage exportPackage : getAllPackages()) {
            String packageName = exportPackage.getPackageName();
            String packageVersion = exportPackage.getVersion().toString();
            if (packageName.equals(name) && packageVersion.startsWith(version)) {
                versions.add(packageVersion);
            }
        }

        return versions;
    }

    private Set<String> names(String name) {
        Set<String> names = new HashSet<String>();

        for (QuasiExportPackage exportPackage : getAllPackages()) {
            String packageName = exportPackage.getPackageName();
            if (packageName.startsWith(name)) {
                names.add(packageName);
            }
        }

        return names;
    }

    private List<QuasiExportPackage> getAllPackages() {
        List<QuasiExportPackage> packages = new ArrayList<QuasiExportPackage>();
        for (QuasiBundle bundle : this.quasiFrameworkFactory.create().getBundles()) {
            packages.addAll(bundle.getExportPackages());
        }
        return packages;
    }
}
