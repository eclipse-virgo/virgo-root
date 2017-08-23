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

package org.eclipse.virgo.nano.deployer.api;

import javax.management.MXBean;

/**
 * MBean for tracking deployer recovery. A notification of type {@value #NOTIFICATION_TYPE} is broadcasted when recovery
 * completes. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <code>must</code> be threadsafe.
 * 
 */
@MXBean
public interface RecoveryMonitor {

    /**
     * The type of notification sent when recovery completes.
     */
    public static final String NOTIFICATION_TYPE = "org.eclipse.virgo.server.recovery";

    /**
     * Indicates whether or not recovery is complete.
     * 
     * @return <code>true</code> if recovery is complete, otherwise <code>false</code>.
     */
    boolean isRecoveryComplete();
}
