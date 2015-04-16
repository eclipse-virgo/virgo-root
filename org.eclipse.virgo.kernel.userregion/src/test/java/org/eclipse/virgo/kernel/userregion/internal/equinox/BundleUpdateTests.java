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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class BundleUpdateTests extends AbstractOsgiFrameworkLaunchingTests {
    
    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/BundleUpdateTests").getAbsolutePath();
    }

    /*
     * This test checks the behaviour of class loading during a bundle update operation. The test passes on Mac OS X, but
     * it seems unlikely to pass on some other operating systems. The basic question is how can classes be loaded from the
     * old version of a bundle after the bundle has been updated if install by reference is used.
     * 
     * The test installs two bundles but.B and but.C and starts but.C. but.C loads class but.A which is exported
     * by but.B and listens synchronously for an update event for bundle but.B. This test method overwrites bundle but.B
     * and issues an update for the bundle. The new version of but.B contains a new version of class but.A.
     * 
     * The old version of bundle but.B also contains a class but.B which depends on the class but.A. The new version
     * of bundle but.B contains a new version of class but.B which depends on the new version of but.A and which will fail
     * if it is loaded with the old version of but.A.
     * 
     * When the synchronous bundle listener in bundle but.C is notified of the update of bundle but.B, it loads the class
     * but.B. This will succeed if it is loads the old version of the class but will fail if it loads the new version of the
     * class.
     */
    @Test
    @Ignore("See DMS-2886")
    public void testUpdate() throws Exception {
        PathReference bBeforeSrc = new PathReference("src/test/resources/but/but.B.before.jar");
        PathReference b = new PathReference("build/but.B.jar");
        b.delete();
        bBeforeSrc.copy(b);
        Bundle bBundle = this.framework.getBundleContext().installBundle(b.toFile().toURI().toString());
        assertNotNull(bBundle);
        
        PathReference cSrc = new PathReference("src/test/resources/but/but.C.jar");
        PathReference c = new PathReference("build/but.C.jar");
        c.delete();
        cSrc.copy(c);
        Bundle cBundle = this.framework.getBundleContext().installBundle(c.toFile().toURI().toString());
        assertNotNull(cBundle);
        cBundle.start();
        
        PathReference bAfterSrc = new PathReference("src/test/resources/but/but.B.after.jar");
        b.delete();
        bAfterSrc.copy(b);
        bBundle.update();
        
    }

}
