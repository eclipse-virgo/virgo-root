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
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 *   
 * MBean that allows for the exploration of state dumps using the QuasiFramework
 */
public class QuasiStateDumpMXBean implements StateDumpMXBean {
	
	private static final String INDENT = "    ";
	
	private final QuasiFrameworkFactory quasiFrameworkFactory;

	public QuasiStateDumpMXBean(QuasiFrameworkFactory quasiFrameworkFactory) {
		this.quasiFrameworkFactory = quasiFrameworkFactory;
	}

	public String[] getSummary(String dumpPath){
		File dumpDir = new File(dumpPath);
		if(dumpDir.exists() && dumpDir.isDirectory()){
			QuasiFramework quasiFramework;
			try {
				quasiFramework = this.quasiFrameworkFactory.create(dumpDir);
			} catch (ZipException e) {
				return new String[]{"Unable to extract the state dump: " + e.getMessage()};
			} catch (IOException e) {
				return new String[]{"Error reading the state dump: " + e.getMessage()};
			}
			quasiFramework.resolve();
			List<QuasiBundle> bundles = quasiFramework.getBundles();
			List<String> lines = new ArrayList<String>();
			for(QuasiBundle bundle: bundles){
				if(!bundle.isResolved()){
					lines.add(String.format("Bundle: %s_%s", bundle.getSymbolicName(), bundle.getVersion().toString()));
					File bundleFile = bundle.getBundleFile();
					if(bundleFile != null){
						lines.add(String.format("From location: %s", bundleFile.getPath()));
					}
					for(QuasiResolutionFailure fail: quasiFramework.diagnose(bundle.getBundleId())){
						lines.add(INDENT + fail.getDescription());
						lines.add("");
					}
				}
			}
			if(lines.size() > 0){
				lines.remove(lines.size() - 1);
			} else {
				lines.add("All Bundles were resolved at the time of this state dump.");
			}
			return lines.toArray(new String[lines.size()]);
		}
		return new String[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getUnresolvedBundleIds(String dumpFile) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public BundleMXBean[] listBundles(String dumpFile) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public BundleMXBean getBundle(String dumpFile, long bundleId) {
		return null;
	}
	
}
