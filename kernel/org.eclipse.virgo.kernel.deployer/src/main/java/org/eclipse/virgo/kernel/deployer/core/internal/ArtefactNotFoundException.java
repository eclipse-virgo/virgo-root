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

package org.eclipse.virgo.kernel.deployer.core.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;


/**
 * Thrown when an artefact is not found, i.e. the artefact does not exist.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 * 
 */
public final class ArtefactNotFoundException extends DeploymentException {

    private static final long serialVersionUID = 7374184559342205863L;
    
    /**
     * @param message The exception's message
     */
    public ArtefactNotFoundException(String message) {
        super(message);
    }
}
