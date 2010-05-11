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
 * {@link PropertyTypeMismatchException} is thrown when a bean could not be created since a value of the wrong type was passed to it. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class PropertyTypeMismatchException extends ComponentException {

    private static final long serialVersionUID = -1625186095560002162L;
    
    private final Class<?> requiredClass;
    
    private final Object value;

    /**
     * @param beanName of bean in which mismatch occurs
     * @param cause Throwable causing detection
     * @param requiredClass class expected of the value
     * @param value actual value supplied
     */
    public PropertyTypeMismatchException(String beanName, Throwable cause, Class<?> requiredClass, Object value) {
        super(beanName, cause);
        this.requiredClass = requiredClass;
        this.value = value;
    }
    
    /**
     * @return the class required by the value
     */
    public Class<?> getRequiredClass() {
        return requiredClass;
    }

    
    /**
     * @return the actual value supplied
     */
    public Object getValue() {
        return value;
    }

}
