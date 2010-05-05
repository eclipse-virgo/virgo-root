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

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_PARAMETERS_THREE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.StandardQuery;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 */
public class StandardQueryTests {

    private static StandardQuery QUERY;

    private static StandardQuery QUERY_PROP;

    private static StubArtefactDepository ARTEFACT_DEPOSITORY;

    @Before
    public void setUp() {
        ARTEFACT_DEPOSITORY = new StubArtefactDepository();
        QUERY = new StandardQuery(ARTEFACT_DEPOSITORY, "foo", "bar");
        QUERY_PROP = new StandardQuery(ARTEFACT_DEPOSITORY, "foo", "bar", TEST_ATTRIBUTE_PARAMETERS_THREE);
    }

    @Test
    public void testNewQuery() {
        QUERY.run();
        Set<Attribute> filters = ARTEFACT_DEPOSITORY.getFilters();
        assertEquals("Wrong number of filters found", 1, filters.size());
    }

    @Test
    public void testNewQueryWithProps() {
        QUERY_PROP.run();
        Set<Attribute> filters = ARTEFACT_DEPOSITORY.getFilters();
        assertEquals("Wrong number of filters found", 1, filters.size());
        assertEquals("Wring properties found", TEST_ATTRIBUTE_PARAMETERS_THREE, filters.iterator().next().getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFilterNullName() {
        QUERY.addFilter(null, "bananas");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFilterNullValue() {
        QUERY.addFilter("monkey", null);
    }

    @Test
    public void testAddFilter() {
        QUERY.addFilter("monkey", "bananas");
        QUERY.run();
        Set<Attribute> filters = ARTEFACT_DEPOSITORY.getFilters();
        assertEquals("Wrong number of filters found", 2, filters.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFilterWithParametersNullName() {
        QUERY.addFilter(null, "bananas", new HashMap<String, Set<String>>(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFilterWithParametersNullValue() {
        QUERY.addFilter("monkey", null, new HashMap<String, Set<String>>(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFilterWithParametersNullParameters() {
        QUERY.addFilter("monkey", "bananas", null);
    }

    @Test
    public void testAddFilterWithParameters() {
        QUERY.addFilter("monkey", "bananas", new HashMap<String, Set<String>>(0));
        QUERY.run();
        Set<Attribute> filters = ARTEFACT_DEPOSITORY.getFilters();
        assertEquals("Wrong number of filters found", 2, filters.size());
    }

    @Test
    public void testQueryNoArtefacts() {
        ARTEFACT_DEPOSITORY.setNextReturnCount(StubArtefactDepository.NONE);
        Set<RepositoryAwareArtifactDescriptor> artefacts = QUERY.run();
        assertEquals(0, artefacts.size());
    }

    @Test
    public void testQueryOneArtefact() {
        ARTEFACT_DEPOSITORY.setNextReturnCount(StubArtefactDepository.ONE);
        Set<RepositoryAwareArtifactDescriptor> artefacts = QUERY.run();
        assertEquals(1, artefacts.size());
    }

    public void testQueryMultipleArtefacts() {
        ARTEFACT_DEPOSITORY.setNextReturnCount(StubArtefactDepository.MANY);
        assertTrue(QUERY.run().size() > 1);
    }

    @Test
    public void testToString() {
        String toString = QUERY_PROP.toString();
        assertTrue("toString dosn't contain all the required information", toString.contains("foo=bar"));
        assertTrue("toString dosn't contain all the required information", toString.contains("2"));
    }

}
