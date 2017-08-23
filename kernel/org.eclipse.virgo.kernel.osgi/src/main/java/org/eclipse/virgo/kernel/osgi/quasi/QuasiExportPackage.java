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

package org.eclipse.virgo.kernel.osgi.quasi;

import java.util.List;

import org.osgi.framework.Version;

/**
 * {@link QuasiExportPackage} is a representation of an exported package
 * from a {@link QuasiBundle}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface QuasiExportPackage extends QuasiParameterised {
	
	/**
	 * 
	 * @return the name of the package being exported
	 */
	public String getPackageName();

	/**
	 * 
	 * @return the <code>Version</code> that the package is exported at.
	 */
	public Version getVersion();
	
	/**
	 * 
	 * @return the {@link QuasiBundle} that provides this <code>QuasiExportPackage</code>
	 */
	public QuasiBundle getExportingBundle();
	
	/**
	 * 
	 * @return The a list {@link QuasiImportPackage}s that are consuming this export
	 */
	public List<QuasiImportPackage> getConsumers();
}
