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

package org.eclipse.virgo.kernel.install.artifact;

import java.io.File;

/**
 * {@link ArtifactIdentityDeterminer} is kernel extension point for determining the identity of an install artifact.
 * It also provides some constants for well known Artifact types.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ArtifactIdentityDeterminer {

    public static final String BUNDLE_TYPE = "bundle";

    public static final String PLAN_TYPE = "plan";

    public static final String CONFIGURATION_TYPE = "configuration";

    public static final String PAR_TYPE = "par";

    /**
     * Determines the identity of the given artifact.
     * 
     * @param file The {@link File} that represents the artifact
     * @param scopeName The name of the artifact's scope
     * @return the identity of the artifact or <code>null</code> if the identity could not be determined
     */
    ArtifactIdentity determineIdentity(File file, String scopeName);

}
