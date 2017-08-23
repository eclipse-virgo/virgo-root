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
 * Signals an error resolving the dependencies of a bundle or library during installation.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
@SuppressWarnings("serial")
public abstract class UnableToSatisfyDependenciesException extends Exception {

    private final String symbolicName;

    private final Version version;

    private final String failureDescription;

    /**
     * Creates a new <code>UnableToSatisfyDependenciesException</code>.
     * 
     * @param symbolicName the symbolic name of the entity that failed to install.
     * @param version the version of the entity that failed to install.
     * @param failureDescription the description of the dependency satisfaction problem.
     */
    protected UnableToSatisfyDependenciesException(String entity, String symbolicName, Version version, String failureDescription) {
        super(formatMessage(entity, symbolicName, version, failureDescription));
        this.symbolicName = symbolicName;
        this.version = version;
        this.failureDescription = failureDescription;
    }

    /**
     * @return the symbolic name of the entity that failed to install.
     */
    public String getSymbolicName() {
        return this.symbolicName;
    }

    /**
     * @return the version of the entity that failed to install.
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * @return the full description of the dependencies that could not be satisfied.
     */
    public String getFailureDescription() {
        return this.failureDescription;
    }

    /**
     * Creates a formatted message to describe the dependency failure.
     */
    private static String formatMessage(String entity, String symbolicName, Version version, String failureDescription) {
        return "Unable to satisfy dependencies of " + entity + " '" + symbolicName + "' at version '" + version + "': " + failureDescription;
    }
    
    /**
     * Get any {@link State} associated with this exception.
     * 
     * @return either <code>null</code> or a <code>State</code>
     */
    public State getState() {
        return null;
    }
    
    /**
     * Get any {@link ResolverError}s associated with this exception.
     * 
     * @return either <code>null</code> or a possibly empty array of <code>ResolverError</code>s
     */
    public ResolverError[] getResolverErrors() {
        return null;
    }
}
