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

package org.eclipse.virgo.web.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.web.core.internal.WebArtifactIdentityDeterminer;

/**
 */
public class WebArtifactIdentityDeterminerTests {

    private ArtifactIdentityDeterminer artefactIdentityDeterminer;

    @Before
    public void setUp() {
        this.artefactIdentityDeterminer = new WebArtifactIdentityDeterminer(new StubHashGenerator());
    }

    @Test
    public void testWarFileType() {
        assertEquals(ArtifactIdentityDeterminer.BUNDLE_TYPE, this.artefactIdentityDeterminer.determineIdentity(new File("test.war"), null).getType());
    }

    @Test
    public void testMixedCaseWarFileType() {
        assertEquals(ArtifactIdentityDeterminer.BUNDLE_TYPE, this.artefactIdentityDeterminer.determineIdentity(new File("test.wAR"), null).getType());
    }

    @Test
    public void testUpperCaseWarFileType() {
        assertEquals(ArtifactIdentityDeterminer.BUNDLE_TYPE, this.artefactIdentityDeterminer.determineIdentity(new File("test.WAR"), null).getType());
    }

    @Test
    public void testNonWarFileType() {
        assertNull(this.artefactIdentityDeterminer.determineIdentity(new File("test.jar"), null));
    }

    @Test
    public void testNoFileType() {
        assertNull(this.artefactIdentityDeterminer.determineIdentity(new File("test"), null));
    }    
}
