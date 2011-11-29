/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
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

/**
 * {@link PlanDescriptor} is a parsed form of a plan file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public final class PlanDescriptor {

    public enum Dependencies {
        INSTALL, NO_INSTALL;

        /** 
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this == INSTALL ? "install" : "noinstall";
        }
    }

    private final String name;

    private final Version version;

    private final boolean scoped;

    private final boolean atomic;

    private final Dependencies dependencies;

    private final List<ArtifactSpecification> artifactSpecifications;

    public PlanDescriptor(String name, Version version, boolean scoped, boolean atomic, Dependencies dependencies,
        List<ArtifactSpecification> artifactSpecifications) {
        this.name = name;
        this.version = version;
        this.scoped = scoped;
        this.atomic = atomic;
        this.dependencies = dependencies;
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

    public Dependencies getDependencies() {
        return dependencies;
    }

    public List<ArtifactSpecification> getArtifactSpecifications() {
        return artifactSpecifications;
    }

}
