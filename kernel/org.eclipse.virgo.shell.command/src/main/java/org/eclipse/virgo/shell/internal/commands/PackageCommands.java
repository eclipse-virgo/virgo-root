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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.PackageCommandFormatter;
import org.eclipse.virgo.shell.internal.util.PackageHolder;

@Command("package")
public final class PackageCommands {

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    private final PackageCommandFormatter formatter;

    public PackageCommands(QuasiFrameworkFactory quasiFrameworkFactory) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
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

        PackageHolder packages = this.getPackages(name);
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
        for (QuasiBundle bundle : this.quasiFrameworkFactory.create().getBundles()) {
            packages.addAll(bundle.getExportPackages());
        }
        return packages;
    }

    private PackageHolder getPackages(String packageName) {
        QuasiFramework framework = this.quasiFrameworkFactory.create();
        if (packageName != null) {
            List<QuasiImportPackage> importers = new ArrayList<QuasiImportPackage>();
            List<QuasiExportPackage> exporters = new ArrayList<QuasiExportPackage>();
            List<QuasiBundle> bundles = framework.getBundles();
            for (QuasiBundle qBundle : bundles) {
                QuasiImportPackage importPackage = processImporters(qBundle, packageName);
                if (importPackage != null) {
                    importers.add(importPackage);
                }
                QuasiExportPackage exportPackage = processExporters(qBundle, packageName);
                if (exportPackage != null) {
                    exporters.add(exportPackage);
                }
            }
            return new PackageHolder(exporters, importers, packageName);
        }
        return null;
    }

    private QuasiImportPackage processImporters(QuasiBundle qBundle, String packageName) {
        for (QuasiImportPackage qImportPackage : qBundle.getImportPackages()) {
            if (qImportPackage.getPackageName().equals(packageName)) {
                return qImportPackage;
            }
        }
        return null;
    }

    private QuasiExportPackage processExporters(QuasiBundle qBundle, String packageName) {
        for (QuasiExportPackage qExportPackage : qBundle.getExportPackages()) {
            if (qExportPackage.getPackageName().equals(packageName)) {
                return qExportPackage;
            }
        }
        return null;
    }
    
}
