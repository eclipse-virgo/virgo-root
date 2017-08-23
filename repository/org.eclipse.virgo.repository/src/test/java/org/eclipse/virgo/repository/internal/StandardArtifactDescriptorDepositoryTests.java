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

package org.eclipse.virgo.repository.internal;

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_EIGHT;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ELEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOUR;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOURTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_NINE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIX;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIXTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWELVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ZERO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ZERO_URI;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_NAME_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_PARAMETERS_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_VALUE_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_ATTRIBUTE_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_ATTRIBUTE_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_ATTRIBUTE_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_NAME_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_NAME_TWELVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_NOTHING;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_TYPE_A;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_TYPE_B;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_TYPE_C;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_URI_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_VERSION_100;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_QUERY_FILTER_VERSION_254A;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_EIGHT;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_ELEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FOURTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_NINE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_TEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_TWELVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.eclipse.virgo.repository.internal.persistence.StubArtifactDescriptorPersister;
import org.junit.Before;
import org.junit.Test;


/**
 * <p>
 * Unit tests for {@link org.eclipse.virgo.repository.internal.StandardArtifactDescriptorDepository}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe test case
 * 
 */
public class StandardArtifactDescriptorDepositoryTests {

    private ArtifactDescriptorDepository artefactDepository;

    private StubArtifactDescriptorPersister stubArtefactPersister;

    private Set<Attribute> filters;

    @Before
    public void setUp() throws Exception {
        this.filters = new HashSet<Attribute>();
        this.stubArtefactPersister = new StubArtifactDescriptorPersister();
        this.artefactDepository = new StandardArtifactDescriptorDepository(this.stubArtefactPersister);
    }

    @Test
    public void testInstantiateArtefactDepositoryExist() throws IOException {
        this.stubArtefactPersister.addArtefact(TEST_ARTEFACT_EIGHT);
        this.stubArtefactPersister.addArtefact(TEST_ARTEFACT_SEVEN);
        this.artefactDepository = new StandardArtifactDescriptorDepository(this.stubArtefactPersister);
        assertEquals(2, this.artefactDepository.getArtifactDescriptorCount());
    }

