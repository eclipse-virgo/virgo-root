/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.config.internal;

import org.eclipse.virgo.nano.core.KernelConfig;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is the standard implementation for the KernelConfig interface. It enables access to the configurations provided by the kernel core.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */

public class StandardKernelConfig implements KernelConfig {
	
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
        LOGGER.debug("Activating StandardKernelConfig component with properties " + context.getProperties());
    }

    protected void deactivate(ComponentContext context) {
        LOGGER.debug("Deactivating StandardKernelConfig component");
        this.context = null;
    }

    @Override
    public String getProperty(String name) {
        Object value = this.context.getProperties().get(name);
        LOGGER.debug(KernelConfig.class.getSimpleName() + ".getProperty() is called with name [" + name + "] value is [" + value + "]");
        if (value instanceof String || value == null) {
            return (String) value;
        } else if (value instanceof String[] && ((String[]) value).length > 0) {
            return ((String[]) value)[0];
        } else {
            return value.toString();
        }
    }

}
