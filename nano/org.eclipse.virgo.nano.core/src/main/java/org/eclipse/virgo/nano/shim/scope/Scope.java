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

package org.eclipse.virgo.nano.shim.scope;

/**
 * {@link Scope} defines a collection of bundles, packages, and services. There is a global scope as well as more local scopes such as those used to contain
 * multi-bundle applications.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of this interface must be thread safe.
 *
 */
public interface Scope {
    
    public static final String PROPERTY_SERVICE_SCOPE = "org.eclipse.virgo.service.scope";
    
    public static final String SCOPE_ID_GLOBAL = "global";
    
    public static final String SCOPE_ID_APP = "app";

    /**
     * Return <code>true</code> if and only if this scope is global.
     * 
     * @return <code>true</code> if and only if this scope is global
     */
    boolean isGlobal();
    
    /**
     * Return the name of this scope or <code>null</code> if this scope is global.
     * 
     * @return the name of this scope or <code>null</code> if this scope is global
     */
    String getScopeName();
    
    /**
     * Get the value of the scope property with the given name or <code>null</code> if the scope has no such property.
     * 
     * @param propertyName the name of the property
     * @return the value of the property or <code>null</code> if the scope has no such property
     */
    Object getProperty(String propertyName);
    
    /**
     * Set the value of the scope property with the given name to the given value.
     * 
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     */
    void setProperty(String propertyName, Object propertyValue);
}
