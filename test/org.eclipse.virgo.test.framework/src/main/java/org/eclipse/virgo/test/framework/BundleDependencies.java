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

package org.eclipse.virgo.test.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BundleDependencies defines class-level metadata which can be used to instruct test framework with extra bundles to
 * append to framework (system bundle) during start up.
 * <p />
 * This set of bundles is considered to be <em>transitively complete</em>, that is all dependencies must be provided.
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BundleDependencies {

    
    /**
     * Bundle entries to load as part of the test framework.
     * 
     * @return
     */
    BundleEntry[] entries() default {};
    
    /**
     * Whether or not {@link #entries() entries} from superclasses should be <em>inherited</em>.
     * <p>Default value is <code>true</code>, which means that entries from subclass will be
     * <em>appended</em> to the list of entries from superclass.
     * 
     * @return
     */
    boolean inheritDependencies() default true;
}
