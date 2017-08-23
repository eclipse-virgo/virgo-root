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

package org.eclipse.virgo.nano.deployer.api;

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.nano.deployer.api.ArtifactIdentity;
import org.junit.Test;


/**
 * ArtifactIdentityTests
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Test thread-safe
 *
 */
public class ArtifactIdentityTests {
    
    @Test 
    public void testArtifactIdentityConstruction() {
        ArtifactIdentity ai = new ArtifactIdentity("type","name","version");
        assertEquals("type", ai.getType());
        assertEquals("name", ai.getName());
        assertEquals("version", ai.getVersion());
    }
}
