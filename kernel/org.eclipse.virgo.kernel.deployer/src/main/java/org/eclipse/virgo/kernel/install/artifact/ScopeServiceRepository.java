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

package org.eclipse.virgo.kernel.install.artifact;

import java.util.Dictionary;
import java.util.Set;

import org.osgi.framework.InvalidSyntaxException;

public interface ScopeServiceRepository {

    void recordService(String scopeName, String[] types, Dictionary<String, Object> properties);

    boolean scopeHasMatchingService(String scopeName, String type, String filter) throws InvalidSyntaxException;

    void clearScope(String scopeName);

    Set<String> knownScopes();

}
