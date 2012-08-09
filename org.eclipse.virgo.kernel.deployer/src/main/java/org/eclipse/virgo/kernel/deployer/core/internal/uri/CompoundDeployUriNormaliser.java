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
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.NonNull;



/**
 * A {@link DeployUriNormaliser} implementation that calls a list of delegate
 * normalisers until the {@link URI} is normalised.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 *
 * As thread-safe as the delegates.
 *
 */
class CompoundDeployUriNormaliser implements DeployUriNormaliser {
    
    private final DeployUriNormaliser[] normalisers;
       
    public CompoundDeployUriNormaliser(@NonNull DeployUriNormaliser[] normalisers) {       
        this.normalisers = normalisers.clone();
    }

    /** 
     * {@inheritDoc}
     */
    public URI normalise(URI uri) throws DeploymentException {
        for (DeployUriNormaliser normaliser : normalisers) {
            URI normalised = normaliser.normalise(uri);
            if (normalised != null) {
                return normalised;
            }
        }
        return null;
    }
}
