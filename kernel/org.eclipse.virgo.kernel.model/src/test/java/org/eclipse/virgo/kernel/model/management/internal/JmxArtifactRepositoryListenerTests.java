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
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.model.management.internal.JmxArtifactRepositoryListener;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Test;


public class JmxArtifactRepositoryListenerTests {

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final RuntimeArtifactModelObjectNameCreator creator = createMock(RuntimeArtifactModelObjectNameCreator.class);

    private final JmxArtifactRepositoryListener listener = new JmxArtifactRepositoryListener(creator);

    @Test(expected = FatalAssertionException.class)
    public void nullCreator() {
        new JmxArtifactRepositoryListener(null);
    }

    @Test
    public void added() throws MalformedObjectNameException, NullPointerException {
        expect(this.creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("test:key=1"));
        replay(creator);

        int initial = this.server.getMBeanCount();
        this.listener.added(new StubCompositeArtifact());
        assertEquals(initial + 1, (int) this.server.getMBeanCount());
        verify(creator);
    }

    @Test
    public void removed() throws MalformedObjectNameException, NullPointerException {
        expect(this.creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("test:key=1")).times(2);
        replay(creator);

        this.listener.added(new StubCompositeArtifact());
        int initial = this.server.getMBeanCount();
        this.listener.removed(new StubCompositeArtifact());
        assertEquals(initial - 1, (int) this.server.getMBeanCount());
        verify(creator);
    }

}
