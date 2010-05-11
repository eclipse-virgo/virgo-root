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

package org.eclipse.virgo.kernel.deployer.hot;

import org.eclipse.virgo.kernel.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

public enum HotDeployerLogEvents implements LogEvent {

    HOT_DEPLOY_PROCESSING_FILE(1, Level.INFO), //
    HOT_DEPLOY_FAILED(2, Level.ERROR), //
    HOT_REDEPLOY_FAILED(3, Level.ERROR), //
    HOT_UNDEPLOY_FAILED(4, Level.ERROR), //
    HOT_DEPLOY_SKIPPED(5, Level.INFO);

    private static final String PREFIX = "HD";

    private final LogEventDelegate delegate;

    private HotDeployerLogEvents(int code, Level level) {
        this.delegate = new LogEventDelegate(PREFIX, code, level);
    }

    /**
     * {@inheritDoc}
     */
    public String getEventCode() {
        return this.delegate.getEventCode();
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel() {
        return this.delegate.getLevel();
    }
}
