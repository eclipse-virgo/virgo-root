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

import java.util.Set;

import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class RepositoryUtils {
    
    public static <T extends ArtifactDescriptor> T selectHighestVersion(Set<T> artifactDescriptors) {
        return selectHighestVersionInRange(artifactDescriptors, null);
    }

    static <T extends ArtifactDescriptor> T selectHighestVersionInRange(Set<T> artifactDescriptors, VersionRange versionRange) {
        T highest = null;

        for (T artifactDescriptor : artifactDescriptors) {
            Version version = artifactDescriptor.getVersion();
            if ((versionRange == null || versionRange.includes(version)) && (highest == null || (version.compareTo(highest.getVersion()) > 0))) {
                highest = artifactDescriptor;
            }
        }

        return highest;
    }
}
