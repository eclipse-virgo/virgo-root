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

package org.eclipse.virgo.util.test;

import java.io.File;

import org.junit.Test;

public class BundleResolutionTests extends AbstractEquinoxLaunchingTests {

    @Test
    public void bundleResolution() throws Exception {
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.common/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.math/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.io/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.env/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.parser.manifest/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.osgi/target/classes").toURI().toString()).start();
        framework.getBundleContext().installBundle(new File("../org.eclipse.virgo.util.osgi.manifest/target/classes").toURI().toString()).start();
    }

    @Override
    protected String getSystemPackages() {
        return "org.slf4j;version=1.6.4," + "org.eclipse.virgo.util.common;version=2.0.0," + "org.eclipse.virgo.util.math;version=2.0.0";
    }
}
