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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * <strong>Not</strong> thread-safe
 * 
 */
final class Index {

    private static final Set<Attribute> EMPTY_ATTRIBUTE_SET = Collections.emptySet();

    private final Map<String, Map<String, Set<Attribute>>> attributeIndex = new HashMap<String, Map<String, Set<Attribute>>>();

    private final Map<Attribute, RepositoryAwareArtifactDescriptor> artifactDescriptorIndex = new HashMap<Attribute, RepositoryAwareArtifactDescriptor>(); // Attribute

    Index(Collection<RepositoryAwareArtifactDescriptor> artifactDescriptors) {
        for (RepositoryAwareArtifactDescriptor artifactDescriptor : artifactDescriptors) {
            this.addArtifactDescriptor(artifactDescriptor);
        }
    }

    void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artefact) {
        for (Attribute attribute : artefact.getAttributes()) {
            this.artifactDescriptorIndex.put(attribute, artefact);
            Map<String, Set<Attribute>> valueIndex = this.attributeIndex.get(attribute.getKey());
            if (valueIndex == null) {
                valueIndex = new HashMap<String, Set<Attribute>>();
                this.attributeIndex.put(attribute.getKey(), valueIndex);
            }
            Set<Attribute> attributes = valueIndex.get(attribute.getValue());
            if (attributes == null) {
                attributes = new HashSet<Attribute>();
                valueIndex.put(attribute.getValue(), attributes);
            }
            attributes.add(attribute);

        }
    }

    void removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        for (Attribute attribute : artifactDescriptor.getAttributes()) {
            if (this.artifactDescriptorIndex.containsKey(attribute)) {
                this.artifactDescriptorIndex.remove(attribute);
                Map<String, Set<Attribute>> valueIndex = this.attributeIndex.get(attribute.getKey());
                if (valueIndex != null) {
                    Set<Attribute> attributes = valueIndex.get(attribute.getValue());
                    if (attributes != null) {
                        attributes.remove(attribute);
                        if (attributes.size() == 0) {
                            valueIndex.remove(attribute.getValue());
                        }
                    }
                    if (valueIndex.size() == 0) {
                        this.artifactDescriptorIndex.remove(attribute);
                    }
                }
            }
        }
    }

    Set<Attribute> findMatchingAttributes(String key, String value) {
        Set<Attribute> result = null;
        Map<String, Set<Attribute>> map = this.attributeIndex.get(key);
        if (map != null) {
            result = map.get(value);
        }
        return (result != null ? result : EMPTY_ATTRIBUTE_SET);
    }

    RepositoryAwareArtifactDescriptor getArtifactDescriptor(Attribute attribute) {
        return this.artifactDescriptorIndex.get(attribute);
    }

}
