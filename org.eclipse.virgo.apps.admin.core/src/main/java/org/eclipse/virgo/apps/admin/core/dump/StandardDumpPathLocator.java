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

package org.eclipse.virgo.apps.admin.core.dump;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


/**
 * <p>
 * StandardDumpPathLocator is an implementation of {@link DumpPathLocator} that 
 * gets the location of the dumps directory from config admin.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardDumpPathLocator is threadsafe
 *
 */
final class StandardDumpPathLocator implements DumpPathLocator{

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String CONFIG_POINT = "org.eclipse.virgo.medic";
    
    private static final String CONFIG_PROPERTY = "dump.root.directory";

    private final ConfigurationAdmin configurationAdmin;
    
    public StandardDumpPathLocator(ConfigurationAdmin configurationAdmin) {
        if(configurationAdmin == null){
            throw new IllegalArgumentException("Configuration Admin must not be null.");
        }
        this.configurationAdmin = configurationAdmin;
    }
    
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
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public File getDumpEntryFile(String folderName, String fileName){
        String path = String.format("%s%s%s%s%s", this.getDumpConfigValue(), FILE_SEPARATOR, folderName, FILE_SEPARATOR, fileName);
        File dumpEntry = new File(path);
        if(dumpEntry.exists() && dumpEntry.isFile()){
            return dumpEntry;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getDumpConfigValue(){
        String result = null;
        try {
            Configuration configurations = this.configurationAdmin.getConfiguration(CONFIG_POINT);
            Dictionary<String, String> properties = configurations.getProperties();
            if(properties != null){
                return properties.get(CONFIG_PROPERTY);
            }
        } catch (IOException e) {
            // no-op
        }
        return result;
    }

}
