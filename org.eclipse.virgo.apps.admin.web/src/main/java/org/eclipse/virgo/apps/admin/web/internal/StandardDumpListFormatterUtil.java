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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.eclipse.virgo.apps.admin.core.DumpInspectorService;


/**
 * <p>
 * DumpListFormatterUtil will get a list of the available dump folders and produce a map 
 * of folder names to formatted display values.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * DumpListFormatterUtil is thread-safe
 *
 */
@Component
public final class StandardDumpListFormatterUtil implements DumpListFormatterUtil {
    
    private final DumpInspectorService dumpInspectorService;
    
    @Autowired
    public StandardDumpListFormatterUtil(DumpInspectorService dumpInspectorService) {
        this.dumpInspectorService = dumpInspectorService;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, String> getAvaliableDumps(){
        List<File> avaliableDumps = this.dumpInspectorService.findAvaliableDumps();
        Map<String, String> formattedDumps = new TreeMap<String, String>();
        for(File dumpDirectory : avaliableDumps){
                formattedDumps.put(dumpDirectory.getName(), dumpDirectory.getName());
        }
        return formattedDumps;
    }
    
}
