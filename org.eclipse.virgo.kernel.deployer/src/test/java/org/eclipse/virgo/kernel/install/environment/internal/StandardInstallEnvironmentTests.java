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

package org.eclipse.virgo.kernel.install.environment.internal;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;

import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.environment.internal.StandardInstallEnvironment;
import org.eclipse.virgo.repository.Repository;

/**
 */
public class StandardInstallEnvironmentTests {
    
    private Repository repository = createMock(Repository.class);

    private InstallLog installLog = createMock(InstallLog.class);
    
    private QuasiFramework quasiFramework = createMock(QuasiFramework.class);
    
    @Test
    public void testStandardInstallEnvironment() {
        InstallEnvironment ie = new StandardInstallEnvironment(this.repository, this.installLog, this.quasiFramework);
        assertEquals(this.repository, ie.getRepository());
        assertEquals(this.installLog, ie.getInstallLog());
    }

}
