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

package org.eclipse.virgo.kernel.artifact.plan;

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.osgi.framework.Version;

// TODO Javadoc
public final class PlanDescriptor {

    private final String name;

    private final Version version;

    private final boolean scoped;

    private final boolean atomic;

    private final List<ArtifactSpecification> artifactSpecifications;

    public PlanDescriptor(String name, Version version, boolean scoped, boolean atomic, List<ArtifactSpecification> artifactSpecifications) {
        this.name = name;
        this.version = version;
        this.scoped = scoped;
        this.atomic = atomic;
        this.artifactSpecifications = artifactSpecifications;
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public boolean getScoped() {
        return scoped;
    }

    public boolean getAtomic() {
        return atomic;
    }

    public List<ArtifactSpecification> getArtifactSpecifications() {
        return artifactSpecifications;
    }

}
