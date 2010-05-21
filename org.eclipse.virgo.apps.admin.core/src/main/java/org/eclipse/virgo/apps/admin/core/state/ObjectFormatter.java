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

package org.eclipse.virgo.apps.admin.core.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;


/**
 */
final class ObjectFormatter {

    
    private ObjectFormatter() {
        // stop construction
    }
    
    /**
     * Return the object after a best attempt to format the object as a string
     * 
     * @param value object
     * @return best attempt to convert object to string representation
     */
    public static String formatObject(Object value) {
        if(value instanceof Object[]) {
            return StringUtils.arrayToDelimitedString((Object[]) value, ", ");
        }
        return value.toString();
    }
    
    /**
     * Will return a Map<String, String> after doing a best attempt to make the values nicely formatted Strings.
     * 
     * @param properties arbitrary property map
     * @return String->String version of properties
     */
    public static Map<String, String> formatMapValues(Map<String, Object> properties){
        Map<String, String> formattedProperties = new HashMap<String, String>();
        for(Entry<String, Object> entry : properties.entrySet()) {
            formattedProperties.put(entry.getKey(), formatObject(entry.getValue()));
        }
        return formattedProperties;
    }
    
}
