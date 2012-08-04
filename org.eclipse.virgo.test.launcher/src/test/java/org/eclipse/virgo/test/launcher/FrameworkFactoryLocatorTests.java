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

package org.eclipse.virgo.osgi.launcher;

import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.osgi.launcher.FrameworkFactoryLocator;
import org.junit.Test;
import org.osgi.framework.launch.FrameworkFactory;


public class FrameworkFactoryLocatorTests {

    @Test
    public void testLocateFrameworkFactory() {
        FrameworkFactory frameworkFactory = FrameworkFactoryLocator.createFrameworkFactory();
        assertNotNull(frameworkFactory);
    }
}
