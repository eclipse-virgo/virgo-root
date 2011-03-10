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
import static org.eclipse.virgo.repository.internal.RepositoryTestData.createDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

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

    private final Set<Attribute> EMTPY_ATTRIBUTE_SET = Collections.<Attribute> emptySet();

    @Test
    public void testVersionRangeFilters() {
        Set<RepositoryAwareArtifactDescriptor> unfiltered = new HashSet<RepositoryAwareArtifactDescriptor>();
        final RepositoryAwareArtifactDescriptor c1low = createDescriptor("configuration", "c1", Version.parseVersion("1.0.0"), EMTPY_ATTRIBUTE_SET);
        unfiltered.add(c1low);
        final RepositoryAwareArtifactDescriptor c1high = createDescriptor("configuration", "c1", Version.parseVersion("2.0.0"), EMTPY_ATTRIBUTE_SET);
        unfiltered.add(c1high);
        final RepositoryAwareArtifactDescriptor c2 = createDescriptor("configuration", "c2", Version.parseVersion("2.0.0"), EMTPY_ATTRIBUTE_SET);
        unfiltered.add(c2);

        Set<RepositoryAwareArtifactDescriptor> filtered = Query.VersionRangeMatchingStrategy.ALL.match(unfiltered, VersionRange.NATURAL_NUMBER_RANGE);
        assertNotNull(filtered);
        assertEquals(filtered, unfiltered);

        filtered = Query.VersionRangeMatchingStrategy.HIGHEST.match(unfiltered, new VersionRange("[1, 3)"));
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(c1high));
        assertTrue(filtered.contains(c2));

        filtered = Query.VersionRangeMatchingStrategy.LOWEST.match(unfiltered, new VersionRange("[1, 3)"));
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(c1low));
        assertTrue(filtered.contains(c2));
    }
}
