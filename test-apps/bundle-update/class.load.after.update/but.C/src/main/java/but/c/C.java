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

package but.c;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class C implements BundleActivator, SynchronousBundleListener {

    public void start(BundleContext context) throws Exception {

        // Load the old version of A
        Class.forName("but.A");

        context.addBundleListener(this);
    }

    public void stop(BundleContext context) throws Exception {
    }

    public void bundleChanged(BundleEvent event) {
        if ("but.B".equals(event.getBundle().getSymbolicName()) && event.getType() == BundleEvent.UPDATED) {
            try {
                // Load B. If successful, this is the old version that is compatible with the old version of A.
                Class.forName("but.B");
            } catch (Exception e) {
                e.printStackTrace();
                // Prevent the JUnit test from passing if we get to here.
                System.exit(987);
            }
        }

    }

}
