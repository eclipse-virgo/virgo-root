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

package org.eclipse.virgo.kernel.module;

/**
 * {@link ModuleContext} is the kernel standard interface to application contexts. Its purpose is to isolate the kernel
 * and server code from Spring DM and Spring application context related types which vary by Spring DM and Spring
 * release and which are multiply loaded.
 * 
 * Eventually, this interface should be able to extend an OSGi standard ModuleContext type. For this reason, this
 * interface talks about 'components' rather than 'beans'.
 * 
 */
public interface ModuleContext {
    
    /**
     * Return a user-friendly name for this {@link ModuleContext}.
     * 
     * @return a user-friendly name for this <code>ModuleContext</code>
     */
    String getDisplayName();
    
    /**
     * Return the names of all the named components in this <code>ModuleContext</code>.
     * 
     * @return an array of component names in this <code>ModuleContext</code>
     */
    String[] getComponentNames();

    /**
     * Return the component instance with the given name.
     * @param componentName the name of the component to retrieve
     * @return an instance of the component
     * @throws NoSuchComponentException if the module does not contain a component with the given name
     */
    Component getComponent(String componentName) throws NoSuchComponentException;

    /**
     * Return the application context underpinning this <code>ModuleContext</code>. <p /> This method must be used with
     * extreme caution. Any attempt to cast the return value may result in a class cast exception if the wrong type is
     * used.
     * 
     * @return the <code>ApplicationContext</code> underpinning this <code>ModuleContext</code>
     */
    Object getApplicationContext();

}
