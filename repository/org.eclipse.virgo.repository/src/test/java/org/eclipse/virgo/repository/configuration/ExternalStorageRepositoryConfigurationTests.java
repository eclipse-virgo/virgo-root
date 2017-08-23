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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.junit.Before;
import org.junit.Test;


public class ExternalStorageRepositoryConfigurationTests {

    private ExternalStorageRepositoryConfiguration configuration;

    private String name;

    private File indexLocation;

    private Set<ArtifactBridge> artefactBridges;

    private String searchPattern;

    @Before
    public void createConfiguration() {
        name = "repo-name";
        indexLocation = new File("build/index");
        artefactBridges = new HashSet<ArtifactBridge>();
        artefactBridges.add(new ArtifactBridge() {

            public ArtifactDescriptor generateArtifactDescriptor(File artifact) throws ArtifactGenerationException {
                return null;
            }
        });
        searchPattern = "search-pattern";

        configuration = new ExternalStorageRepositoryConfiguration(name, indexLocation, artefactBridges, searchPattern, null);
    }

    @Test
    public void name() {
        assertEquals(name, configuration.getName());
    }

    @Test
    public void indexLocation() {
        assertEquals(indexLocation, configuration.getIndexLocation());
    }

    @Test
    public void searchPattern() {
        assertEquals(searchPattern, configuration.getSearchPattern());
    }

    @Test
    public void artefactBridges() {
        assertEquals(artefactBridges, configuration.getArtefactBridges());
    }
}
