/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.artifact;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * An <code>ArtifactSpecification</code> is a reference to an artifact by type, name and version <i>range</i> or URI,
 * but not both, together with some properties.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class ArtifactSpecification {

    private static final Map<String, String> IMMUTABLE_EMPTY_MAP = Collections.<String, String> emptyMap();

    private final String type;

    private final String name;

    private final VersionRange versionRange;

    private final URI uri;

    private final Map<String, String> properties;

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range and no properties.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange) {
        this(type, name, versionRange, null, null);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range, and properties.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     * @param properties the properties of the artifact specification, which must not be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange,
        @NonNull Map<String, String> properties) {
        this(type, name, versionRange, null, properties);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given URI and with no properties.
     * 
     * @param uri the URI of the artifact specification, which must not be <code>null</code>
     */
    public ArtifactSpecification(@NonNull URI uri) {
        this(null, null, null, uri, null);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given URI and with no properties.
     * 
     * @param uri the URI of the artifact specification, which must not be <code>null</code>
     * @param properties the properties of the artifact specification, which must not be <code>null</code>
     */
    public ArtifactSpecification(@NonNull URI uri, @NonNull Map<String, String> properties) {
        this(null, null, null, uri, properties);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range, URI, and properties.
     * <p>
     * Either the type, name, and version range must be non-<code>null</code> and the URI <code>null</code> or the URI
     * must be non-<code>null</code> and the type, name, and version range <code>null</code>. The properties may be
     * <code>null</code>.
     * 
     * @param type the type of the artifact specification
     * @param name the name of the artifact specification
     * @param versionRange the version range of the artifact specification
     * @param uri the URI of the artifact specification
     * @param properties the properties of the artifact specification, which may be <code>null</code>
     */
    private ArtifactSpecification(String type, String name, VersionRange versionRange, URI uri, Map<String, String> properties) {
        this.type = type;
        this.name = name;
        this.versionRange = versionRange;
        this.uri = uri;
        this.properties = properties == null ? IMMUTABLE_EMPTY_MAP : Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the type.
     * 
     * @return the type, or <code>null</code> if this artifact specification is by URI
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the name.
     * 
     * @return the name, or <code>null</code> if this artifact specification is by URI
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the version range.
     * 
     * @return a {@link VersionRange}, or <code>null</code> if this artifact specification is by URI
     */
    public VersionRange getVersionRange() {
        return this.versionRange;
    }

    /**
     * Returns the URI. If a URI was not specified, returns <code>null</code>.
     * 
     * @return a {@link URI} or <code>null</code> if this artifact specification is by type, name, and version range
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Returns the properties. If properties were not specified, returns an empty map. The returned properties may not
     * be modified.
     * 
     * @return a properties map, never <code>null</code>
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

}
