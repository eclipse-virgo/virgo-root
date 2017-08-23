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

import org.osgi.framework.Constants;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.shell.internal.util.ServiceHolder;
import org.eclipse.virgo.util.common.StringUtils;

public final class ServiceCommandFormatter {

    private static final int MAX_LINE_LENGTH = 80;

    private static final String MULTIPLE_OBJECT_CLASSES_SUFFIX = ", ...";

    private static final String ID = "Id";

    private static final String OBJECT_CLASSES = "Object Class(es)";

    private static final String PROVIDER = "Providing Bundle";

    public List<String> formatList(List<ServiceHolder> services) {
        Collections.sort(services, new QuasiLiveServiceComparator());

        int maxIdLength = ID.length();
        int maxProviderLength = PROVIDER.length();
        for (ServiceHolder service : services) {
            int idLength = ((Long) service.getServiceId()).toString().length();
            maxIdLength = idLength > maxIdLength ? idLength : maxIdLength;
            int providerLength = ((Long) service.getProvider().getBundleId()).toString().length();
            maxProviderLength = providerLength > maxProviderLength ? providerLength : maxProviderLength;
        }

        int objectClassWidth = MAX_LINE_LENGTH - (2 + maxIdLength + maxProviderLength);

        List<String> lines = new ArrayList<String>();
        String format = String.format("%%-%ds %%-%ds %%%ds", maxIdLength, objectClassWidth, maxProviderLength);
        lines.add(String.format(format, ID, OBJECT_CLASSES, PROVIDER));

        for (ServiceHolder service : services) {
            Object objectClass = service.getProperties().get(Constants.OBJECTCLASS);
            lines.add(String.format(format, service.getServiceId(), formatObjectClass(objectClass, objectClassWidth),
                service.getProvider().getBundleId()));
        }

        return lines;
    }

    public List<String> formatExamine(ServiceHolder service) {
        List<String> lines = new ArrayList<String>();

        lines.add(String.format("Properties:"));
        lines.addAll(formatProperties(service.getProperties()));

        QuasiBundle provider = service.getProvider();
        lines.add("");
        lines.add(String.format("Publisher: %s %s [%s]", provider.getSymbolicName(), provider.getVersion().toString(), provider.getBundleId()));

        lines.add("");
        lines.add(String.format("Consumer(s):"));
        List<QuasiBundle> consumers = service.getConsumers();
        if (consumers.size() == 0) {
            lines.add(String.format("    None"));
        } else {
            for (QuasiBundle consumer : consumers) {
                lines.add(String.format("    %s %s [%s]", consumer.getSymbolicName(), consumer.getVersion().toString(), consumer.getBundleId()));
            }
        }

        return lines;
    }

    private static String formatObjectClass(Object objectClass, int maxLength) {
        StringBuilder sb = new StringBuilder();
        if (objectClass == null) {
            objectClass = new String[0];
        }
        String[] objectClasses;
        if (objectClass instanceof String) {
            objectClasses = StringUtils.commaDelimitedListToStringArray((String) objectClass);
        } else if (objectClass instanceof Object[]) {
            objectClasses = (String[]) objectClass;
        } else {
            objectClasses = StringUtils.commaDelimitedListToStringArray(objectClass.toString());
        }

        if (objectClasses.length == 0) {
            sb.append("<none>");
        } else {
            if (objectClasses.length > 1) {
                maxLength -= MULTIPLE_OBJECT_CLASSES_SUFFIX.length();
            }

            String formattedObjectClass = StringUtils.abbreviateDotSeparatedString(objectClasses[0], maxLength);
            sb.append(formattedObjectClass);

            if (objectClasses.length > 1) {
                sb.append(MULTIPLE_OBJECT_CLASSES_SUFFIX);
            }
        }

        return sb.toString();
    }

    private List<String> formatProperties(Map<String, Object> properties) {
        List<String> lines = new ArrayList<String>();
        List<String> keys = new ArrayList<String>(properties.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            lines.add(String.format("    %s:", key));
            Object value = properties.get(key);
            if (value instanceof Object[]) {
                List<String> propertyLines = PropertyFormatter.formatPropertyValue(value, MAX_LINE_LENGTH - 8);
                for (String propertyLine : propertyLines) {
                    lines.add(String.format("        %s", propertyLine));
                }
            } else {
                lines.add(String.format("        %s", value));
            }
        }
        
        return lines;
    }

    private static class QuasiLiveServiceComparator implements Comparator<ServiceHolder> {

        public int compare(ServiceHolder service1, ServiceHolder service2) {
            Long id1 = service1.getServiceId();
            Long id2 = service2.getServiceId();
            return id1.compareTo(id2);
        }

    }
}
