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
 * {@link ComponentClassLoadingException} is thrown when a bean could not be created since its class cannot be loaded.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class ComponentClassLoadingException extends ComponentException {

    private static final long serialVersionUID = 8914671915110743419L;

    private final String beanClassName;

    private final ClassLoader classLoader;

    public ComponentClassLoadingException(String beanName, Throwable cause, String beanClassName, ClassLoader classLoader) {
        super(beanName, cause);
        this.beanClassName = beanClassName;
        this.classLoader = classLoader;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
