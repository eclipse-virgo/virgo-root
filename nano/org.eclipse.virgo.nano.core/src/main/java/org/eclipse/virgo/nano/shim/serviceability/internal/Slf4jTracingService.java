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

package org.eclipse.virgo.nano.shim.serviceability.internal;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.slf4j.MDC;


public final class Slf4jTracingService implements TracingService {

    public static final String APPLICATION_NAME = "applicationName";

    public String getCurrentApplicationName() {
        return MDC.get(APPLICATION_NAME);
    }

    public void setCurrentApplicationName(String applicationName) {
        MDC.put(APPLICATION_NAME, applicationName);
    }

}
