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
public class QuasiStateDumpMBean implements StateDumpMBean {
	
	private static final String INDENT = "    ";
	
	private final QuasiFrameworkFactory quasiFrameworkFactory;

	public QuasiStateDumpMBean(QuasiFrameworkFactory quasiFrameworkFactory) {
		this.quasiFrameworkFactory = quasiFrameworkFactory;
	}

	@Override
	public String[] getSummary(String dumpId){
		File dumpDir = new File(dumpId);
		if(dumpDir.exists() && dumpDir.isDirectory()){
			QuasiFramework quasiFramework;
			try {
				quasiFramework = this.quasiFrameworkFactory.create(new File(dumpDir.getParent()));
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
					lines.add(String.format("Bundle: %s-%s", bundle.getSymbolicName(), bundle.getVersion().toString()));
					for(QuasiResolutionFailure fail: quasiFramework.diagnose(bundle.getBundleId())){
						lines.add(INDENT + fail.getDescription());
						lines.add("");
					}
					lines.add("");
				}
			}
			return lines.toArray(new String[lines.size()]);
		}
		return new String[0];
	}
	
}
