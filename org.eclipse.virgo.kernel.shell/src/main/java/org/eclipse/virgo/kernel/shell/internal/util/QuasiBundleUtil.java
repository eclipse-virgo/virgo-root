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
package org.eclipse.virgo.kernel.shell.internal.util;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 * Util methods to help with Bundle related commands and formatting
 *
 */
public final class QuasiBundleUtil {

	private QuasiFrameworkFactory quasiFrameworkFactory;

	public QuasiBundleUtil(QuasiFrameworkFactory quasiFrameworkFactory) {
		this.quasiFrameworkFactory = quasiFrameworkFactory;
	}
	
    public List<QuasiBundle> getAllBundles() {
        return this.getQuasiFramework().getBundles();
    }
    
    public QuasiBundle getBundle(long bundleId) {
        return this.getQuasiFramework().getBundle(bundleId);
    }
    
    public List<QuasiResolutionFailure> getResolverReport(long bundleId) {
        QuasiFramework framework = this.getQuasiFramework();
        framework.resolve();
        return framework.diagnose(bundleId);
    }

    private QuasiFramework getQuasiFramework() {
        return this.quasiFrameworkFactory.create();
    }
    
}
