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

package org.eclipse.virgo.kernel.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.osgi.framework.Version;

public class StubArtifactRepository implements RuntimeArtifactRepository {

    private final Set<Artifact> artifacts = new HashSet<Artifact>();

    public boolean add(Artifact artifact) {
        return this.artifacts.add(artifact);
    }

    public boolean remove(Artifact artifact) {
        return this.remove(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getRegion());
    }

    public boolean remove(String type, String name, Version version, Region region) {
        return this.artifacts.remove(getArtifact(type, name, version, region));
    }

    public Set<Artifact> getArtifacts() {
        return this.artifacts;
    }

    public Artifact getArtifact(String type, String name, Version version, Region region) {
        for (Artifact artifact : this.artifacts) {
            if (artifact.getType().equals(type) && artifact.getName().equals(name) && artifact.getVersion().equals(version) && artifact.getRegion().equals(region)) {
                return artifact;
            }
        }
        return null;
    }
}
