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
package org.eclipse.virgo.apps.admin.web.medic;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicMBeanExporter {
	
    private final Logger logger = LoggerFactory.getLogger(MedicMBeanExporter.class);

    private static final String DOMAIN = "org.eclipse.virgo.kernel";
    
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	
    private final List<ObjectInstance> mBeans = new ArrayList<ObjectInstance>();

	public MedicMBeanExporter(String serverHome) {
		try {
			ObjectName dumpMBeanName = new ObjectName(String.format("%s:type=Medic,name=DumpInspector", DOMAIN));
			mBeans.add(this.server.registerMBean(new FileSystemDumpInspector(serverHome + "/serviceability/dump"), dumpMBeanName));
		} catch (Exception e) {
			logger.error("Unable to register the DumpInspectorMBean", e);
		} 
	}
	
	public void close(){
		for(ObjectInstance mBean : mBeans){
			try {
				this.server.unregisterMBean(mBean.getObjectName());
			} catch (Exception e) {
				logger.error("Unable to unregister MBean", e);
			} 
		}
	}
 
    /**
     * Generate array of {@link File}s, one for each file in the directory <code>dir</code> (just like {@link File#listFiles()}).
     * This function, however, <i>never</i> returns the <strong><code>null</code></strong> pointer, but instead throws
     * an exception if it cannot determine the files, or if <code>dir</code> isn't a directory.
     *  
     * @param dir directory file for which the files should be generated.
     * @param logger where to log warnings or errors, if not null
     * @return array of {@link File}s; may be the empty array.
     * @throws FatalIOException when {@link File#listFiles()} returns <strong><code>null</code></strong> even after a retry.
     */
    protected static File[] listFiles(File dir, Logger logger) throws IOException {
        File[] files = dir.listFiles();
        if (files==null) {
            if (logger!=null) logger.warn("'" + dir + "'.listFiles() returned null first time.");
            try {
                dir.getCanonicalPath(); // potentially involves I/O
            } catch (IOException ioe) {
                if (logger!=null) logger.warn("PreRetry logic '" + dir + "'.getCanonicalPath() threw IOException.", ioe);
            }
            files = dir.listFiles();
        }
        if (files==null) {
            if (logger!=null) logger.error("'" + dir + "'.listFiles() returned null on retry.");
            throw new IOException("listFiles() failed for file " + dir);
        }
        return files;
    }
    
    /**
     * Delete the supplied {@link File} and, for directories, recursively delete any nested directories or files.
     * 
     * @param root the root <code>File</code> to delete.
     * @return <code>true</code> if the <code>File</code> was deleted, otherwise <code>false</code>.
     * @see #deleteRecursively(String)
     */
    protected static boolean doRecursiveDelete(File root) {
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
				if(children != null) {
					for (File file : children) {
	                    doRecursiveDelete(file);
	                }
				}
            }
            return root.delete();
        }
        return false;
    }
	
}
