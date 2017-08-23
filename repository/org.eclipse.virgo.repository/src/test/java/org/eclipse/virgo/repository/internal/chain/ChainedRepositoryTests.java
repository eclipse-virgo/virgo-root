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

package org.eclipse.virgo.repository.internal.chain;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.chain.ChainedRepository;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

public class ChainedRepositoryTests {

    private Repository repository1;

    private Repository repository2;

    private Repository repository3;

    private Repository chain;

    @Before
    public void setup() {
        this.repository1 = createMock(Repository.class);
        this.repository2 = createMock(Repository.class);
        this.repository3 = createMock(Repository.class);

        /*
         * expect(this.repository1.getName()).andReturn("one"); expect(this.repository2.getName()).andReturn("two");
         * expect(this.repository3.getName()).andReturn("three");
         */
    }

    @Test
    public void repositoryName() {
        replay(this.repository1, this.repository2, this.repository3);
        this.chain = new ChainedRepository("chainName", Arrays.asList(this.repository1, this.repository2, this.repository3));
        assertEquals("chainName", this.chain.getName());
        verify(this.repository1, this.repository2, this.repository3);
    }

    @Test
    public void createQuery() {

        String key = "key";
        String value = "value";

        Query query1 = createMock(Query.class);
        Query query2 = createMock(Query.class);
        Query query3 = createMock(Query.class);

        expect(this.repository1.createQuery(key, value)).andReturn(query1);
        expect(this.repository2.createQuery(key, value)).andReturn(query2);
        expect(this.repository3.createQuery(key, value)).andReturn(query3);

        replay(this.repository1, this.repository2, this.repository3);

        this.chain = new ChainedRepository("chainName", Arrays.asList(this.repository1, this.repository2, this.repository3));

        this.chain.createQuery(key, value);

        verify(this.repository1, this.repository2, this.repository3);
    }

    @Test
    public void createQueryWithProperties() {

        String key = "key";
        String value = "value";
        Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

        Query query1 = createMock(Query.class);
        Query query2 = createMock(Query.class);
        Query query3 = createMock(Query.class);

        expect(this.repository1.createQuery(key, value, properties)).andReturn(query1);
        expect(this.repository2.createQuery(key, value, properties)).andReturn(query2);
        expect(this.repository3.createQuery(key, value, properties)).andReturn(query3);

        replay(this.repository1, this.repository2, this.repository3);

        this.chain = new ChainedRepository("chainName", Arrays.asList(this.repository1, this.repository2, this.repository3));

        this.chain.createQuery(key, value, properties);

        verify(this.repository1, this.repository2, this.repository3);
    }

    @Test
    public void get() {
        VersionRange versionRange = new VersionRange("[1.0.0,2.0.0)");

        RepositoryAwareArtifactDescriptor artefact1 = createMock(RepositoryAwareArtifactDescriptor.class);
        RepositoryAwareArtifactDescriptor artefact2 = createMock(RepositoryAwareArtifactDescriptor.class);

        expect(this.repository1.get("name", "type", versionRange)).andReturn(artefact1);
        expect(this.repository2.get("name", "type", versionRange)).andReturn(null);
        expect(this.repository3.get("name", "type", versionRange)).andReturn(artefact2);

        expect(artefact1.getVersion()).andReturn(new Version("1.8.4")).anyTimes();
        expect(artefact2.getVersion()).andReturn(new Version("1.0.3")).anyTimes();

        replay(this.repository1, this.repository2, this.repository3, artefact1, artefact2);

        this.chain = new ChainedRepository("chainName", Arrays.asList(this.repository1, this.repository2, this.repository3));

        ArtifactDescriptor artefact = this.chain.get("name", "type", versionRange);
        assertNotNull(artefact);
        assertEquals(artefact, artefact1);

        verify(this.repository1, this.repository2, this.repository3, artefact1, artefact2);
    }
}
