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

package org.eclipse.virgo.kernel.services.concurrent;

import java.util.concurrent.ExecutorService;

import org.eclipse.virgo.kernel.services.concurrent.KernelScheduledThreadPoolExecutor;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;


/**
 */
public class KernelScheduledThreadPoolExecutorTests extends AbstractExecutorTests {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExecutorService getExecutor() {
        return getNamed("foo");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExecutorService getNamed(String name) {
        return new KernelScheduledThreadPoolExecutor(1, name, new StubTracingService());
    }

    private static final class StubTracingService implements TracingService {

        public String getCurrentApplicationName() {
            return null;
        }

        public void setCurrentApplicationName(String applicationName) {
        }

    }

}
