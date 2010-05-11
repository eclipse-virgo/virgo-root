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

package org.eclipse.virgo.kernel.deployer.core;

/**
 * {@link ServerModuleInfo} provides management information for a deployed module of an application.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ServerModuleInfo extends Comparable<ServerModuleInfo>{

    /**
     * Get the personality of this module.
     * 
     * @return the personality of this module or <code>null</code> if this module is devoid of personality
     */
    String getPersonality();

    /**
     * Get an identifier of this module which uniquely identifies the module among all modules of the same personality.
     * 
     * @return the personality identifier of this module or <code>null</code> if this module is devoid of personality
     */
    String getPersonalityIdentifier();
    
    /**
     * Get the symbolic name that this bundle is based on.
     * @return the symbolic bundle name as specified in the manifest header
     */
    String getBundleSymbolicName();
    
}


