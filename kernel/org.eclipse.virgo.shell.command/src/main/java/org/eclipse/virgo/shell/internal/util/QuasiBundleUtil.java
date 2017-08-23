/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.shell.internal.util;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 * Utility methods to help with Bundle related commands and formatting
 *
 */
public final class QuasiBundleUtil {

	private QuasiFrameworkFactory quasiFrameworkFactory;

	/**
	 * 
	 * @param quasiFrameworkFactory
	 */
	public QuasiBundleUtil(QuasiFrameworkFactory quasiFrameworkFactory) {
		this.quasiFrameworkFactory = quasiFrameworkFactory;
	}
	
	/**
	 * 
	 * @return
	 */
    public List<QuasiBundle> getAllBundles() {
        return this.getQuasiFramework().getBundles();
    }
    
    /**
     * 
     * @param bundleId
     * @return
     */
    public QuasiBundle getBundle(long bundleId) {
        return this.getQuasiFramework().getBundle(bundleId);
    }
    
    /**
     * 
     * @param bundleId
     * @return
     */
    public List<QuasiResolutionFailure> getResolverReport(long bundleId) {
        QuasiFramework framework = this.getQuasiFramework();
        // We only care about the side-effect here
        framework.resolve();
        return framework.diagnose(bundleId);
    }

    /**
     * 
     * @return
     */
    private QuasiFramework getQuasiFramework() {
        return this.quasiFrameworkFactory.create();
    }
    
}
