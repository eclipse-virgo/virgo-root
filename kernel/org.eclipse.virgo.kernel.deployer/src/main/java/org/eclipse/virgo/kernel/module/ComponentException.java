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
 * {@link ComponentException} is thrown when a bean could not be created. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public abstract class ComponentException extends Exception {

    private static final long serialVersionUID = 2389586771459082654L;

    private final String beanName;

    /**
     * Construct a {@link ComponentException} with the given bean name caused by the given exception.
     * 
     * @param beanName the name of the bean that could not be created
     * @param cause the exception used to construct this exception
     */
    public ComponentException(String beanName, Throwable cause) {
        super("Could not create bean '" + beanName + "'", cause);
        this.beanName = beanName;
    }

    /**
     * @return the name of the bean that could not be created.
     */
    public String getBeanName() {
        return beanName;
    }

}
