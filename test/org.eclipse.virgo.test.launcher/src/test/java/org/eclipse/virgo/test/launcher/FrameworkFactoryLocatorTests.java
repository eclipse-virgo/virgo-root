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

package org.eclipse.virgo.test.launcher;

import org.junit.Test;
import org.osgi.framework.launch.FrameworkFactory;

import static org.junit.Assert.assertNotNull;


public class FrameworkFactoryLocatorTests {

    @Test
    public void testLocateFrameworkFactory() {
        FrameworkFactory frameworkFactory = FrameworkFactoryLocator.createFrameworkFactory();
        assertNotNull(frameworkFactory);
    }
}
