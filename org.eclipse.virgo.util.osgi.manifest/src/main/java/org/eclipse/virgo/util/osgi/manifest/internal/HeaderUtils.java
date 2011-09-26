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

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 */
final class HeaderUtils {
        
    static List<String> toList(String key, Map<String, String> map) {
        String string = map.get(key);
        
        List<String> list = new MapUpdatingList(map, key);
        
        if (string != null) {
            String[] components = string.split(",");
            for (String component : components) {
                list.add(component.trim());
            }
        }
        
        return list;                    
    }
    
    static String toString(List<String> strings) {
        if (strings.isEmpty()) {
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        
        Iterator<String> iterator = strings.iterator();
        
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
