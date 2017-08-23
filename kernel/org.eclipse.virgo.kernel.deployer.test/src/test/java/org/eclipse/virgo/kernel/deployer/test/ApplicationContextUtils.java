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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;

final class ApplicationContextUtils {
    
    static void assertApplicationContextContainsExpectedBeanDefinitions(ApplicationContext applicationContext, String... beanNames) {        
        for (String beanName : beanNames) {
            assertTrue("A definition for a bean named " + beanName + " was not found in the application context", applicationContext.containsBeanDefinition(beanName));
        }        
    }
    
    static ApplicationContext getApplicationContext(BundleContext bundleContext, String symbolicName) {
        ServiceReference<?>[] serviceReferences = null;
        
        try {
            serviceReferences = bundleContext.getServiceReferences(ApplicationContext.class.getName(), "(Bundle-SymbolicName=" + symbolicName + ")");
        } catch (InvalidSyntaxException e) {
            fail(e.toString());
        }
        
        if (serviceReferences != null) {
            assertEquals("Found " + serviceReferences.length + " matching service references when only 1 was expected", 1, serviceReferences.length);
            return (ApplicationContext) bundleContext.getService(serviceReferences[0]);
        }
        
        return null;
    }
    
    static void awaitApplicationContext(BundleContext bundleContext, String symbolicName, long timeout) {
        long endTime = System.currentTimeMillis() + (timeout * 1000);
        
        ApplicationContext applicationContext = null;
        
        while (applicationContext == null && System.currentTimeMillis() < endTime) {
            applicationContext = getApplicationContext(bundleContext, symbolicName);
        }
        
        if (applicationContext == null) {
            fail("ApplicationContext for bundle with symbolic name '" + symbolicName + "' was not published within " + timeout + " seconds.");
        }
    }
}
