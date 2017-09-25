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
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * A filter implementation that allows you to match on a given set of properties.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class PropertiesFilter extends AbstractFilter {

    private static final String FILTER_STRING_FORMAT = "(%s=%s)";

    private static final Pattern PROPERTIES_PATTERN = Pattern.compile("\\(([^=&\\(\\)]*)=([^=&\\(\\)]*)\\)");

    private final Map<String, Object> properties;

    /**
     * Creates a new {@link PropertiesFilter} that matches on a collection of properties. You may need to pass in a
     * {@link TreeMap} in order to preserve the property ordering so that it matches the filter string generated by your
     * code.
     * 
     * @param properties The properties to match on
     */
    public PropertiesFilter(Map<String, Object> properties) {
        assertNotNull(properties, "properties");
        this.properties = properties;
    }

    /**
     * Creates a new {@link PropertiesFilter} that matches on a set of properties defined in a filter string.
     * 
     * @param filterString The filter string to parse and match on
     * @throws InvalidSyntaxException if the filterString is not of valid filter syntax
     */
    public PropertiesFilter(String filterString) throws InvalidSyntaxException {
        assertNotNull(filterString, filterString);
        this.properties = parseFilterString(filterString);
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(ServiceReference<?> reference) {
        for (Entry<String, Object> entry : this.properties.entrySet()) {
            Object value = reference.getProperty(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(Dictionary<String, ?> dictionary) {
        for (Entry<String, Object> entry : this.properties.entrySet()) {
            Object value = dictionary.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
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
        if (this.properties.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Entry<String, Object> entry : this.properties.entrySet()) {
            sb.append(String.format(FILTER_STRING_FORMAT, entry.getKey(), entry.getValue()));
        }

        if (this.properties.size() > 1) {
            sb.insert(0, "(&");
            sb.append(")");
        }

        return sb.toString();
    }

    private Map<String, Object> parseFilterString(String filterString) throws InvalidSyntaxException {
        Map<String, Object> properties = new TreeMap<String, Object>();

        Matcher matcher = PROPERTIES_PATTERN.matcher(filterString);
        while (matcher.find()) {
            properties.put(matcher.group(1), matcher.group(2));
        }

        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(Map<String, ?> map) {
        for (Entry<String, Object> entry : this.properties.entrySet()) {
            Object value = map.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}