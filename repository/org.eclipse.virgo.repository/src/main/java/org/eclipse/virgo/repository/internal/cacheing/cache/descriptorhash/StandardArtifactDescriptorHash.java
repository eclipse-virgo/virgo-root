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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.util.FileDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link StandardArtifactDescriptorHash} encapsulates the repository cache hash validation logic.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
class StandardArtifactDescriptorHash implements ArtifactDescriptorHash {

    private static final String DEFAULT_DIGEST_ALGORITHM = FileDigest.SHA_DIGEST_ALGORITHM;

    private static final String HASH_ATTRIBUTE_NAME = "hash";

    private static final String HASH_ALGORITHM_ATTRIBUTE_NAME = "hash-algorithm";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardArtifactDescriptorHash.class);

    private final boolean hasHash;

    private final String hash;

    private final String digestAlgorithm;

    StandardArtifactDescriptorHash(ArtifactDescriptor artifactDescriptor) {
        this.hash = getAttribute(artifactDescriptor, HASH_ATTRIBUTE_NAME, null);
        this.hasHash = isReasonableHash(this.hash);

        this.digestAlgorithm = getAttribute(artifactDescriptor, HASH_ALGORITHM_ATTRIBUTE_NAME, DEFAULT_DIGEST_ALGORITHM);
    }

    private String getAttribute(ArtifactDescriptor artifactDescriptor, String attributeName, String defaultAttributeValue) {
        String attributeValue = defaultAttributeValue;
        Set<Attribute> attributeSet = getAttributeSet(artifactDescriptor, attributeName);
        if (!attributeSet.isEmpty()) {
            if (attributeSet.size() > 1) {
                LOGGER.warn("Attribute with name " + attributeName + " has multiple values - using first and ignoring others");
            }
            Attribute algorithmAttribute = attributeSet.iterator().next();

            String key = algorithmAttribute.getKey();
            if (attributeName.equals(key)) {
                attributeValue = algorithmAttribute.getValue();
            } else {
                LOGGER.warn("Attribute found using name " + attributeName + " has unexpected key '" + key + "'");
            }
        }
        return attributeValue;
    }

    private Set<Attribute> getAttributeSet(ArtifactDescriptor artifactDescriptor, String attributeName) {
        Set<Attribute> hashAttributeSet = artifactDescriptor.getAttribute(attributeName);
        if (hashAttributeSet == null) {
            hashAttributeSet = new HashSet<Attribute>();
        }
        return hashAttributeSet;
    }

    private boolean isReasonableHash(String hashValue) {
        boolean reasonable = hashValue != null && !hashValue.isEmpty();
        if (!reasonable) {
            LOGGER.warn("Hash value {} is unreasonable", hashValue);
        }
        return reasonable;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPresent() {
        return this.hasHash;
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(String hashToMatch) {
        return isPresent() ? this.hash.equals(hashToMatch) : false;
    }

    /**
     * {@inheritDoc}
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

}
