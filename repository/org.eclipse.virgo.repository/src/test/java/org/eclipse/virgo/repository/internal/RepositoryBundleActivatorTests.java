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

package org.eclipse.virgo.repository.internal;

import org.junit.Test;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.internal.RepositoryBundleActivator;
import org.eclipse.virgo.test.stubs.framework.OSGiAssert;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;

public class RepositoryBundleActivatorTests {
    
    @Test
    public void servicePublicationAndRetraction() throws Exception {
        
        RepositoryBundleActivator activator = new RepositoryBundleActivator();
        StubBundleContext context = new StubBundleContext();  
        context.addFilter(new ObjectClassFilter(EventLogger.class));
        
        activator.start(context);
        
        OSGiAssert.assertServiceRegistrationCount(context, RepositoryFactory.class, 1);
        OSGiAssert.assertServiceRegistrationCount(context, DumpContributor.class, 1);
        
        activator.stop(context);
        
        OSGiAssert.assertServiceRegistrationCount(context, RepositoryFactory.class, 0);
        OSGiAssert.assertServiceRegistrationCount(context, DumpContributor.class, 0);
    }
    
    @Test
    public void stopWithoutRegistrationInStart() throws Exception {
        BundleContext context = new StubBundleContext();
        new RepositoryBundleActivator().stop(context);
    }
}
