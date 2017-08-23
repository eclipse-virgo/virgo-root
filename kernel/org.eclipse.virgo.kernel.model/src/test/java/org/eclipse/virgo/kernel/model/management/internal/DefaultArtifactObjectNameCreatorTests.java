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

package org.eclipse.virgo.kernel.model.management.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.internal.DefaultRuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Test;


public class DefaultArtifactObjectNameCreatorTests {

    private final DefaultRuntimeArtifactModelObjectNameCreator creator = new DefaultRuntimeArtifactModelObjectNameCreator("test-domain");
    
    @Test(expected = FatalAssertionException.class)
    public void nullArtifactModelArtifact() {
        creator.createArtifactModel(null);
    }

    @Test
    public void successArtifactModel() {
        ObjectName objectName = creator.createArtifactModel(new StubCompositeArtifact());
        assertNotNull(objectName);
        assertEquals("test-domain:artifact-type=test-type,name=test-name,region=test-region,type=ArtifactModel,version=0.0.0", objectName.getCanonicalName());
    }

    @Test
 	public void createArtifactsOfTypeQuery() throws Exception {
 		ObjectName artifactsOfTypeQuery = creator.createArtifactsOfTypeQuery("test-type");
 		assertNotNull(artifactsOfTypeQuery);
 		assertEquals("test-domain:artifact-type=test-type,type=ArtifactModel,*", artifactsOfTypeQuery.getCanonicalName());
 	}

    @Test
 	public void createArtifactsOfTypeQueryQuoting() throws Exception {
 		ObjectName artifactsOfTypeQuery = creator.createArtifactsOfTypeQuery("test:type");
 		assertNotNull(artifactsOfTypeQuery);
 		assertEquals("test-domain:artifact-type=\"test:type\",type=ArtifactModel,*", artifactsOfTypeQuery.getCanonicalName());
 	}

     @Test
 	public void artifactVersionQuery() throws Exception {
 		ObjectName artifactsVersionQuery = creator.createArtifactVersionsQuery("test-type", "test-name");
 		assertNotNull(artifactsVersionQuery);
 		assertEquals("test-domain:artifact-type=test-type,name=test-name,type=ArtifactModel,*", artifactsVersionQuery.getCanonicalName());
 	}

     @Test
 	public void allArtifactsQuery() throws Exception {
 		ObjectName artifactsQuery = creator.createAllArtifactsQuery();
 		assertNotNull(artifactsQuery);
 		assertEquals("test-domain:type=ArtifactModel,*", artifactsQuery.getCanonicalName());
 	}

}
