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

package test;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;

public class TracingServiceHolder {

    private static TracingServiceHolder INSTANCE = new TracingServiceHolder();

    private volatile TracingService tracingService;

    public static TracingServiceHolder getInstance() {
        return INSTANCE;
    }

    public TracingService getTracingService() {
        return tracingService;
    }

    public void setTracingService(TracingService tracingService) {
        this.tracingService = tracingService;
    }

}
