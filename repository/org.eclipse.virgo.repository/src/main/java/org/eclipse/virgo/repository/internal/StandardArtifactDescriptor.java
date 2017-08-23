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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.osgi.framework.Version;


/**
 * <p>
 * The standard implementation of <code>ArtifactDescriptor</code>.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is threadsafe
 * 
 */
public final class StandardArtifactDescriptor implements ArtifactDescriptor {

    private static final Set<Attribute> EMPTY_ATTRIBUTE_SET = Collections.emptySet();

    private final Map<String, Set<Attribute>> attributesIndex = new HashMap<String, Set<Attribute>>();

    private final Set<Attribute> attributes;

    private final URI uri;

    private final String type;

    private final String name;

    private final Version version;

    private final int hash;

    private final String filename;

    /**
     * Simple constructor to store the values passed in for later reference.
     * 
     * @param uri whence the artifact
     * @param type artifact type
     * @param name artifact name
     * @param version artifact version
     * @param filename suggested name for the file of the artifact
     * @param attributes of the artifact
     */
    public StandardArtifactDescriptor(URI uri, String type, String name, Version version, String filename, Set<Attribute> attributes) {
        if (uri == null || type == null || name == null || version == null || attributes == null) {
            throw new IllegalArgumentException("Arguments can not be null");
        }

        Set<Attribute> tempAttributes;

        for (Attribute attribute : attributes) {
            tempAttributes = this.attributesIndex.get(attribute.getKey());
            if (tempAttributes == null) {
                tempAttributes = new HashSet<Attribute>();
                this.attributesIndex.put(attribute.getKey(), tempAttributes);
            }
            tempAttributes.add(attribute);
        }

        this.attributes = attributes;

        this.filename = addAttributeIfNotAlreadyPresent(FILENAME_ATTRIBUTE, filename);

        addAttributeIfNotAlreadyPresent(URI, uri.toString());
        this.uri = uri;

        addAttributeIfNotAlreadyPresent(TYPE, type);
        this.type = type;

        addAttributeIfNotAlreadyPresent(NAME, name);
        this.name = name;

        addAttributeIfNotAlreadyPresent(VERSION, version.toString());
        this.version = version;

        this.hash = this.type.hashCode() + this.name.hashCode() + this.version.hashCode();
    }

    private String addAttributeIfNotAlreadyPresent(String name, String value) {

        if (this.attributesIndex.containsKey(name)) {
            return this.attributesIndex.get(name).iterator().next().getValue();
        } else {
            if (value != null) {
                addAttribute(name, value);
            }
            return value;
        }
    }

    public URI getUri() {
        return uri;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Attribute> getAttribute(String name) {
        Set<Attribute> tempAttributes = this.attributesIndex.get(name);
        if (tempAttributes == null) {
            tempAttributes = EMPTY_ATTRIBUTE_SET;
        }
        return tempAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Attribute> getAttributes() {
        return Collections.unmodifiableSet(this.attributes);
    }

    private void addAttribute(String name, String value) {
        Attribute requiredAttribute = new StandardAttribute(name, value);
        this.attributes.add(requiredAttribute);
        Set<Attribute> requiredAttributeSet = new HashSet<Attribute>();
        requiredAttributeSet.add(requiredAttribute);
        this.attributesIndex.put(name, requiredAttributeSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && this.getClass().equals(obj.getClass())) {
            StandardArtifactDescriptor sad = (StandardArtifactDescriptor) obj;
            return this.hash == sad.hash && this.type.equals(sad.type) && this.name.equals(sad.name) && this.version.equals(sad.version);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s with %d attributes", this.uri.getPath(), this.attributes.size());
    }

    /**
     * {@inheritDoc}
     */
    public String getFilename() {
        return this.filename;
    }

}