    @Test
    public void testInstantiateArtefactDepositoryNotExist() {
        assertEquals(0, this.artefactDepository.getArtifactDescriptorCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddArtefactNull() throws DuplicateArtifactException {
        this.artefactDepository.addArtifactDescriptor(null);
    }

    @Test
    public void testAddArtefactNotExist() throws DuplicateArtifactException {
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
        assertEquals(1, this.artefactDepository.getArtifactDescriptorCount());
    }

    @Test(expected = DuplicateArtifactException.class)
    public void testAddArtefactExist() throws DuplicateArtifactException {
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
    }

    @Test(expected = DuplicateArtifactException.class)
    public void testAddArtefactExistUrl() throws DuplicateArtifactException {
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ZERO);
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ZERO_URI);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveArtefactNull() {
        this.artefactDepository.removeArtifactDescriptor((URI) null);
    }

    @Test
    public void testRemoveArtefactExist() throws DuplicateArtifactException {
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
        assertNotNull(this.artefactDepository.removeArtifactDescriptor(TEST_URI_ONE));
        assertEquals(0, this.artefactDepository.getArtifactDescriptorCount());
    }

    @Test
    public void testRemoveArtefactNotExist() {
        assertNull(this.artefactDepository.removeArtifactDescriptor(TEST_URI_ONE));
    }

    @Test
    public void testPersistArtefactDepository() throws DuplicateArtifactException, IOException {
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_TWO);
        this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_THREE);
        this.artefactDepository.persist();
        assertTrue(this.stubArtefactPersister.getLastPersisted().contains(TEST_ARTEFACT_ONE));
        assertTrue(this.stubArtefactPersister.getLastPersisted().contains(TEST_ARTEFACT_TWO));
        assertTrue(this.stubArtefactPersister.getLastPersisted().contains(TEST_ARTEFACT_THREE));
    }

    @Test
    public void testResolveArtefactNull() {
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(new HashSet<Attribute>());
        assertEquals("Wrong number of Artefacts returned", 0, artefacts.size());
        this.populateArtefactDepository();
        artefacts = this.artefactDepository.resolveArtifactDescriptors(new HashSet<Attribute>());
        assertEquals(16, artefacts.size());
    }

    @Test
    public void testResolveArtefactsAll() {
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(new HashSet<Attribute>());
        assertEquals("Wrong number of Artefacts returned", 0, artefacts.size());
        this.populateArtefactDepository();
        artefacts = this.artefactDepository.resolveArtifactDescriptors(new HashSet<Attribute>());
        assertEquals(16, artefacts.size());
    }

    @Test
    public void testResolveArtefactsType() {
        this.populateArtefactDepository();
        this.filters.add(TEST_QUERY_FILTER_TYPE_A);
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 5, artefacts.size());
    }

    @Test
    public void testResolveArtefactsName() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_NAME_THIRTEEN);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
        assertTrue("Wrong Artefact returned", artefacts.contains(TEST_ARTEFACT_THIRTEEN));
    }

    @Test
    public void testResolveArtefactsVersion() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_VERSION_100);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
        assertTrue("Wrong number of Artefacts returned", artefacts.contains(TEST_ARTEFACT_SIXTEEN));
    }

    @Test
    public void testResolveArtefactsVersionMatchMany() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_VERSION_254A);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 9, artefacts.size());
    }

    @Test
    public void testResolveArtefactsURI() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_URI_THREE);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
        assertTrue("Wrong number of Artefacts returned", artefacts.contains(TEST_ARTEFACT_THREE));
    }

    // TEST_ATTRIBUTE_THREE_AGAIN

    @Test
    public void testResolveArtefactsAttributeNoProperties() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_ATTRIBUTE_ONE);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 3, artefacts.size());
    }

    @Test
    public void testResolveArtefactsAttributePropertiesManyResults() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_ATTRIBUTE_TWO);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 2, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_TYPE_C);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 2, artefacts.size());
    }

    @Test
    public void testResolveArtefactsAttributePropertiesOneResult() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_ATTRIBUTE_THREE);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 2, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_TYPE_C);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
    }

    @Test
    public void testResolveCombinationQuery() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_VERSION_254A);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 9, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_TYPE_B);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 6, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_NAME_TWELVE);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
        assertTrue("Wrong Artefact returned", artefacts.contains(TEST_ARTEFACT_TWELVE));
    }

    @Test
    public void testResolveRemovedArtefacts() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_VERSION_254A);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 9, artefacts.size());

        this.artefactDepository.removeArtifactDescriptor(TEST_URI_NINE);
        this.artefactDepository.removeArtifactDescriptor(TEST_URI_TEN);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 7, artefacts.size());

        this.artefactDepository.removeArtifactDescriptor(TEST_URI_THIRTEEN);
        this.artefactDepository.removeArtifactDescriptor(TEST_URI_FOURTEEN);
        this.artefactDepository.removeArtifactDescriptor(TEST_URI_FIFTEEN);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 4, artefacts.size());

        this.artefactDepository.removeArtifactDescriptor(TEST_URI_EIGHT);
        this.artefactDepository.removeArtifactDescriptor(TEST_URI_ELEVEN);
        this.artefactDepository.removeArtifactDescriptor(TEST_URI_TWELVE);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 1, artefacts.size());
        assertTrue("Wrong Artefact returned", artefacts.contains(TEST_ARTEFACT_SEVEN));
    }

    @Test
    public void testResolveArtefactsNoResults() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        StandardAttribute filter = new StandardAttribute(TEST_ATTRIBUTE_NAME_TWO, TEST_ATTRIBUTE_VALUE_TWO, TEST_ATTRIBUTE_PARAMETERS_THREE);

        this.filters.add(filter);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 0, artefacts.size());
    }

    @Test
    public void testResolveArtefactsNoResultsTwo() {
        this.populateArtefactDepository();
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 16, artefacts.size());

        this.filters.add(TEST_QUERY_FILTER_NOTHING);
        artefacts = this.artefactDepository.resolveArtifactDescriptors(this.filters);
        assertEquals("Wrong number of Artefacts returned", 0, artefacts.size());
    }

    private void populateArtefactDepository() {
        try {
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ONE);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_TWO);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_THREE);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_FOUR);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_FIVE);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_SIX);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_SEVEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_EIGHT);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_NINE);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_TEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_ELEVEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_TWELVE);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_THIRTEEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_FOURTEEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_FIFTEEN);
            this.artefactDepository.addArtifactDescriptor(TEST_ARTEFACT_SIXTEEN);
        } catch (DuplicateArtifactException e) {
            throw new RuntimeException("Test error while populating the depository");
        }
    }

}
