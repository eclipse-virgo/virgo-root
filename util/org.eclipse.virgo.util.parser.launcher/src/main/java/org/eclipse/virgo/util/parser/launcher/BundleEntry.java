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

package org.eclipse.virgo.util.parser.launcher;

import java.net.URI;

/**
 * Represents a declaration by the user to install a bundle into OSGi and to
 * optionally start that bundle.
 * <p/>
 * Bundle declarations are typically represented as a string of the form:
 * <code>&lt;path&gt;[&lt;@start&gt;]</code>. In this string, path is either a
 * {@link URI} or a file path (relative or absolute). The <code>@start</code> flag
 * indicates that the bundle should be started by the launcher. Bundles are not started
 * automatically unless specified.
 */
public final class BundleEntry {

	private final URI uri;

	private final boolean autoStart;

	public BundleEntry(URI uri, boolean autoStart) {
		this.uri = uri;
		this.autoStart = autoStart;
	}

	public URI getURI() {
		return this.uri;
	}

	public boolean isAutoStart() {
		return this.autoStart;
	}

}
