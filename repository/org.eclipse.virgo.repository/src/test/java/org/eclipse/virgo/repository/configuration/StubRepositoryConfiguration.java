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

package org.eclipse.virgo.repository.configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.configuration.PersistentRepositoryConfiguration;


public class StubRepositoryConfiguration extends PersistentRepositoryConfiguration {

    public StubRepositoryConfiguration(ArtifactBridge artifactBridge, File indexLocation) {
        super("stub-repository", indexLocation, createArtifactBridges(artifactBridge), null);
    }

    private static Set<ArtifactBridge> createArtifactBridges(ArtifactBridge artifactBridge) {
        Set<ArtifactBridge> artifactBridges = new HashSet<ArtifactBridge>();
        artifactBridges.add(artifactBridge);

        return artifactBridges;
    }
}
