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

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Used to expose management information about a deployed application, and it's 
 * constituent modules/deployed bundles. See {@link ServerModuleInfo ServerModuleInfo}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be Thread Safe
 * 
 */
public interface ServerApplicationInfo {

    /**
     * @return The name of the application as specified in the manifest, otherwise something sensible
     */
    String getName();

    /**
     * @return The specified version of the application or '0' if unknown
     */
    String getVersion();

    /**
     * @return The number of milliseconds since midnight, January 1, 1970 UTC, when this application was deployed
     */
    Date getDeployTime();

    /**
     * @return The type of the application, OSGi/non-OSGi etc...
     */
    String getType();

    /**
     * Get the {@link ServerModuleInfo ServerModuleInfos} of all the modules of this application.
     * 
     * @return this application's {@link ServerModuleInfo ServerModuleInfos}
     */
    List<ServerModuleInfo> getServerModuleInfo();
    
    /**
     * Get the URI used to deploy this application.
     * 
     * @return the string form of the URI
     */
    String getURI();

}
