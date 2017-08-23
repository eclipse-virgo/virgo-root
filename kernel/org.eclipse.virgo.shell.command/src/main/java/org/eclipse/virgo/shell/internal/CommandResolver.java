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

package org.eclipse.virgo.shell.internal;

import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * A <code>CommandResolver</code> is used to resolve Shell commands provided by an OSGi service. The mechanism by which
 * commands are resolved for a service are a detail of the implementation.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
interface CommandResolver {

    /**
     * Resolve commands from the supplied {@link ServiceReference} and <code>service</code>
     * 
     * @param serviceReference The reference to the service
     * @param service The service
     * @return A <code>List</code> of <code>CommandDescriptor</code>s or an empty list if no commands were found
     */
    List<CommandDescriptor> resolveCommands(ServiceReference<?> serviceReference, Object service);

}
