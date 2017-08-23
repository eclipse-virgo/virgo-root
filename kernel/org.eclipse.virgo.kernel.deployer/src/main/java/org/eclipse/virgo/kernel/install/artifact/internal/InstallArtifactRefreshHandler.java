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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;


/**
 * A <code>InstallArtifactRefreshHandler</code> is used to handle the refresh of an
 * {@link InstallArtifact}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations <strong>must</strong> be thread-safe.
 *
 */
public interface InstallArtifactRefreshHandler {
    
    /**
     * Handles refresh of the supplied {@link InstallArtifact}.
     * 
     * @param installArtifact The <code>InstallArtifact</code> to refresh
     * @return <code>true</code> if the refresh was successful, otherwise <code>false</code>.
     */
    boolean refresh(InstallArtifact installArtifact);
}
