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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 */
@Ignore("[DMS-2882] Bundle activation policy is current ignored by the pipelined deployer")
public class BundleActivationPolicyTests extends AbstractParTests {
    
    private static final String MODULE_A_BUNDLE_SYMBOLIC_NAME = "bundle.activation.policy.module.a";
    private static final String MODULE_B_BUNDLE_SYMBOLIC_NAME = "bundle.activation.policy.module.b";
    
    @Test
    public void lazyActivationPolicy() throws Throwable {
        deploy(new File("src/test/resources/bundle-activation-policy.par"));
        Bundle[] bundles = this.framework.getBundleContext().getBundles();
        Bundle moduleA = null;
        Bundle moduleB = null;
        
        for (Bundle bundle : bundles) {
            String bundleSymbolicName = (String)bundle.getHeaders().get("Bundle-SymbolicName");
            if (bundleSymbolicName != null) {
                if (bundleSymbolicName.endsWith(MODULE_A_BUNDLE_SYMBOLIC_NAME)) {
                    moduleA = bundle;
                    if (moduleB != null) {
                        break;
                    }
                } else if (bundleSymbolicName.endsWith(MODULE_B_BUNDLE_SYMBOLIC_NAME)) {
                    moduleB = bundle;
                    if (moduleA != null) {
                        break;
                    }
                }
            }
        }
        
        assertNotNull(moduleA);
        assertNotNull(moduleB);
        
        assertTrue(moduleA.getState() == Bundle.ACTIVE);
        assertTrue(moduleB.getState() == Bundle.STARTING);
        
        Class<?> clazz = moduleA.loadClass("a.UseBundleB");
        Object instance = clazz.newInstance();
        assertNotNull(clazz.getMethod("getClassInBundleB", (Class[])null).invoke(instance, (Object[])null));
        
        assertTrue(moduleA.getState() == Bundle.ACTIVE);
        assertTrue(moduleB.getState() == Bundle.ACTIVE);
    }
}
