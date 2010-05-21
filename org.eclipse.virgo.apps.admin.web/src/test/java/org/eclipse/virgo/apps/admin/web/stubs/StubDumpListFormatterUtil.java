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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.apps.admin.web.internal.DumpListFormatterUtil;



/**
 */
final public class StubDumpListFormatterUtil implements DumpListFormatterUtil {
    
    /**
     * {@inheritDoc}
     */
    public Map<String, String> getAvaliableDumps(){
        return new HashMap<String, String>();
    }
    
}
