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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.shell.Command;
import org.eclipse.virgo.kernel.shell.internal.formatting.PackageCommandFormatter;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;

@Command("package")
public final class PackageCommands {

    private final StateService stateService;

    private final PackageCommandFormatter formatter;

    public PackageCommands(StateService stateService) {
        this.stateService = stateService;
        this.formatter = new PackageCommandFormatter();
    }

    @Command("list")
    public List<String> list() {
        return this.formatter.formatList(getAllPackages());
    }

    @Command("examine")
    public List<String> examine(String name, String versionString) {
        List<QuasiExportPackage> matchingExports = new ArrayList<QuasiExportPackage>();

        Version version;

        try {
            version = AbstractInstallArtifactBasedCommands.convertToVersion(versionString);
        } catch (IllegalArgumentException iae) {
            return Arrays.asList(iae.getMessage());
        }

        QuasiPackage packages = this.stateService.getPackages(null, name);
        for (QuasiExportPackage exportPackage : packages.getExporters()) {
            if (exportPackage.getVersion().equals(version)) {
                matchingExports.add(exportPackage);
            }
        }

        if (matchingExports.isEmpty()) {
            return Arrays.asList(String.format("No package with name '%s' and version '%s' was found", name, version));
        } else {
            return this.formatter.formatExamine(matchingExports);
        }
    }

    private List<QuasiExportPackage> getAllPackages() {
        List<QuasiExportPackage> packages = new ArrayList<QuasiExportPackage>();
        for (QuasiBundle bundle : this.stateService.getAllBundles(null)) {
            packages.addAll(bundle.getExportPackages());
        }
        return packages;
    }
}
