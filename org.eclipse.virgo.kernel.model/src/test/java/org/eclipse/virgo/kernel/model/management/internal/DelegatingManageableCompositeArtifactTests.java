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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import javax.management.MalformedObjectNameException;

import org.eclipse.virgo.kernel.model.CompositeArtifact;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.model.management.internal.DelegatingManageableCompositeArtifact;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Test;


public class DelegatingManageableCompositeArtifactTests {

    @Test(expected = FatalAssertionException.class)
    public void nullCreator() {
        new DelegatingManageableCompositeArtifact(null, new StubCompositeArtifact());
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifact() {
        new DelegatingManageableCompositeArtifact(createMock(RuntimeArtifactModelObjectNameCreator.class), null);
    }

    @Test
    public void success() throws MalformedObjectNameException, NullPointerException {
        RuntimeArtifactModelObjectNameCreator creator = createMock(RuntimeArtifactModelObjectNameCreator.class);
        CompositeArtifact artifact = createMock(CompositeArtifact.class);

        DelegatingManageableCompositeArtifact manageablePlanArtifact = new DelegatingManageableCompositeArtifact(creator, artifact);

        expect(artifact.isAtomic()).andReturn(true);
        expect(artifact.isScoped()).andReturn(true);
        replay(creator, artifact);

        assertTrue(manageablePlanArtifact.isAtomic());
        assertTrue(manageablePlanArtifact.isScoped());

        verify(creator, artifact);
    }

}
