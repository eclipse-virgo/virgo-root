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

package org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.StandardArtifactDescriptorHash;
import org.eclipse.virgo.repository.util.FileDigest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardArtifactDescriptorHashTests {

    private static final String HASH_1 = "hash 1";
    
    private static final String HASH_2 = "hash 2";

    private static final String HASH_ATTRIBUTE_NAME = "hash";
    
    private static final String NON_HASH_ATTRIBUTE_NAME = "name";
    
    private static final String NON_HASH_ATTRIBUTE_VALUE = "value";
    
    private static final String HASH_ALGORITHM_ATTRIBUTE_NAME = "hash-algorithm";

    private RepositoryAwareArtifactDescriptor mockRepositoryAwareArtifactDescriptor;

    private StandardArtifactDescriptorHash artifactDescriptorHash;

    private Set<Attribute> hashAttributeSet;
    
    private Set<Attribute> algorithmAttributeSet;

    @Before
    public void setUp() throws Exception {
        setUpArtifactDescriptor();
    }

    private void setUpArtifactDescriptor() {
        this.hashAttributeSet = new HashSet<Attribute>();
        this.algorithmAttributeSet = new HashSet<Attribute>();
        this.mockRepositoryAwareArtifactDescriptor = createMock(RepositoryAwareArtifactDescriptor.class);
        expect(this.mockRepositoryAwareArtifactDescriptor.getAttribute(eq(HASH_ATTRIBUTE_NAME))).andReturn(this.hashAttributeSet).anyTimes();
        expect(this.mockRepositoryAwareArtifactDescriptor.getAttribute(eq(HASH_ALGORITHM_ATTRIBUTE_NAME))).andReturn(this.algorithmAttributeSet).anyTimes();
    }

    private void setUpArtifactDescriptorHash() {
        this.artifactDescriptorHash = new StandardArtifactDescriptorHash(this.mockRepositoryAwareArtifactDescriptor);
    }

    @After
    public void tearDown() throws Exception {
        verifyMocks();
        resetMocks();
    }

    @Test
    public void testIsPresentEmptyAttributeSet() {
        replayMocks();
        setUpArtifactDescriptorHash();

        assertFalse(this.artifactDescriptorHash.isPresent());
    }

    @Test
    public void testIsPresentNonHashAttribute() {
        Attribute nonHashAttribute = new StandardAttribute(NON_HASH_ATTRIBUTE_NAME, NON_HASH_ATTRIBUTE_VALUE);
        this.hashAttributeSet.add(nonHashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertFalse(this.artifactDescriptorHash.isPresent());
    }
    
    @Test
    public void testIsPresentUnreasonableHashAttribute() {
        Attribute hashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, "");
        this.hashAttributeSet.add(hashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertFalse(this.artifactDescriptorHash.isPresent());
    }
    
    @Test
    public void testIsPresentReasonableHashAttribute() {
        Attribute hashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, HASH_1);
        this.hashAttributeSet.add(hashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertTrue(this.artifactDescriptorHash.isPresent());
    }

    @Test
    public void testIsPresentMultipleHashAttributes() {
        Attribute hashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, HASH_1);
        this.hashAttributeSet.add(hashAttribute);
        Attribute extraHashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, HASH_2);
        this.hashAttributeSet.add(extraHashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertTrue(this.artifactDescriptorHash.isPresent());
    }
    
    @Test
    public void testGetDigestAlgorithm() {
        Attribute algorithmAttribute = new StandardAttribute(HASH_ALGORITHM_ATTRIBUTE_NAME, FileDigest.MD5_DIGEST_ALGORITHM);
        this.algorithmAttributeSet.add(algorithmAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertEquals(FileDigest.MD5_DIGEST_ALGORITHM, this.artifactDescriptorHash.getDigestAlgorithm());
    }

    @Test
    public void testMatchesNotPresent() {
        replayMocks();
        setUpArtifactDescriptorHash();

        assertFalse(this.artifactDescriptorHash.matches(HASH_1));
    }
    
    @Test
    public void testMatchesPresentMatches() {
        Attribute hashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, HASH_1);
        this.hashAttributeSet.add(hashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertTrue(this.artifactDescriptorHash.matches(HASH_1));
    }
    
    @Test
    public void testMatchesPresentNoMatch() {
        Attribute hashAttribute = new StandardAttribute(HASH_ATTRIBUTE_NAME, HASH_1);
        this.hashAttributeSet.add(hashAttribute);
        replayMocks();
        setUpArtifactDescriptorHash();

        assertFalse(this.artifactDescriptorHash.matches(HASH_2));
    }



    private void replayMocks() {
        replay(this.mockRepositoryAwareArtifactDescriptor);
    }

    private void verifyMocks() {
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    private void resetMocks() {
        reset(this.mockRepositoryAwareArtifactDescriptor);
    }

}
