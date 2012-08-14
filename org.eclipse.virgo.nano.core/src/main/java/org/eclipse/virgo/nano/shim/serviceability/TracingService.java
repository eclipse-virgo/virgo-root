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

package org.eclipse.virgo.nano.shim.serviceability;

/**
 * <p>
 * The Platform's tracing service.
 * <p/>
 * <p>
 * An instance of this interface is available from the OSGi service registry.
 * </p>
 * <p>
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread safe.
 * </p>
 * 
 */
public interface TracingService {

    /**
     * Informs the tracing service that the calling thread is within the scope of the application identified by
     * <code>applicationName</code>. This setting can be cleared, e.g. because the calling thread is no longer in the
     * scope of an application, by calling this method with <code>null</code>.
     * 
     * @param applicationName The name of the application that is in scope on the calling thread.
     */
    void setCurrentApplicationName(String applicationName);

    /**
     * Returns the name of the application that is currently associated with the current thread
     * 
     * @return The name of the application associated with the thread.
     */
    String getCurrentApplicationName();

}
