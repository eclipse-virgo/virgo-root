/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.apache.openejb.config.DynamicDeployer;
import org.eclipse.virgo.medic.eventlog.EventLogger;

public class OpenEjbDeployerDSComponent {

    static EventLogger eventLogger = null;
    static DynamicDeployer dynamicDeployer = null;

    static EventLogger getEventLogger() {
        return eventLogger;
    }

    static DynamicDeployer getDynamicDeployer() {
        return dynamicDeployer;
    }

    public void bindEventLogger(EventLogger logger) {
        eventLogger = logger;
    }

    public void unbindEventLogger(EventLogger logger) {
        eventLogger = null;
    }

    public void bindDynamicDeployer(DynamicDeployer deployer) {
        dynamicDeployer = deployer;
    }

    public void unbindDynamicDeployer(DynamicDeployer deployer) {
        dynamicDeployer = null;
    }

}
