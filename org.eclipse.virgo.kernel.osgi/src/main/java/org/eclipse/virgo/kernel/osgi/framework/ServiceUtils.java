/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.framework;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ServiceUtils} is a collection of OSGi service utilities for use by the kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe
 */
public class ServiceUtils {

    public static final String PROPERTY_KERNEL_STARTUP_WAIT_LIMIT = "org.eclipse.virgo.kernel.startup.wait.limit";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtils.class);

    private static final int DEFAULT_STARTUP_WAIT_LIMIT = 180; // 3 minutes

    private static volatile int maxSecondsWaitForService = DEFAULT_STARTUP_WAIT_LIMIT;

    private static volatile long maxMillisWaitForService = 0;

    private static final Object monitor = new Object();

    /**
     * Wait for a service of the given class but throw a TimeoutException if this takes longer than the number of
     * seconds configured in the framework property org.eclipse.virgo.kernel.startup.wait.limit.
     */
    public static <T> T getPotentiallyDelayedService(BundleContext context, Class<T> serviceClass) throws TimeoutException, InterruptedException {
        T service = null;
        OsgiServiceHolder<T> serviceHolder;
        long millisWaited = 0;
        while (service == null && millisWaited <= getInitialisedWaitLimit(context)) {
            try {
                serviceHolder = OsgiFrameworkUtils.getService(context, serviceClass);
                if (serviceHolder != null) {
                    service = serviceHolder.getService();
                } else {
                    millisWaited += sleepABitMore();
                }
            } catch (IllegalStateException e) {
            }
        }
        if (service == null) {
            throw new TimeoutException(serviceClass.getName());
        }
        return service;
    }

    private static long getInitialisedWaitLimit(BundleContext context) {
        if (maxMillisWaitForService == 0) {
            synchronized (monitor) {
                if (maxMillisWaitForService == 0) {
                    maxSecondsWaitForService = readBundleStartupWaitLimit(context);
                    maxMillisWaitForService = TimeUnit.SECONDS.toMillis(maxSecondsWaitForService);
                }
            }
        }
        return maxMillisWaitForService;
    }

    /**
     * Returns the service wait limit in seconds. This method will only return the correct value after
     * getInitialisedWaitLimit has been called, for instance after TimeoutException has been thrown from
     * getPotentiallyDelayedService.
     * 
     * @return the service wait limit in seconds or 0 if this has not been initialised
     */
    public static long getWaitLimitSeconds() {
        return maxSecondsWaitForService;
    }

    private static int readBundleStartupWaitLimit(BundleContext context) {
        String waitLimitProperty = readFrameworkProperty(PROPERTY_KERNEL_STARTUP_WAIT_LIMIT, context);
        if (!hasText(waitLimitProperty)) {
            return DEFAULT_STARTUP_WAIT_LIMIT;
        }

        try {
            return Integer.parseInt(waitLimitProperty);
        } catch (NumberFormatException e) {
            LOGGER.warn("Could not parse property {} with value '{}'. Using default limit {} seconds", new Object[] {
                PROPERTY_KERNEL_STARTUP_WAIT_LIMIT, waitLimitProperty, DEFAULT_STARTUP_WAIT_LIMIT });
            return DEFAULT_STARTUP_WAIT_LIMIT;
        }
    }

    private static String readFrameworkProperty(String propertyKey, BundleContext context) {
        return context.getProperty(propertyKey);
    }

    private static boolean hasText(String string) {
        return (string != null && !string.trim().isEmpty());
    }

    private static long sleepABitMore() throws InterruptedException {
        long before = System.currentTimeMillis();
        Thread.sleep(100);
        return System.currentTimeMillis() - before;
    }

}
