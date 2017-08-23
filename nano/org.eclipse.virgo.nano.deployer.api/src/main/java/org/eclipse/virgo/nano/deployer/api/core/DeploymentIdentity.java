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

import java.io.Serializable;

/**
 * {@link DeploymentIdentity} is an interface for the serializable objects returned when an artefact is deployed.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface DeploymentIdentity extends Serializable {
    
    /**
     * Get the type of the deployed artefact with this {@link DeploymentIdentity}.
     * 
     * @return the type of the deployed artefact
     */
    String getType();
    
    /**
     * Get the name of the deployed artefact with this {@link DeploymentIdentity}.
     * 
     * @return the symbolic name of the deployed artefact
     */
    String getSymbolicName();
    
    /**
     * Get the version of the deployed artefact with this {@link DeploymentIdentity}.
     * 
     * @return the version of the deployed artefact
     */
    String getVersion();

}
