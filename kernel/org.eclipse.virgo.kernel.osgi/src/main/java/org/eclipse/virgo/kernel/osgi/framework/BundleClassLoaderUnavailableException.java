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

import org.osgi.framework.Bundle;

/**
 * Exception signalling that the <code>ClassLoader</code> for a {@link Bundle} is not available.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public final class BundleClassLoaderUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 4014635817682252026L;

	public BundleClassLoaderUnavailableException() {
		super();
	}

	public BundleClassLoaderUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public BundleClassLoaderUnavailableException(String message) {
		super(message);
	}

	public BundleClassLoaderUnavailableException(Throwable cause) {
		super(cause);
	}

}
