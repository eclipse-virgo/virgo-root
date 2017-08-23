/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2010 Chariot Solutions
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsklyut - initial contribution
 */

package org.eclipse.virgo.test.framework;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * BundleEntry defines a URI of the bundle to install and autoStart flag that signals to framework that particular entry
 * must be started as well as installed.
 * <p />
 * Note: This annotation is used by {@link BundleDependencies} and not designed to be used stand-alone
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface BundleEntry {

    /**
     * URI of the bundle to install.  Must be a valid {@link URI} format.
     * @return
     */
    String value();

    /**
     * (* Optional *)
     * Flag to auto start bundle after install into framework.
     * By default it is true.
     * 
     * For fragment bundles - make sure to set it to false
     * @return
     */
    boolean autoStart() default true;
}