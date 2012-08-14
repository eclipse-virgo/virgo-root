/*******************************************************************************
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal;

import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * StandardSpringContextAccessor avoids issues of multiple Spring frameworks 
 * being present by not using any spring types.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 */
public class StandardSpringContextAccessor implements SpringContextAccessor {
    
    private static final String APPLICATION_CONTEXT_CLASS_NAME = "org.springframework.context.ApplicationContext";
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean isSpringPowered(@NonNull Bundle bundle){
        BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext != null) {
            String symbolicName = bundle.getSymbolicName();
            try {
                ServiceReference<?>[] refs = bundleContext.getAllServiceReferences(APPLICATION_CONTEXT_CLASS_NAME, "(Bundle-SymbolicName=" + symbolicName + ")");
                if (refs != null && refs.length > 0) {
                    return true;
                }
            } catch (InvalidSyntaxException e) {
                Assert.isFalse(true, "Unexpected exception %s", e.getMessage());
            }
        }
        return false;      
    }
    
}
