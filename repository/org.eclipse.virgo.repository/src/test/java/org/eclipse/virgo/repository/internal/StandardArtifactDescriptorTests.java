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

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_NAME_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_NAME_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_THIRTEEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.eclipse.virgo.repository.Attribute;
import org.junit.Test;
import org.osgi.framework.Version;


/**
 * <p>
 * Unit tests for {@link org.eclipse.virgo.repository.internal.StandardArtifactDescriptor}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe test case
 * 
 */
public class StandardArtifactDescriptorTests {

    @Test(expected = IllegalArgumentException.class)
    public void testStandardArtefactNullURI() {
        new StandardArtifactDescriptor(null, "foo", "bar", new Version("0"), null, Collections.<Attribute> emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardArtefactNullType() {
        new StandardArtifactDescriptor(TEST_URI_ONE, null, "bar", new Version("0"), null, Collections.<Attribute> emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardArtefactNullName() {
        new StandardArtifactDescriptor(TEST_URI_ONE, "foo", null, new Version("0"), null, Collections.<Attribute> emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardArtefactNullVersion() {
        new StandardArtifactDescriptor(TEST_URI_ONE, "foo", "bar", null, null, Collections.<Attribute> emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStandardArtefactNullAttributes() {
        new StandardArtifactDescriptor(TEST_URI_ONE, "foo", "bar", new Version("0"), null, null);
    }

    @Test
    public void testGetName() {
        assertEquals("Incorrect name reported", TEST_NAME_ONE, TEST_NAME_ONE);
        assertEquals("Incorrect name reported", TEST_NAME_FIFTEEN, TEST_NAME_FIFTEEN);
    }

    @Test
    public void testGetURI() {
        assertEquals("Supplied file not returned", TEST_URI_THIRTEEN, TEST_ARTEFACT_THIRTEEN.getUri());
        assertEquals("Supplied file not returned", TEST_URI_FIFTEEN, TEST_ARTEFACT_FIFTEEN.getUri());
    }

    @Test
    public void testToString() {
        assertTrue(
            "Wrong toString returned for constructor args. '" + TEST_URI_ONE.getPath() + "' was not in '" + TEST_ARTEFACT_ONE.toString() + "'",
            TEST_ARTEFACT_ONE.toString().contains(TEST_URI_ONE.getPath()));
        assertTrue("Wrong toString returned for constructor args", TEST_ARTEFACT_FIFTEEN.toString().contains(TEST_URI_FIFTEEN.getPath()));
    }

}
