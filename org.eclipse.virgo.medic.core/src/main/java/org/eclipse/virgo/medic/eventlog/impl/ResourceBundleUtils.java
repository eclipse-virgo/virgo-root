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

package org.eclipse.virgo.medic.eventlog.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ResourceBundleUtils {

    private static final String PROPERTIES_SUFFIX = ".properties";

    static List<String> generateCandidatePropertiesFileNames(String baseName, Locale locale) {
        List<String> candidateNames = new ArrayList<String>();

        candidateNames.addAll(generateCandidatePropertiesFileNames(baseName, locale.getLanguage(), locale.getCountry(), locale.getVariant()));

        Locale defaultLocale = Locale.getDefault();

        if (!defaultLocale.equals(locale)) {
            candidateNames.addAll(generateCandidatePropertiesFileNames(baseName, defaultLocale.getLanguage(), defaultLocale.getCountry(),
                defaultLocale.getVariant()));
        }

        candidateNames.add(baseName + PROPERTIES_SUFFIX);

        return candidateNames;
    }

    private static List<String> generateCandidatePropertiesFileNames(String baseName, String language, String country, String variant) {
        List<String> candidateNames = new ArrayList<String>();

        if (hasText(language)) {
            if (hasText(country)) {
                if (hasText(variant)) {
                    candidateNames.add(baseName + "_" + language + "_" + country + "_" + variant + PROPERTIES_SUFFIX);
                }
                candidateNames.add(baseName + "_" + language + "_" + country + PROPERTIES_SUFFIX);
            }
            candidateNames.add(baseName + "_" + language + PROPERTIES_SUFFIX);
        }

        return candidateNames;
    }

    private static boolean hasText(String string) {
        return (string != null && string.length() > 0);
    }
}
