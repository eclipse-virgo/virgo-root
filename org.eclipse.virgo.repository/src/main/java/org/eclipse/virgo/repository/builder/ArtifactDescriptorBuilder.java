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

package org.eclipse.virgo.repository.builder;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptor;
import org.osgi.framework.Version;


/**
 * A simple builder API for creating instances of {@link ArtifactDescriptor}.
 * <p />
 * Created <code>ArtifactDescriptor</code>s are created by calling {@link #build()}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe. Intended for single-threaded usage.
 * 
 */
public final class ArtifactDescriptorBuilder {

    private volatile URI uri;

    private volatile String fileName;

    private volatile String type;

    private volatile String name;

    private volatile Version version;

    private final Set<Attribute> attributes = new HashSet<Attribute>();

    /**
     * Sets the URI for the {@link ArtifactDescriptor} being created
     * 
     * @param uri the URI for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder setUri(URI uri) {
        this.uri = uri;
        this.fileName = deriveFilename(uri);
        return this;
    }

    /**
     * Sets the type for the {@link ArtifactDescriptor} being created
     * 
     * @param type the type for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the name for the {@link ArtifactDescriptor} being created
     * 
     * @param name the name for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the version for the {@link ArtifactDescriptor} being created
     * 
     * @param version the version for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder setVersion(Version version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the version for the {@link ArtifactDescriptor} being created. This method converts the {@link String} to a
     * proper {@link Version} internally. This conversion may fail if the input is not a properly formatted version
     * string.
     * 
     * @param version the version for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder setVersion(String version) {
        this.version = new Version(version);
        return this;
    }

    /**
     * Adds an attribute for the {@link ArtifactDescriptor} being created
     * 
     * @param attribute the attribute for the {@link ArtifactDescriptor} being created
     * @return <code>this</code> {@link ArtifactDescriptorBuilder}
     */
    public ArtifactDescriptorBuilder addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    /**
     * Build an {@link ArtifactDescriptor} from the values set on this builder. Creation of this
     * {@link ArtifactDescriptor} is not guaranteed to complete successfully if some of the values have not been set or
     * are set to illegal values.
     * 
     * @return a new {@link ArtifactDescriptor} created from the values set on this builder
     */
    public ArtifactDescriptor build() {
        return new StandardArtifactDescriptor(this.uri, this.type, this.name, this.version, this.fileName, this.attributes);
    }

    private String deriveFilename(URI uri) {
        if ("file".equals(uri.getScheme())) {
            return new File(uri).getName();
        } else {
            return null;
        }
    }
}
