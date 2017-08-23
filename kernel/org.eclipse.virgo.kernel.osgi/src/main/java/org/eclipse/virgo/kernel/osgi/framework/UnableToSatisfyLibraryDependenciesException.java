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

import org.osgi.framework.Version;

/**
 * Signals an error resolving the dependencies of a library during installation.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class UnableToSatisfyLibraryDependenciesException extends UnableToSatisfyDependenciesException {

    private static final long serialVersionUID = -2705837902792185894L;

    private static final String LIBRARY_ENTITY = "library";

    /**
     * Creates a new <code>UnableToSatisfyLibraryDependenciesException</code>.
     * 
     * @param symbolicName the library symbolic name.
     * @param version the library version.
     * @param failureDescription a description of the failure.
     */
    public UnableToSatisfyLibraryDependenciesException(String symbolicName, Version version, String failureDescription) {
        super(LIBRARY_ENTITY, symbolicName, version, failureDescription);
    }
}
