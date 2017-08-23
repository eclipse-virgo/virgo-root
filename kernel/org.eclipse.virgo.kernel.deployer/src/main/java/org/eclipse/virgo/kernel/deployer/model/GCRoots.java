/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.model;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.nano.serviceability.NonNull;

/**
 * {@link GCRoots}, "garbage collection roots", provides access to the install artifacts that have been installed
 * directly rather than as children of other install artifacts. The notion is that an install artifact becomes eligible
 * for garbage collection when it is no longer accessible from the GC roots. The "GC root" terminology is important as a
 * GC root may be a child of another install artifact due to the way install artifacts may be shared.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations of this interface must be thread safe.
 */
public interface GCRoots extends Iterable<InstallArtifact> {

    /**
     * Checks whether or not the given {@link InstallArtifact} is a GC root.
     * 
     * @param installArtifact the {@link InstallArtifact} to be checked, which must not be <code>null</code>
     * @return <code>true</code> if and only if the given {@link InstallArtifact} is a GC root
     */
    boolean isGCRoot(@NonNull InstallArtifact installArtifact);

}
