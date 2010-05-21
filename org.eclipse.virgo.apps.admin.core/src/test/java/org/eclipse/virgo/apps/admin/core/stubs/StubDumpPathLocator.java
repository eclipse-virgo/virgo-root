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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.io.File;

import org.eclipse.virgo.apps.admin.core.dump.DumpPathLocator;



/**
 */
final public class StubDumpPathLocator implements DumpPathLocator{

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    /**
     * {@inheritDoc}
     */
    public File getDumpDirectory(){
        String path = this.getDumpConfigValue();
        if(path != null){
            File dumpDir = new File(path);
            if(dumpDir.exists() && dumpDir.isDirectory()){
                return dumpDir;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public File getDumpFolder(String folderName){
        String path = String.format("%s%s%s", this.getDumpConfigValue(), FILE_SEPARATOR, folderName);
        File dumpDir = new File(path);
        if(dumpDir.exists() && dumpDir.isDirectory()){
            return dumpDir;
        }else{
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public File getDumpEntryFile(String folderName, String fileName){
        String path = String.format("%s%s%s%s%s", this.getDumpConfigValue(), FILE_SEPARATOR, folderName, FILE_SEPARATOR, fileName);
        File dumpEntry = new File(path);
        if(dumpEntry.exists() && dumpEntry.isFile()){
            return dumpEntry;
        }else{
            return null;
        }
    }

    private String getDumpConfigValue(){
        return String.format("serviceability%sdumps", FILE_SEPARATOR);
    }

}
