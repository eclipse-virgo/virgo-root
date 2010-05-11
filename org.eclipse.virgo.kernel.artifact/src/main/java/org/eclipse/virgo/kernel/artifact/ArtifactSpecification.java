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

package org.eclipse.virgo.kernel.artifact;

import java.util.Collections;
import java.util.Map;

import org.eclipse.virgo.util.osgi.VersionRange;

/**
 * An <code>ArtifactSpecification</code> is a reference to an artifact by type, name and version <i>range</i>.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class ArtifactSpecification {

    private final String type;

    private final String name;

    private final VersionRange versionRange;

    private final Map<String, String> properties;

    public ArtifactSpecification(String type, String name, VersionRange versionRange) {
        this(type, name, versionRange, null);
    }

    public ArtifactSpecification(String type, String name, VersionRange versionRange, Map<String, String> properties) {
        this.type = type;
        this.name = name;
        this.versionRange = versionRange;
        this.properties = properties == null ? Collections.<String, String> emptyMap() : Collections.unmodifiableMap(properties);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public VersionRange getVersionRange() {
        return versionRange;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

}
