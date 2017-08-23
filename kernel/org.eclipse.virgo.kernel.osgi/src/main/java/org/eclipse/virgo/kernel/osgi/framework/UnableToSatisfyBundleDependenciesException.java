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

package org.eclipse.virgo.kernel.osgi.framework;

import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Version;

/**
 * Signals an error resolving the dependencies of a bundle during installation.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class UnableToSatisfyBundleDependenciesException extends UnableToSatisfyDependenciesException {

    private static final long serialVersionUID = -4291846255969729124L;

    private static final String BUNDLE_ENTITY = "bundle";

    private final State state;
    
    private final ResolverError[] resolverErrors;

    /**
     * Creates a new <code>UnableToSatisfyBundleDependenciesException</code>.
     * 
     * @param symbolicName the symbolic name of the bundle.
     * @param version the bundle version.
     * @param failureDescription a description of the failure.
     */
    public UnableToSatisfyBundleDependenciesException(String symbolicName, Version version, String failureDescription) {
        super(BUNDLE_ENTITY, symbolicName, version, failureDescription);
        this.state = null;
        this.resolverErrors = null;
    }

    /**
     * Creates a new <code>UnableToSatisfyBundleDependenciesException</code>.
     * 
     * @param symbolicName the symbolic name of the bundle.
     * @param version the bundle version.
     * @param failureDescription a description of the failure.
     * @param state of bundle
     * @param resolverErrors a possibly empty array of {@linkplain ResolverError}s.
     */
    public UnableToSatisfyBundleDependenciesException(String symbolicName, Version version, String failureDescription, State state, ResolverError[] resolverErrors) {
        super(BUNDLE_ENTITY, symbolicName, version, failureDescription);
        this.state = state;
        this.resolverErrors = resolverErrors.clone();
    }
    
    /** 
     * {@inheritDoc}
     */
    public final State getState() {
        return this.state;
    }

    /** 
     * {@inheritDoc}
     */
    public final ResolverError[] getResolverErrors() {
        return this.resolverErrors.clone();
    }
}
