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
 * {@link InvalidComponentPropertyException} is thrown when a bean could not be created since the bean does have a particular
 * property. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class InvalidComponentPropertyException extends ComponentException {

    private static final long serialVersionUID = 6920347784034413774L;

    private final String propertyName;

    private final Class<?> beanClass;

    public InvalidComponentPropertyException(String beanName, Throwable cause, String propertyName, Class<?> beanClass) {
        super(beanName, cause);
        this.propertyName = propertyName;
        this.beanClass = beanClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

}
