/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.management.console.stubs.objects;

import java.util.Map;

public class ObjectName {

    private final String domain;

    private final Map<String, String> properties;

    public ObjectName(String domain, Map<String, String> properties) {
        this.domain = domain;
        this.properties = properties;
    }

    // Stub methods

    public String get(String property) {
        return this.properties.get(property);
    }

    public String domain() {
        return domain;
    }

}
