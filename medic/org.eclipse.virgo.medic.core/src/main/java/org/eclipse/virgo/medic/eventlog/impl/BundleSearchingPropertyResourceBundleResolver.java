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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyResourceBundle;

import org.osgi.framework.Bundle;

public final class BundleSearchingPropertyResourceBundleResolver implements PropertyResourceBundleResolver {

    public List<PropertyResourceBundle> getResourceBundles(Bundle bundle, String candidatePropertiesFileName) {
        Enumeration<?> entries = bundle.findEntries("", candidatePropertiesFileName, false);
        List<PropertyResourceBundle> propertyBundles = new ArrayList<PropertyResourceBundle>();
        if (entries != null) {
            while (entries.hasMoreElements()) {
                URL propertiesFile = (URL) entries.nextElement();
                InputStream input = null;
                try {
                    input = propertiesFile.openStream();
                    propertyBundles.add(new PropertyResourceBundle(input));
                } catch (IOException ioe) {
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
        return propertyBundles;
    }
}
