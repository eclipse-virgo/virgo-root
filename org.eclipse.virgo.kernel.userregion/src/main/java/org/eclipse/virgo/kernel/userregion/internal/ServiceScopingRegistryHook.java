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

package org.eclipse.virgo.kernel.userregion.internal;

import java.util.Collection;
import java.util.Iterator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.framework.hooks.service.FindHook;

/**
 * Service registry hook that enforces the service scoping behaviour.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
@SuppressWarnings("deprecation")
final class ServiceScopingRegistryHook implements FindHook, EventHook {

	private final ServiceScopingStrategy serviceScopingStrategy;

	public ServiceScopingRegistryHook(
			ServiceScopingStrategy serviceScopingStrategy) {
		this.serviceScopingStrategy = serviceScopingStrategy;
	}

	@SuppressWarnings("unchecked")
	public void find(BundleContext context, String name, String filter,
			boolean allServices,
			@SuppressWarnings("rawtypes") Collection references) {
		this.serviceScopingStrategy.scopeReferences(references, context, name,
				filter);
	}

	@SuppressWarnings("rawtypes")
	public void event(ServiceEvent event, Collection contexts) {
		ServiceReference ref = event.getServiceReference();
		for (Iterator iterator = contexts.iterator(); iterator.hasNext();) {
			BundleContext context = (BundleContext) iterator.next();
			if (!this.serviceScopingStrategy.isPotentiallyVisible(ref, context)) {
				iterator.remove();
			}
		}
	}

}
