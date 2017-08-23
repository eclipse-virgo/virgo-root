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

package org.eclipse.virgo.kernel.osgi.framework;

import org.osgi.framework.ServiceReference;

/**
 * An <code>OsgiServiceHolder</code> holds a service retrieved from the OSGi service registry and a
 * <code>ServiceReference</code> for the held service.
 * <p />
 * An <code>OsgiServiceHolder</code> is {@link Comparable} and uses the encapsulated {@link ServiceReference}. to
 * determine its ordering.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 * @param <T> Type of service to hold
 */
public interface OsgiServiceHolder<T> extends Comparable<OsgiServiceHolder<?>> {

    T getService();

    ServiceReference<T> getServiceReference();
}
