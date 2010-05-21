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

package org.eclipse.virgo.apps.admin.web.internal;

import java.util.Map;

/**
 * <p>
 * DumpListFormatterUtil will get a list of the available dump folders and produce a map 
 * of folder names to formatted display values.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementation of DumpListFormatterUtil should be thread safe
 *
 */
public interface DumpListFormatterUtil {

    /**
     * If it is not possible to format the name of the dump folder, maybe because it has 
     * been renamed, the name of the folder should be used as the display name.
     * 
     * @return Map of <Key, Display name>
     */
    public Map<String, String> getAvaliableDumps();
    
}
