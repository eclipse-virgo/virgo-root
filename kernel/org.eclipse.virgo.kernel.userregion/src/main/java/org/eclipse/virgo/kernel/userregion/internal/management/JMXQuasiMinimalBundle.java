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

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;

/**
 * 
 *
 */
public class JMXQuasiMinimalBundle {

	private final QuasiBundle quasiBundle;

	public JMXQuasiMinimalBundle(QuasiBundle quasiBundle) {
		this.quasiBundle = quasiBundle;
	}

	public final long getIdentifier(){
		return this.quasiBundle.getBundleId();
	}
	
	public final String getSymbolicName(){
		return this.quasiBundle.getSymbolicName();
	}
	
	public final String getVersion() {
		return this.quasiBundle.getVersion().toString();
	}
	
	public final String getState() {
		if(this.quasiBundle.isResolved()){
			return "RESOLVED";
		}
		return "UNRESOLVED";
	}
	
	public final String getRegion() {
		return this.quasiBundle.getRegion().getName();
	}
	
	public final String getLocation() {
		return this.quasiBundle.getBundleLocation();
	}
	
	public final boolean getFragment() {
		return this.quasiBundle.getFragments() != null && this.quasiBundle.getFragments().size() > 0;
	}
	
	public final String[] getExportedPackages() {
		List<QuasiExportPackage> exportPackages = this.quasiBundle.getExportPackages();
		String[] packages = new String[exportPackages.size()];
		int i = 0;
		for (QuasiExportPackage quasiExportPackage : exportPackages) {
			packages[i] = quasiExportPackage.getPackageName() + ";" + quasiExportPackage.getVersion().toString();
			i++;
		}
		return packages;
	}
	
	public final String[] getImportedPackages() {
		List<QuasiImportPackage> importPackages = this.quasiBundle.getImportPackages();
		String[] packages = new String[importPackages.size()];
		int i = 0;
		for (QuasiImportPackage quasiImportPackage : importPackages) {
			packages[i] = quasiImportPackage.getPackageName() + ";" + quasiImportPackage.getVersionConstraint().toString();
			i++;
		}
		return packages;
	}
	
}
