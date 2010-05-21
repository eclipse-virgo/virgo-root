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

package org.eclipse.virgo.apps.admin.core;

import java.io.File;

/**
 * <code>ApplicationManagerService</code> defines a simple service API for managing applications within the server.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be thread-safe.
 * 
 */
public interface ArtifactService {

    /**
     * @param stagedFile 
     * @return string describing result
     */
    String deploy(File stagedFile);

    /**
     * Request a reference to the staging directory where artefacts to be deployed should be placed.
     * 
     * @return the staging directory.
     */
    File getStagingDirectory();


}
