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

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.osgi.framework.Bundle;

public final class StandardMessageResolver implements MessageResolver {

    private static final String RESOURCE_BUNDLE_NAME = "EventLogMessages";

    private final LocaleResolver localeResolver;

    private final PropertyResourceBundleResolver resourceBundleLocator;

    private final Bundle primaryBundle;

    private final Bundle secondaryBundle;

    public StandardMessageResolver(LocaleResolver localeResolver, PropertyResourceBundleResolver resourceBundleLocator, Bundle primaryBundle,
        Bundle secondaryBundle) {
        this.localeResolver = localeResolver;
        this.resourceBundleLocator = resourceBundleLocator;
        this.primaryBundle = primaryBundle;
        this.secondaryBundle = secondaryBundle;
    }

    public String resolveLogEventMessage(String eventCode) {
        return resolveLogEventMessage(eventCode, this.localeResolver.getLocale());
    }

    public String resolveLogEventMessage(String eventCode, Locale locale) {
        List<String> candidatePropertiesFileNames = ResourceBundleUtils.generateCandidatePropertiesFileNames(RESOURCE_BUNDLE_NAME, locale);

        String message = resolveMessageInBundle(this.primaryBundle, eventCode, candidatePropertiesFileNames);

        if (message == null) {
            message = resolveMessageInBundle(this.secondaryBundle, eventCode, candidatePropertiesFileNames);
        }

        return message;
    }

    private String resolveMessageInBundle(Bundle bundle, String key, List<String> candidatePropertiesFileNames) {
        for (String candidatePropertiesFileName : candidatePropertiesFileNames) {
            List<PropertyResourceBundle> resourceBundles = this.resourceBundleLocator.getResourceBundles(bundle, candidatePropertiesFileName);
            for (PropertyResourceBundle resourceBundle : resourceBundles) {
                try {
                    String string = resourceBundle.getString(key);
                    if (string != null) {
                        return string;
                    }
                } catch (MissingResourceException mre) {
                    // Continue searching
                }
            }
        }
        return null;
    }
}
