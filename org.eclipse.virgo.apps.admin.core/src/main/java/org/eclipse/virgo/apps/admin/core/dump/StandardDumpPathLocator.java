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
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.InvalidSyntaxException;


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
    
    private static final String CONFIG_PROPERTY = "dump.root.directory";

    private static final String MEDIC_MBEAN_QUERY = "org.eclipse.virgo.kernel:type=Configuration,name=org.eclipse.virgo.medic";

	private String dumpConfigValue;
    
    public StandardDumpPathLocator() throws InvalidSyntaxException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Object attribute = mBeanServer.getAttribute(new ObjectName(MEDIC_MBEAN_QUERY), "Properties");
            TabularDataSupport table = (TabularDataSupport) attribute;
            CompositeDataSupport composite = (CompositeDataSupport) table.get(new Object[]{CONFIG_PROPERTY});
            this.dumpConfigValue = composite.get("value").toString();
        } catch (Exception e) {
            //no-op
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public File getDumpDirectory(){
        String path = this.dumpConfigValue;
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
        String path = String.format("%s%s%s", this.dumpConfigValue, FILE_SEPARATOR, folderName);
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
        String path = String.format("%s%s%s%s%s", this.dumpConfigValue, FILE_SEPARATOR, folderName, FILE_SEPARATOR, fileName);
        File dumpEntry = new File(path);
        if(dumpEntry.exists() && dumpEntry.isFile()){
            return dumpEntry;
        }
        return null;
    }

}
