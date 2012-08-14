/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api.core;

import java.net.URI;


/**
 * A <code>DeployUriNormaliser</code> is used to normalise deploy {@link URI URIs} such that they're suitable for
 * consumption by the deployer's internals.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface DeployUriNormaliser {

    /**
     * Normalises the supplied <code>uri</code> such that it is suitable for consumption by the deployer. If the
     * <code>uri</code> is not understood, <code>null</code> is returned.
     * 
     * @param uri The {@link URI} to normalise
     * @return The normalised <code>URI</code>.
     * 
     * @throws DeploymentException if the <code>uri</code> is understood but it cannot be normalised.
     */
    URI normalise(URI uri) throws DeploymentException;
}
