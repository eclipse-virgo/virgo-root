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

package org.eclipse.virgo.web.enterprise.openejb.deployer.log;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum OpenEjbDeployerLogEvents implements LogEvent {

        DEPLOYED_APP(0, Level.INFO),
        FAILED_TO_DEPLOY_APP(1, Level.WARNING),
        UNDEPLOYED_APP(2, Level.INFO),
        FAILED_TO_UNDEPLOY_APP(3, Level.WARNING);

        private static final String PREFIX = "OE";
        
        private final int code;
        
        private final Level level;

        private OpenEjbDeployerLogEvents(int code, Level level) {
            this.code = code;
            this.level = level;        
        }

        /**
         * {@inheritDoc}
         */
        public String getEventCode() {
            return String.format("%s%04d%1.1s", PREFIX, this.code, this.level);
        }

        /**
         * {@inheritDoc}
         */
        public Level getLevel() {
            return this.level;
        }

    }

