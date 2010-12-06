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

package org.eclipse.virgo.test.framework.dmkernel;

import java.util.Collection;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


/**
 * JUnit TestRunner for running OSGi integration tests on the dm Kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * As thread-safe as OsgiTestRunner
 * 
 */
public class DmKernelTestRunner extends OsgiTestRunner {

    private static final long DEFAULT_USER_REGION_START_WAIT_TIME = 60000;
    
    private final long userRegionStartWaitTime;

    public DmKernelTestRunner(Class<?> klass) throws InitializationError {
        this(klass, DEFAULT_USER_REGION_START_WAIT_TIME);
    }
    
    protected DmKernelTestRunner(Class<?> klass, long userRegionStartWaitTime) throws InitializationError {
        super(klass);
        this.userRegionStartWaitTime = userRegionStartWaitTime;
    }

    @Override
    protected BundleContext getTargetBundleContext(BundleContext bundleContext) {
        Collection<ServiceReference<BundleContext>> serviceReferences = getUserRegionBundleContextServiceReferences(bundleContext);

        if (serviceReferences != null) {
            if (serviceReferences.size() != 1) {
                throw new IllegalStateException("There must be exactly one user region bundle context in the service registry. "
                    + serviceReferences.size() + " were found.");
            } else {
                BundleContext targetBundleContext = (BundleContext) bundleContext.getService(serviceReferences.iterator().next());
                if (targetBundleContext != null) {
                    return targetBundleContext;
                }
            }
        }
        throw new IllegalStateException("User region's bundle context was not available from the service registry within " + (this.userRegionStartWaitTime / 1000) + " seconds.");
    }

    private Collection<ServiceReference<BundleContext>> getUserRegionBundleContextServiceReferences(BundleContext bundleContext) {
                       
        long startTime = System.currentTimeMillis();
        
        Collection<ServiceReference<BundleContext>> serviceReferences = doGetUserRegionBundleContextServiceReferences(bundleContext);
        
        while (serviceReferences.size() == 0) {
            if (System.currentTimeMillis() < (this.userRegionStartWaitTime + startTime)) {
                try {
                    Thread.sleep(500);
                    serviceReferences = doGetUserRegionBundleContextServiceReferences(bundleContext);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            } else {
                break;
            }
        }                
        return serviceReferences;
    }
    
    private Collection<ServiceReference<BundleContext>> doGetUserRegionBundleContextServiceReferences(BundleContext bundleContext) {
        try {            
            return bundleContext.getServiceReferences(BundleContext.class, "(org.eclipse.virgo.kernel.regionContext=true)");
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException when looking up the user region's BundleContext", e);
        }
    }
}
