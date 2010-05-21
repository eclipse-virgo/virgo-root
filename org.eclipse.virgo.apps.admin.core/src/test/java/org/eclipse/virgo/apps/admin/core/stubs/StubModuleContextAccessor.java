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

package org.eclipse.virgo.apps.admin.core.stubs;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.module.Component;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.module.NoSuchComponentException;


/**
 */
public class StubModuleContextAccessor implements ModuleContextAccessor {
    
    /** 
     * {@inheritDoc}
     */
    public ModuleContext getModuleContext(Bundle arg0) {
        return new ModuleContext() {
            
            public String getDisplayName() {
                return "testDisplayName";
            }
            
            public String[] getComponentNames() {
                return new String[0];
            }
            
            public Component getComponent(String arg0) throws NoSuchComponentException {
                throw new NoSuchComponentException("fail");
            }
            
            public Object getApplicationContext() {
                return new Object();
            }
        };
    }

}
