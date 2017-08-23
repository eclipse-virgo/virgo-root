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

package org.eclipse.virgo.kernel.deployer.core.internal.uri;

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;


/**
 * A {@link DeployUriNormaliser} implementation that normalises file: {@link URI URIs}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
class FileDeployUriNormaliser implements DeployUriNormaliser {
    
    private static final String SCHEME_FILE = "file";

    /** 
     * {@inheritDoc}
     */
    public URI normalise(URI uri) {
        if (SCHEME_FILE.equals(uri.getScheme())) {            
            return uri;
        }
        return null;
    }
}
