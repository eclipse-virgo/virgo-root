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

package org.eclipse.virgo.kernel.dm.internal;

import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;

import org.eclipse.virgo.kernel.module.ServiceProxyInspector;

/**
 */
final class StandardServiceProxyInspector implements ServiceProxyInspector {

    /**
     * {@inheritDoc}
     */
    public boolean isLive(Object proxy) {
        if (proxy instanceof ImportedOsgiServiceProxy) {
            return ((ImportedOsgiServiceProxy) proxy).getServiceReference().getBundle() != null;
        } else {
            return true;
        }
    }

}
