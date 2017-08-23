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

package org.eclipse.virgo.kernel.install.artifact.internal.scoping;

import org.osgi.framework.Version;


/**
 * A factory for creating scope names.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ScopeNameFactory {
    
    private static final String SCOPE_SEPARATOR = "-";
    
    public static String createScopeName(String name, Version version) {
        String scopeName = name + SCOPE_SEPARATOR + versionToShortString(version);
        return scopeName;        
    }
    
    private static String versionToShortString(Version version) {
        String result = version.toString();
        while (result.endsWith(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
}
