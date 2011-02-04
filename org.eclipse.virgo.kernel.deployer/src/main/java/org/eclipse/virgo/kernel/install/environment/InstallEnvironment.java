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

package org.eclipse.virgo.kernel.install.environment;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.repository.Repository;

/**
 * An <code>InstallEnvironment</code> encapsulates the environment in which an install pipeline runs.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface InstallEnvironment {

    /**
     * Returns the {@link Repository} which the install pipeline should use to satisfy dependencies.
     * 
     * @return a <code>Repository</code>
     */
    Repository getRepository();

    /**
     * Returns the <code>InstallLog</code> for this <code>InstallEnvironment</code>.
     * 
     * @return the environment's install log.
     */
    InstallLog getInstallLog();

    /**
     * Returns the tree's OSGi state. This a side-state, i.e. manipulating this state will not affect the OSGi
     * framework's global state. If the state is not available, for instance because the tree is not being installed or
     * modified, then returns <code>null</code>.
     * 
     * @return an OSGi state or <code>null</code>
     */
    QuasiFramework getQuasiFramework();

    /**
     * Delete any resources associated with this {@link InstallEnvironment}.
     */
    void destroy();
}
