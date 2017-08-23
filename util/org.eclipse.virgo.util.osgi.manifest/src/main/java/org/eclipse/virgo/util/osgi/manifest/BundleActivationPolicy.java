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

package org.eclipse.virgo.util.osgi.manifest;

import java.util.List;

/**
 * Represents a bundle's <code>Bundle-ActivationPolicy</code> header.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface BundleActivationPolicy extends Parameterised {

    /**
     * An enumeration of the legal values for a bundle's activation policy, 
     * specified using the <code>Bundle-ActivationPolicy</code> header.
     * <p />
     */    
    enum Policy {
        /**
         * The bundle will be activated eagerly.
         */
        EAGER,
        /**
         * The bundle will be activated lazily.
         */
        LAZY;
    }

    /**
     * Returns the value of the <code>Bundle-ActivationPolicy</code> header. If no header
     * is specified returns the default value of {@link Policy#EAGER}.
     * @return the value of the <code>Bundle-ActivationPolicy</code> header.
     */
    Policy getActivationPolicy();
    
    /**
     * Sets the value of the <code>Bundle-ActivationPolicy</code> header.
     * 
     * @param policy the bundle's activation policy.
     */
    void setActivationPolicy(Policy policy);
    
    /**
     * Returns a list of the class names specified in the header's <code>exclude</code> directive. Returns an empty list
     * if the header has no <code>exclude</code> directive.
     * 
     * @return the list of exclusions
     */
    List<String> getExclude();
    
    /**
     * Returns a list of the class names specified in the header's <code>include</code> directive. Returns an empty list
     * if the header has no <code>include</code> directive.
     * 
     * @return the list of inclusions
     */
    List<String> getInclude();
}
