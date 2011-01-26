/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregionfactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.osgi.framework.Version;

/**
 * {@link UserRegionPackageImportPolicy} is a {@link RegionPackageImportPolicy} for a user region.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
class UserRegionPackageImportPolicy implements RegionPackageImportPolicy {

    private static final String MANDATORY_ATTRIBUTE_NAME_SEPARATOR = ",";

    private static final String MANDATORY_DIRECTIVE_NAME = "mandatory";

    private static final String VERSION_ATTRIBUTE_NAME = "version";

    private static final String WILDCARD = "*";

    private final Map<String, ImportedPackage> importedPackages = new HashMap<String, ImportedPackage>();

    /**
     * Construct a {@link UserRegionPackageImportPolicy} for the specified import package list which must not contain
     * wildcards.
     * 
     * @param regionImports a string representing a list of imported packages
     */
    UserRegionPackageImportPolicy(String regionImports) {
        if (regionImports != null && !regionImports.isEmpty()) {
            if (regionImports.contains(WILDCARD)) {
                throw new IllegalArgumentException("Wildcards not supported in region imports: '" + regionImports + "'");
            }
            BundleManifest manifest = BundleManifestFactory.createBundleManifest();
            manifest.setHeader("Import-Package", regionImports);
            List<ImportedPackage> list = manifest.getImportPackage().getImportedPackages();
            for (ImportedPackage importedPackage : list) {
                String packageName = importedPackage.getPackageName();
                this.importedPackages.put(packageName, importedPackage);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isImported(String packageName, Map<String, Object> exportAttributes, Map<String, String> exportDirectives) {
        ImportedPackage importedPackage = this.importedPackages.get(packageName);
        if (importedPackage != null) {
            Map<String, String> importAttributes = importedPackage.getAttributes();
            Set<String> importAttributeNames = importAttributes.keySet();

            // Check any attribute values match.
            for (String importAttributeName : importAttributeNames) {
                if (exportAttributes == null) {
                    return false;
                }
                Object exportAttributeValue = exportAttributes.get(importAttributeName);
                if (importAttributeName.equals(VERSION_ATTRIBUTE_NAME)) {
                    if (exportAttributeValue != null && exportAttributeValue instanceof Version) {
                        Version exportVersion = (Version) exportAttributeValue;
                        String importAttributeValue = importAttributes.get(importAttributeName);
                        VersionRange importVersion = new VersionRange(importAttributeValue);
                        if (!importVersion.isIncluded(exportVersion)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (exportAttributeValue != null && exportAttributeValue instanceof String) {
                        String exportAttributeValueString = (String) exportAttributeValue;
                        String importAttributeValue = importAttributes.get(importAttributeName);
                        if (!exportAttributeValueString.equals(importAttributeValue)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

            }

            // Check mandatory attributes are present.
            if (exportDirectives != null) {
                String mandatoryDirectiveValue = exportDirectives.get(MANDATORY_DIRECTIVE_NAME);
                if (mandatoryDirectiveValue != null) {
                    for (String mandatoryAttribute : mandatoryDirectiveValue.split(MANDATORY_ATTRIBUTE_NAME_SEPARATOR)) {
                        if (!importAttributeNames.contains(mandatoryAttribute)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        return false;
    }

}
