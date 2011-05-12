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
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.junit.Test;


public class DefaultArtifactObjectNameCreatorTests {

    private final DefaultRuntimeArtifactModelObjectNameCreator creator = new DefaultRuntimeArtifactModelObjectNameCreator("test-domain");

    @Test(expected = FatalAssertionException.class)
    public void nullArtifact() {
        creator.create(null);
    }

    @Test
    public void success() {
        ObjectName objectName = creator.create(new StubCompositeArtifact());
        assertNotNull(objectName);
        assertEquals("test-domain:artifact-type=test-type,name=test-name,region=test-region,type=KernelModel,version=0.0.0", objectName.getCanonicalName());
    }
}
