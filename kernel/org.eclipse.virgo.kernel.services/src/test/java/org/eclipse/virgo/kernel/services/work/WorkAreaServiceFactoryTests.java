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

package org.eclipse.virgo.kernel.services.work;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.kernel.services.work.WorkAreaServiceFactory;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

/**
 */
public class WorkAreaServiceFactoryTests {

    @Test
    public void testCreate() {
        File work = new File("build");
        WorkAreaServiceFactory factory = new WorkAreaServiceFactory(work);
        
        StubBundle bundle = new StubBundle("foo", new Version("1.0.0"));
        
        WorkArea service = (WorkArea) factory.getService(bundle, null);
        assertNotNull(service);
        assertEquals(bundle, service.getOwner());
    }
}
