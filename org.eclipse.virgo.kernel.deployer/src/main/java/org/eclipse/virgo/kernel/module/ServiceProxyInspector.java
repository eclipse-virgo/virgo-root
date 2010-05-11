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

package org.eclipse.virgo.kernel.module;

/**
 * {@link ServiceProxyInspector} is the kernel standard interface for probing
 * service proxies. Its purpose is to isolate the kernel and server code from
 * Spring DM service proxy related types which vary by Spring DM and Spring
 * release and which are multiply loaded, for instance due to cloning.
 * 
 */
public interface ServiceProxyInspector {
	
	/**
	 * Given a service proxy, determine whether or not the service is currently available.
	 * <p />
	 * Given an object that is not a service proxy, return <code>true</code>.
	 * 
	 * @param proxy the service proxy to be checked
	 * @return <code>true</code> if and only if the service proxy is currently available
	 */
	boolean isLive(Object proxy);

}
