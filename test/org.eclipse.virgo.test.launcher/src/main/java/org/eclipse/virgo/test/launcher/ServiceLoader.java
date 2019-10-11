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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.launch.FrameworkFactory;

/**
 * Simple service loader that follows the same location convention as the Java 6 <a
 * href="http://java.sun.com/javase/6/docs/api/java/util/ServiceLoader.html">ServiceLoader</a>. This implementation is
 * designed to run on on Java 5.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * @param <T> service type
 */
public final class ServiceLoader<T> {

    private static final String CONFIG_FILE_PREFIX = "META-INF/services/";

    private final Class<T> serviceClass;

    private ServiceLoader(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public static <T> ServiceLoader<T> load(Class<T> serviceClass) {
        return new ServiceLoader<>(serviceClass);
    }

    /**
     * Gets all known implementations of the service interface. Equivalent to calling {@link #get(ClassLoader)} with
     * {@link ClassLoader#getSystemClassLoader()}.
     * @return set of implementation types
     * 
     * @see #get(ClassLoader)
     */
    public Set<T> get() {
        return get(ClassLoader.getSystemClassLoader());
    }

    /**
     * Gets all known implementations configured and visible in the supplied {@link ClassLoader}.
     * @param classLoader from which to look
     * @return set of implementation types
     */
    public Set<T> get(ClassLoader classLoader) {
        Set<T> results = new HashSet<>();
        try {
            Enumeration<URL> serviceFiles = findServiceFiles(classLoader);
            Set<Class<?>> implTypes = new HashSet<>();
            while (serviceFiles.hasMoreElements()) {
                URL url = serviceFiles.nextElement();
                String implName = readImplementationClassName(url);
                Class<?> cl = loadImplType(classLoader, implName);
                if (implTypes.add(cl)) {
                    results.add(createInstance(cl));
                }
            }
        } catch (IOException e) {
            throw new ServiceLoaderError("Unable to read config locations", e);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private T createInstance(Class<?> cl) {
        try {
            return (T) cl.newInstance();
        } catch (Exception e) {
            throw new ServiceLoaderError("Unable to instantiate service provider class '" + cl.getName() + "'", e);
        }
    }

    private Class<?> loadImplType(ClassLoader classLoader, String implName) {
        try {
            return classLoader.loadClass(implName);
        } catch (ClassNotFoundException e) {
            throw new ServiceLoaderError("Unable to load service class '" + implName + "' from '" + classLoader + "'", e);
        }
    }

    private Enumeration<URL> findServiceFiles(ClassLoader classLoader) throws IOException {
        String concreteConfigName = CONFIG_FILE_PREFIX + this.serviceClass.getName();
        return classLoader.getResources(concreteConfigName);
    }

    private String readImplementationClassName(URL input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input.openStream()));
        String name = null;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.length() > 0) {
                    String nonComment = readNonCommentContent(trimmed);
                    if (nonComment.length() > 0) {
                        name = nonComment;
                    }
                }
            }
        } finally {
            reader.close();
        }
        return name;
    }

    private String readNonCommentContent(String trimmed) {
        int index = trimmed.indexOf("#");
        if (index > -1) {
            return trimmed.substring(0, index);
        } else {
            return trimmed;
        }
    }

    public static void main(String[] args) {
        ServiceLoader<FrameworkFactory> factory = ServiceLoader.load(FrameworkFactory.class);
        Set<FrameworkFactory> set = factory.get(FrameworkFactory.class.getClassLoader());
        System.out.println(set);
    }

    public static final class ServiceLoaderError extends Error {

        private static final long serialVersionUID = 6134843287168204658L;

        ServiceLoaderError(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
