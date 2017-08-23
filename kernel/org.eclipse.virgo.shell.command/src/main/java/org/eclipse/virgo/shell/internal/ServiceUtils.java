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

package org.eclipse.virgo.shell.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility methods for working with services in the OSGi service registry.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class ServiceUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtils.class);
    
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz, String requiredProperty, String requiredPropertyValue) {
        T result = null;
        try {
            ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences((String)null, String.format("(%s=*)", requiredProperty));
            if (serviceReferences != null) {
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    Object offeredPropertyValue = serviceReference.getProperty(requiredProperty);
                    if (offeredPropertyValue instanceof String) { // String value
                        String offeredProperty = (String) offeredPropertyValue;
                        if (offeredProperty != null && requiredPropertyValue.equals(offeredProperty)) {
                            Object potentialResult = bundleContext.getService(serviceReference);
                            if (clazz.isInstance(potentialResult)) {
                                result = clazz.cast(bundleContext.getService(serviceReference));
                                break;
                            }
                        }
                    } else if (offeredPropertyValue instanceof String[]) { // String[] value
                        String[] offeredProperties = (String[]) offeredPropertyValue;
                        if (offeredProperties != null && arrayContainsEntry(offeredProperties, requiredPropertyValue)) {
                            Object potentialResult = bundleContext.getService(serviceReference);
                            if (clazz.isInstance(potentialResult)) {
                                result = clazz.cast(bundleContext.getService(serviceReference));
                                break;
                            }
                        }
                    } else {
                        LOGGER.warn(String.format(
                            "Matching service found from bundle %d but with a bad type for the '%s' property, String or String[] expected.",
                            serviceReference.getBundle().getBundleId(), requiredProperty));
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException", e);
        }

        return result;
    }
    
    private static <T> boolean arrayContainsEntry(T[] array, T entry) {
        if (entry == null || array == null) {
            return false;
        }
        for (T arrayEntry : array) {
            if (arrayEntry != null && arrayEntry.equals(entry)) {
                return true;
            }
        }
        return false;
    }
}
