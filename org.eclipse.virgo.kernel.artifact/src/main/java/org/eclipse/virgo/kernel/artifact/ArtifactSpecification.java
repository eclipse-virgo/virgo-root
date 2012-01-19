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

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * An <code>ArtifactSpecification</code> is a reference to an artifact by type, name and version <i>range</i> with an
 * optional URI to bypass repository lookup.
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

    private final Map<String, String> properties;

    private final URI uri;

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, and version range and <code>null</code> URI
     * and properties.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange) {
        this(type, name, versionRange, null, null);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range, and URI and with
     * <code>null</code> properties.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     * @param uri the URI of the artifact specification, which may be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange, URI uri) {
        this(type, name, versionRange, uri, null);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range, and properties and with a
     * <code>null</code> URI.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     * @param properties the properties of the artifact specification, which may be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange, Map<String, String> properties) {
        this(type, name, versionRange, null, properties);
    }

    /**
     * Constructs a {@link ArtifactSpecification} with the given type, name, version range, URI, and properties.
     * 
     * @param type the type of the artifact specification, which must not be <code>null</code>
     * @param name the name of the artifact specification, which must not be <code>null</code>
     * @param versionRange the version range of the artifact specification, which must not be <code>null</code>
     * @param uri the URI of the artifact specification, which may be <code>null</code>
     * @param properties the properties of the artifact specification, which may be <code>null</code>
     */
    public ArtifactSpecification(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange, URI uri,
        Map<String, String> properties) {
        this.type = type;
        this.name = name;
        this.versionRange = versionRange;
        this.uri = uri;
        this.properties = properties == null ? IMMUTABLE_EMPTY_MAP : Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the type.
     * 
     * @return the type, neverl <code>null</code>
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the name.
     * 
     * @return the name, never <code>null</code>
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the version range.
     * 
     * @return a {@link VersionRange}, never <code>null</code>
     */
    public VersionRange getVersionRange() {
        return this.versionRange;
    }

    /**
     * Returns the URI. If a URI was not specified, returns <code>null</code>.
     * 
     * @return a {@link URI} or <code>null</code>
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
