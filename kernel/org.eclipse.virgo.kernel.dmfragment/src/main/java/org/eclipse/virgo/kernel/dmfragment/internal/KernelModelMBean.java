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

package org.eclipse.virgo.kernel.dmfragment.internal;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.springframework.jmx.export.SpringModelMBean;


/**
 * An extension of {@link SpringModelMBean} that acts as a Server-specific MBeans.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * As thread-safe as <code>SpringModelMBean</code>.
 */
final class KernelModelMBean extends SpringModelMBean {

    private final TracingService tracingService;

    private final String applicationName;

    public KernelModelMBean(TracingService tracingService, String applicationName) throws RuntimeOperationsException, MBeanException {
        this.tracingService = tracingService;
        this.applicationName = applicationName;
    }

    @Override
    public Object invoke(String opName, Object[] opArgs, String[] sig) throws MBeanException, ReflectionException {
        String originalApplicationName = this.tracingService.getCurrentApplicationName();

        try {
            this.tracingService.setCurrentApplicationName(this.applicationName);
            return super.invoke(opName, opArgs, sig);
        } finally {
            this.tracingService.setCurrentApplicationName(originalApplicationName);
        }
    }
}
