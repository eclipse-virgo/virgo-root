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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.io.File;

import org.eclipse.virgo.apps.admin.core.ArtifactService;

/**
 * 
 * Test Stub for testing
 * 
 */
public class StubArtifactService implements ArtifactService{

    public String deploy(File stagedFile) {
        return "Deploy";
    }

    public File getStagingDirectory() {
        return new File("foo");
    }
	
}
