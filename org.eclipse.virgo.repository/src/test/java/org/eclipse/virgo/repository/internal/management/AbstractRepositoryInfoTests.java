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

package org.eclipse.virgo.repository.internal.management;

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TYPE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_NAME_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_VERSION_ONE;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.StubArtefactDepository;
import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.junit.Test;


public abstract class AbstractRepositoryInfoTests {

    protected StubArtefactDepository depository = new StubArtefactDepository();

    protected abstract RepositoryInfo getRepositoryInfo(ArtifactDescriptorDepository depository);

    @Test
    public void testSummariesMany() {
        depository.setNextReturnCount(StubArtefactDepository.MANY);

        RepositoryInfo info = getRepositoryInfo(depository);
        Set<ArtifactDescriptorSummary> summaries = info.getAllArtifactDescriptorSummaries();
        assertEquals(2, summaries.size());
    }

    @Test
    public void testSummariesOne() {
        depository.setNextReturnCount(StubArtefactDepository.ONE);

        RepositoryInfo info = getRepositoryInfo(depository);
        Set<ArtifactDescriptorSummary> summaries = info.getAllArtifactDescriptorSummaries();
        assertEquals(1, summaries.size());

        ArtifactDescriptorSummary summary = summaries.iterator().next();
        assertEquals(TEST_ARTEFACT_TYPE, summary.getType());
        assertEquals(TEST_NAME_ONE, summary.getName());
        assertEquals(TEST_VERSION_ONE.toString(), summary.getVersion());
    }

    @Test
    public void testSummariesNone() {
        depository.setNextReturnCount(StubArtefactDepository.NONE);

        RepositoryInfo info = getRepositoryInfo(depository);
        Set<ArtifactDescriptorSummary> summaries = info.getAllArtifactDescriptorSummaries();
        assertEquals(0, summaries.size());
    }
}
