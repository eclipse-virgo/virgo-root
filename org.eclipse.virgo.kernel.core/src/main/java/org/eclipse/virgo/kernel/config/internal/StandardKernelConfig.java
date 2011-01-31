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

package org.eclipse.virgo.kernel.config.internal;

import org.eclipse.virgo.kernel.core.KernelConfig;
import org.osgi.service.component.ComponentContext;

public class StandardKernelConfig implements KernelConfig {

    ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
        System.out.println("Activating StandardKernelConfig component with properties " + context.getProperties());
    }

    protected void deactivate(ComponentContext context) {
        System.out.println("Deactivating StandardKernelConfig component");
        this.context = null;
    }

    @Override
    public String getProperty(String name) {
        Object value = this.context.getProperties().get(name);
        System.out.println("KernelConfig.getProperty() is called with name [" + name + "] value is [" + value + "]");
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof String[] && ((String[]) value).length > 0) {
            return ((String[]) value)[0];
        } else {
            return value.toString();
        }
    }

}
