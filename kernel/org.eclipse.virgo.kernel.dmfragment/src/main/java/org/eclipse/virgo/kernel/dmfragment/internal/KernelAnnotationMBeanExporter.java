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
import javax.management.modelmbean.ModelMBean;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;


/**
 * An extension of {@link AnnotationMBeanExporter} that exports Kernel-specific MBeans.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * As thread-safe as <code>MBeanExporter</code>.
 */
final class KernelAnnotationMBeanExporter extends AnnotationMBeanExporter {

    private final TracingService tracingService;

    KernelAnnotationMBeanExporter(TracingService tracingService) {
        this.tracingService = tracingService;
    }

    @Override
    protected ModelMBean createModelMBean() throws MBeanException {
        return new KernelModelMBean(this.tracingService, this.tracingService.getCurrentApplicationName());
    }
}
