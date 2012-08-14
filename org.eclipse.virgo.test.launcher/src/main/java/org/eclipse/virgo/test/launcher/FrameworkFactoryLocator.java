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

package org.eclipse.virgo.test.launcher;

import java.util.Set;

import org.osgi.framework.launch.FrameworkFactory;

/**
 * Utility class for locating the {@link FrameworkFactory} for the current running VM.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 */
final class FrameworkFactoryLocator {

    /**
     * Creates an instance of {@link FrameworkFactory}. The <code>FrameworkFactory</code> is loaded using the
     * {@link ServiceLoader} as per the standard conventions.
     * 
     * @return the <code>FrameworkFactory</code> instance.
     * @see FrameworkFactory
     * @see ServiceLoader
     * @throws IllegalStateException if no <code>FrameworkFactory</code> is found, or if more than one is present.
     */
    public static FrameworkFactory createFrameworkFactory() {
        ServiceLoader<FrameworkFactory> factoryLoader = ServiceLoader.load(FrameworkFactory.class);
        return uniqueServiceFromLoader(factoryLoader);
    }

    private static <T> T uniqueServiceFromLoader(ServiceLoader<T> factoryLoader) {
        Set<T> services = factoryLoader.get(FrameworkFactory.class.getClassLoader());

        if (services.isEmpty()) {
            throw new IllegalStateException("No FrameworkFactory services found.");
        } else if (services.size() > 1) {
            throw new IllegalStateException("Unable to locate unique FrameworkFactory. Found " + services.size()
                + " factories. Do you have multiple OSGi implementations on your classpath?");
        } else {
            return services.iterator().next();
        }

    }
}
