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
 * {@link Component} is an abstraction of a Spring bean which is not bound to Spring types.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public interface Component {
    
    /**
     * Returns this component's name.
     * 
     * @return the component name
     */
    String getName();
    
    /**
     * Returns this component's concrete type as a class name.
     * 
     * @return the name of the component's type
     */
    String getType();
    
    /**
     * Returns whether this component is a prototype.
     * 
     * @return <code>true</code> if and only if this component is a prototype
     */
    boolean isPrototype();
    
    /**
     * Returns whether this component is a singleton.
     * 
     * @return <code>true</code> if and only if this component is a singleton
     */
    boolean isSingleton();

}
