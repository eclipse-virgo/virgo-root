/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2010 Chariot Solutions, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsklyut - initial contribution
 */

package org.eclipse.virgo.test.framework.dmkernel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.virgo.test.framework.BundleEntry;


/**
 * UserRegionBundleDependencies defines class-level metadata which can be used to instruct test framework with extra bundles to
 * append to <a href="http://wiki.eclipse.org/Virgo/Concepts#Regions"> UserRegion</a> during start up.
 * <p />
 * This set of bundles is considered to be "transitively complete", that is all dependencies must be provided.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RegionBundleDependencies {
    
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
