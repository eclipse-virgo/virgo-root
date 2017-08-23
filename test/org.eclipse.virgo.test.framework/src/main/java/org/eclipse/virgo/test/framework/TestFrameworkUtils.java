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

package org.eclipse.virgo.test.framework;

import java.lang.annotation.Annotation;

import org.eclipse.virgo.util.common.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;

public final class TestFrameworkUtils {

    private TestFrameworkUtils() {
        // do not new me
    }

    public static BundleContext getBundleContextForTestClass(Class<?> testClass) {
        ClassLoader classLoader = testClass.getClassLoader();
        if (classLoader instanceof BundleReference) {
            return ((BundleReference) classLoader).getBundle().getBundleContext();
        } else {
            throw new IllegalArgumentException("Class '" + testClass.getName() + "' does not appear to be a valid test class.");
        }
    }

    /**
     * Checks {@link Constants#FRAGMENT_HOST} header to determine if bundle is a fragment
     * 
     * @param bundle
     * @return
     */
    public static boolean isFragment(Bundle bundle) {
        Assert.notNull(bundle, "bundle is required");
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }

    public static Class<?> findAnnotationDeclaringClass(Class<? extends Annotation> annotation, Class<?> clazzToStart) {
        Assert.notNull(annotation, "Annotation is required");
        // stop on object
        if (clazzToStart == null || clazzToStart.equals(Object.class)) {
            return null;
        }

        return clazzToStart.getAnnotation(annotation) != null ? clazzToStart : findAnnotationDeclaringClass(annotation, clazzToStart.getSuperclass());
    }
}
