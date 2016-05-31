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

package org.eclipse.virgo.kernel.deployer.app.spring;

import java.util.Collection;

import org.eclipse.virgo.kernel.module.KernelModuleContextAccessorFactory;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

/**
 * {@link UserRegionModuleContextAccessor} accesses {@link ModuleContext ModuleContexts} in either the the user region or the kernel, but this class must
 * be loaded in the user region.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class UserRegionModuleContextAccessor implements ModuleContextAccessor {
    
    private final ModuleContextAccessor kernelModuleContextAccessor;

    UserRegionModuleContextAccessor() {
        this.kernelModuleContextAccessor = KernelModuleContextAccessorFactory.create();
    }

    /**
     * {@inheritDoc}
     */
    public ModuleContext getModuleContext(@NonNull Bundle bundle) {
        BundleContext bundleContext = bundle.getBundleContext();
        // The bundle must have a bundle context in order to have a module context.
        if (bundleContext != null) {
            String symbolicName = bundle.getSymbolicName();
            try {
                Collection<ServiceReference<ApplicationContext>> refs = bundleContext.getServiceReferences(ApplicationContext.class,
                    "(Bundle-SymbolicName=" + symbolicName + ")");
                if (refs.size() != 0) {
                    for (ServiceReference<ApplicationContext> ref : refs) {
                        Object service = bundleContext.getService(ref);
                        try {
                            // Avoid kernel region application contexts.
                            if (service instanceof ApplicationContext) {
                                ApplicationContext appCtx = (ApplicationContext) service;
                                if (appCtx instanceof ConfigurableOsgiBundleApplicationContext) {
                                    if (bundleContext == ((ConfigurableOsgiBundleApplicationContext) appCtx).getBundleContext()) {
                                        return new ModuleContextWrapper((ConfigurableOsgiBundleApplicationContext) appCtx);
                                    }
                                }
                            }
                        } finally {
                            bundleContext.ungetService(ref);
                        }
                    }
                }
            } catch (InvalidSyntaxException e) {
                Assert.isFalse(true, "Unexpected exception %s", e.getMessage());
            }
        }
        // Try the kernel in case the bundle resides in the kernel region.
        return this.kernelModuleContextAccessor.getModuleContext(bundle);
    }

}
