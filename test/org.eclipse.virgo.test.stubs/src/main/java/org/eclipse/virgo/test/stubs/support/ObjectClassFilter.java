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

package org.eclipse.virgo.test.stubs.support;

import static org.eclipse.virgo.test.stubs.internal.Assert.assertNotNull;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * A filter implementation that allows you to match on a given class type.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class ObjectClassFilter extends AbstractFilter {

    private static final String FILTER_STRING_FORMAT = "(objectClass=%s)";

    private final String className;

    /**
     * @param clazz The {@link Class} for the filter to match on
     */
    public ObjectClassFilter(Class<?> clazz) {
        assertNotNull(clazz, "clazz");
        this.className = clazz.getName();
    }

    /**
     * @param className The name of the {@link Class} for the filter to match on
     */
    public ObjectClassFilter(String className) {
        assertNotNull(className, "className");
        this.className = className;
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(ServiceReference<?> reference) {
        return contains((String[]) reference.getProperty(Constants.OBJECTCLASS), this.className);
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(Dictionary<String, ?> dictionary) {
        return contains((String[]) dictionary.get(Constants.OBJECTCLASS), this.className);
    }

    /**
     * {@inheritDoc}
     */
    public boolean matchCase(Dictionary<String, ?> dictionary) {
        return match(dictionary);
    }

    /**
     * {@inheritDoc}
     */
    public String getFilterString() {
        return String.format(FILTER_STRING_FORMAT, this.className);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean matches(Map<String, ?> map) {
        return contains((String [])map.get(Constants.OBJECTCLASS), this.className);
    }

    private boolean contains(String[] strings, String toMatch) {
        for (String string : strings) {
            if (toMatch.equals(string)) {
                return true;
            }
        }
        return false;
    }
    
}
