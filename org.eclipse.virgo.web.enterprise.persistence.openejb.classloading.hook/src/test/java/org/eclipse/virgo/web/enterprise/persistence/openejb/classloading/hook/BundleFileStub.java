/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.persistence.openejb.classloading.hook;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;

public class BundleFileStub extends BundleFile {
	private String classpath;
	
	public String getClassPath() {
		return classpath;
	}
	
	public BundleFileStub(String classpath) {
		this.classpath = classpath;
	}
	
	public File getBaseFile() {
		return new File(classpath);
	} 

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsDir(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BundleEntry getEntry(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getEntryPaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getFile(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open() throws IOException {
		// TODO Auto-generated method stub

	}
	
}
