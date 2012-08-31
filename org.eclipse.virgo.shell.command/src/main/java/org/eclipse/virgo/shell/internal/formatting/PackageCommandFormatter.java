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

package org.eclipse.virgo.shell.internal.formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;

public final class PackageCommandFormatter {

    private static final int MAX_LINE_LENGTH = 80;

    private static final String NAME = "Name";

    private static final String VERSION = "Version";

    private static final String PROVIDER = "Providing Bundle";

    public List<String> formatList(List<QuasiExportPackage> packages) {
        Collections.sort(packages, new QuasiExportPackageComparator());

        int maxNameLength = NAME.length();
        int maxVersionLength = VERSION.length();
        for (QuasiExportPackage exportPackage : packages) {
            int nameLength = exportPackage.getPackageName().length();
            maxNameLength = nameLength > maxNameLength ? nameLength : maxNameLength;
            int versionLength = exportPackage.getVersion().toString().length();
            maxVersionLength = versionLength > maxVersionLength ? versionLength : maxVersionLength;
        }

        List<String> lines = new ArrayList<String>();
        String format = String.format("%%-%ds %%-%ds %%%ds", maxNameLength, maxVersionLength, MAX_LINE_LENGTH
            - (2 + maxNameLength + maxVersionLength));
        lines.add(String.format(format, NAME, VERSION, PROVIDER));

        for (QuasiExportPackage exportPackage : packages) {
            lines.add(String.format(format, exportPackage.getPackageName(), exportPackage.getVersion().toString(),
                exportPackage.getExportingBundle().getBundleId()));
        }

        return lines;
    }

    public List<String> formatExamine(List<QuasiExportPackage> exportPackages) {
        List<String> lines = new ArrayList<String>();

        boolean first = true;

        for (QuasiExportPackage exportPackage : exportPackages) {
            if (!first) {
                lines.add("");
                lines.add("");
            }
            QuasiBundle exportingBundle = exportPackage.getExportingBundle();
            lines.add(String.format("Exporter: %s %s [%s]", exportingBundle.getSymbolicName(), exportingBundle.getVersion().toString(),
                exportingBundle.getBundleId()));

            lines.add("");
            lines.add(String.format("Attributes:"));
            lines.addAll(formatProperties(exportPackage.getAttributes()));

            lines.add("");
            lines.add(String.format("Directives:"));
            lines.addAll(formatProperties(exportPackage.getDirectives()));

            lines.add("");
            lines.add(String.format("Importer(s):"));
            if (exportPackage.getConsumers().isEmpty()) {
                lines.add(String.format("    %s", "None"));
            } else {
                for (QuasiImportPackage importPackage : exportPackage.getConsumers()) {
                    QuasiBundle importingBundle = importPackage.getImportingBundle();
                    lines.add(String.format("    %s %s [%s]", importingBundle.getSymbolicName(), importingBundle.getVersion().toString(),
                        importingBundle.getBundleId()));
                    lines.add(String.format("        Import-Package attributes:"));
                    lines.addAll(formatProperties(importPackage.getAttributes(), "            "));
                    lines.add(String.format("        Import-Package directives:"));
                    lines.addAll(formatProperties(importPackage.getDirectives(), "            "));
                }
            }
            first = false;
        }

        return lines;
    }

    private List<String> formatProperties(Map<String, Object> properties) {
        return formatProperties(properties, "    ");
    }

    private List<String> formatProperties(Map<String, Object> properties, String indent) {
        List<String> lines = new ArrayList<String>();
        List<String> keys = new ArrayList<String>(properties.keySet());
        if (keys.isEmpty()) {
            lines.add(String.format("%s%s", indent, "None"));
        } else {
            Collections.sort(keys);

            for (String key : keys) {
                lines.add(String.format("%s%s:", indent, key));
                Object value = properties.get(key);
                if (value instanceof Object[]) {
                    List<String> propertyLines = PropertyFormatter.formatPropertyValue(value, MAX_LINE_LENGTH - 8);
                    for (String propertyLine : propertyLines) {
                        lines.add(String.format("%s    %s", indent, propertyLine));
                    }
                } else {
                    lines.add(String.format("%s    %s", indent, value));
                }
            }
        }
        return lines;
    }

    private static class QuasiExportPackageComparator implements Comparator<QuasiExportPackage> {

        public int compare(QuasiExportPackage package1, QuasiExportPackage package2) {
            int value = package1.getPackageName().compareTo(package2.getPackageName());
            if (value != 0) {
                return value;
            }
            return package1.getVersion().compareTo(package2.getVersion());
        }

    }

}
