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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.chain.ChainedQuery;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Before;
import org.junit.Test;

public class ChainedQueryTests {

    private Query query1;

    private Query query2;

    private Query query3;

    private ChainedQuery chainedQuery;

    @Before
    public void setup() {
        query1 = createMock(Query.class);
        query2 = createMock(Query.class);
        query3 = createMock(Query.class);

        chainedQuery = new ChainedQuery(Arrays.asList(query1, query2, query3));
    }

    @Test
    public void filterAddition() {

        String name = "name";
        String value = "value";

        expect(query1.addFilter(name, value)).andReturn(query1);
        expect(query2.addFilter(name, value)).andReturn(query2);
        expect(query3.addFilter(name, value)).andReturn(query3);

        replay(query1, query2, query3);

        chainedQuery.addFilter(name, value);

        verify(query1, query2, query3);
    }

    @Test
    public void filterWithPropertiesAddition() {

        String name = "name";
        String value = "value";
        Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

        expect(query1.addFilter(name, value, properties)).andReturn(query1);
        expect(query2.addFilter(name, value, properties)).andReturn(query2);
        expect(query3.addFilter(name, value, properties)).andReturn(query3);

        replay(query1, query2, query3);

        chainedQuery.addFilter(name, value, properties);

        verify(query1, query2, query3);
    }

    @Test
    public void runWithNoResult() {
        expect(query1.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>());
        expect(query2.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>());
        expect(query3.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>());

        replay(query1, query2, query3);

        assertEquals(0, chainedQuery.run().size());

        verify(query1, query2, query3);
    }

    @Test
    public void runWithResult() {
        RepositoryAwareArtifactDescriptor artefact1 = createMock(RepositoryAwareArtifactDescriptor.class);
        RepositoryAwareArtifactDescriptor artefact2 = createMock(RepositoryAwareArtifactDescriptor.class);
        RepositoryAwareArtifactDescriptor artefact3 = createMock(RepositoryAwareArtifactDescriptor.class);

        expect(query1.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>(Arrays.asList(artefact1)));
        expect(query2.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>(Arrays.asList(artefact2)));
        expect(query3.run()).andReturn(new HashSet<RepositoryAwareArtifactDescriptor>(Arrays.asList(artefact3)));

        replay(query1, query2, query3);

        Set<RepositoryAwareArtifactDescriptor> result = chainedQuery.run();
        verify(query1, query2, query3);

        assertEquals(3, result.size());
        Iterator<RepositoryAwareArtifactDescriptor> artefacts = result.iterator();
        assertEquals(artefacts.next(), artefact1);
        assertEquals(artefacts.next(), artefact2);
        assertEquals(artefacts.next(), artefact3);
    }

    @Test
    public void setVersionRangeFilter() {
        final VersionRange versionRange = VersionRange.naturalNumberRange();

        expect(query1.setVersionRangeFilter(versionRange)).andReturn(query1);
        expect(query2.setVersionRangeFilter(versionRange)).andReturn(query2);
        expect(query3.setVersionRangeFilter(versionRange)).andReturn(query3);

        replay(query1, query2, query3);

        chainedQuery.setVersionRangeFilter(versionRange);

        verify(query1, query2, query3);
    }

    @Test
    public void setVersionRangeFilterWithStrategy() {
        final VersionRange versionRange = VersionRange.naturalNumberRange();

        expect(query1.setVersionRangeFilter(versionRange, Query.VersionRangeMatchingStrategy.HIGHEST)).andReturn(query1);
        expect(query2.setVersionRangeFilter(versionRange, Query.VersionRangeMatchingStrategy.HIGHEST)).andReturn(query2);
        expect(query3.setVersionRangeFilter(versionRange, Query.VersionRangeMatchingStrategy.HIGHEST)).andReturn(query3);

        replay(query1, query2, query3);

        chainedQuery.setVersionRangeFilter(versionRange, Query.VersionRangeMatchingStrategy.HIGHEST);

        verify(query1, query2, query3);
    }
}
