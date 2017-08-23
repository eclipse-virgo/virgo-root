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

import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;
import java.util.UUID;

/**
 * Utility class for resolving placeholders inside a {@link Properties} instance. These placeholders can refer to other
 * properties in the <code>Properties</code> instance. The place holders may also have a modifier in them
 * 
 * <pre>
 * ${com.springsource:modifier}
 * </pre>
 * 
 * where everything after the colon is considered the modifier. This class does not interpret these modifiers but
 * rather delegates to a {@link PlaceholderValueTransformer} for processing.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 */
public final class PropertyPlaceholderResolver {
    
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^:\\}]*):?([^\\}]*)?\\}");

    private static final PlaceholderValueTransformer IDENTITY_TRANSFORMER = new PlaceholderValueTransformer() {

        public String transform(String propertyName, String propertyValue, String modifier) {
            return propertyValue;
        }

    };

    /**
     * Resolves all placeholders in the supplied {@link Properties} instance.
     * 
     * @param input the properties to resolve.
     * @return the resolved properties.
     */
    public Properties resolve(Properties input) {
        return resolve(input, IDENTITY_TRANSFORMER);
    }

    /**
     * Resolves all placeholders in the supplied {@link Properties} instance and transform any based on their modifiers.
     * 
     * @param input the properties to resolve.
     * @param transformer a transformer for handling property modifiers
     * @return the resolved properties.
     */
    public Properties resolve(Properties input, PlaceholderValueTransformer transformer) {
        Properties result = new Properties();
        Enumeration<?> propertyNames = input.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            result.setProperty(propertyName, resolveProperty(propertyName, input, transformer));
            
        }

        return result;
    }

    /**
     * Resolves all placeholders in the supplied string with values from a {@link Properties} instance.
     * 
     * @param input the string to resolve
     * @param props the properties to use for resolution
     * @return the resolved string
     */
    public String resolve(String input, Properties props) {
        return resolve(input, props, IDENTITY_TRANSFORMER);
    }

    /**
     * Resolves all placeholders in the supplied string with values from a {@link Properties} instance and transform any
     * based on their modifiers.
     * 
     * @param input the string to resolve
     * @param props the properties to use for resolution
     * @param transformer a transformer for handling property modifiers
     * @return the resolved string
     */
    public String resolve(String input, Properties props, PlaceholderValueTransformer transformer) {
        String key = UUID.randomUUID().toString();
        props.put(key, input);
        String value = resolveProperty(key, props, transformer);
        props.remove(key);
        return value;
    }

    private String resolveProperty(String name, Properties props, PlaceholderValueTransformer transformer) {
        Stack<String> visitState = new Stack<String>();
        return resolve(name, props, transformer, visitState);
    }

    private String resolve(String name, Properties props, PlaceholderValueTransformer transformer, Stack<String> visitState) {
        visitState.push(name);

        String initialValue = props.getProperty(name);
        if(initialValue == null) {
            throw new RuntimeException("No value found for placeholder '" + name + "'");
        }

        Matcher matcher = PATTERN.matcher(initialValue);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String propName = matcher.group(1);
            if (visitState.contains(propName)) {
                throw new IllegalArgumentException(formatPropertyCycleMessage(visitState));
            }

            String value = resolve(propName, props, transformer, visitState);
            if (matcher.group(2).length() > 0) {
                value = transformer.transform(propName, value, matcher.group(2));
            }
            matcher.appendReplacement(sb, escapeBackslashes(value));
        }
        matcher.appendTail(sb);

        visitState.pop();
        return sb.toString();
    }

    private static String escapeBackslashes(String string) {
        StringBuffer sb = new StringBuffer(string.length());
        int bsIndex = string.indexOf("\\");
        int pos = 0;
        while (bsIndex != -1) {
            sb.append(string.substring(pos,bsIndex+1));
            sb.append("\\"); // another backslash
            pos = bsIndex+1;
            bsIndex = string.indexOf("\\",pos);
        }
        sb.append(string.substring(pos, string.length()));
        return new String(sb);
    }
    
    private String formatPropertyCycleMessage(Stack<String> visitState) {
        StringBuilder sb = new StringBuilder();
        sb.append("Circular reference in property definitions: ");
        for (String name : visitState) {
            sb.append(name).append(" -> ");
        }

        sb.append(visitState.iterator().next());
        return sb.toString();
    }

    /**
     * An interface for property placeholder modifiers. Implementations of this interface are called when a property
     * placeholder modifier is detected on a class.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations must be threadsafe.
     * 
     */
    public static interface PlaceholderValueTransformer {

        /**
         * Transforms a property from its initial value to some other value
         * 
         * @param propertyName the name of the property being transformed
         * @param propertyValue the original value of the property
         * @param modifier the modifer string attached to the placeholder
         * @return A string that has been modified by this transformer and to be used in place of the original value
         */
        String transform(String propertyName, String propertyValue, String modifier);
    }
}
