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

package org.eclipse.virgo.shell.stubs;

import java.io.File;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

/**
 */
public class StubQuasiFrameworkFactory implements QuasiFrameworkFactory {

	private StubBundle[] stubBundles;

	public StubQuasiFrameworkFactory(StubBundle... stubBundles) {
		this.stubBundles = stubBundles;
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public QuasiFramework create() {
        return new StubQuasiFramework(this.stubBundles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuasiFramework create(File arg0) {
        return new StubQuasiFramework(this.stubBundles);
    }

}
