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

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.BundleActivationPolicy;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;


class StandardBundleActivationPolicy extends BaseParameterised implements BundleActivationPolicy {

    StandardBundleActivationPolicy(HeaderParser parser) {
        super(parser);
    }

    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        return parser.parseBundleActivationPolicy(parseString);
    }

    /** 
     * {@inheritDoc}
     */
    public Policy getActivationPolicy() {        
        if (Constants.ACTIVATION_LAZY.equals(this.name)) {
            return Policy.LAZY;
        } else {
            return Policy.EAGER;
        }        
    }

    /** 
     * {@inheritDoc}
     */
    public void setActivationPolicy(Policy policy) {
        if (Policy.LAZY.equals(policy)) {
            this.name = Constants.ACTIVATION_LAZY;
        } else {
            this.name = null;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getExclude() {
        return HeaderUtils.toList(Constants.EXCLUDE_DIRECTIVE, getDirectives());
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getInclude() {
        return HeaderUtils.toList(Constants.INCLUDE_DIRECTIVE, getDirectives());
    }
}
