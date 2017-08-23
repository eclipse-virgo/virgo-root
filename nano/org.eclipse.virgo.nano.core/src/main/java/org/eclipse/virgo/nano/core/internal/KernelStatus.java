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

package org.eclipse.virgo.nano.core.internal;

final class KernelStatus implements KernelStatusMBean {

    static final String STATUS_STARTING = "STARTING";

    static final String STATUS_STARTED = "STARTED";

    private volatile String status = STATUS_STARTING;

    public String getStatus() {
        return this.status;
    }

    public void setStarted() {
        this.status = STATUS_STARTED;
    }

}
