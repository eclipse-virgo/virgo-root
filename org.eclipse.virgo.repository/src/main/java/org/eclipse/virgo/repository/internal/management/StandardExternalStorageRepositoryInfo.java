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

package org.eclipse.virgo.repository.internal.management;

import java.net.URI;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.MutableRepository;
import org.eclipse.virgo.repository.management.ArtifactDescriptorSummary;
import org.eclipse.virgo.repository.management.ExternalStorageRepositoryInfo;
import org.osgi.framework.Version;


/**
 * Standard implementation of {@link ExternalStorageRepositoryInfo}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 *
 */
public class StandardExternalStorageRepositoryInfo extends AbstractRepositoryInfo implements ExternalStorageRepositoryInfo {
    
    private static final String TYPE = "external";
    
    private final MutableRepository repository;
    
    public StandardExternalStorageRepositoryInfo(String name, ArtifactDescriptorDepository depository, MutableRepository repository) {
        super(name, depository);
        this.repository = repository;
    }
    
    public String getType() {
        return TYPE;
    }

    /** 
     * {@inheritDoc}
     */
    public ArtifactDescriptorSummary publish(String artifactUri) {
        URI uri = URI.create(artifactUri);
        
        try {
            ArtifactDescriptor published = this.repository.publish(uri);
            ArtifactDescriptorSummary summary = null;
            if (published != null) {
                summary = new ArtifactDescriptorSummary(published.getType(), published.getName(), published.getVersion().toString());   
            }
            return summary;
        } catch (DuplicateArtifactException dae) {
            throw new IllegalArgumentException("The artifact '" + artifactUri + "' cannot be published: it is a duplicate of existing artifact '" + dae.getOriginal().getUri() + "'");
        }
    }

    /** 
     * {@inheritDoc}
     */
    public boolean retract(String type, String name, String version) {
        return this.repository.retract(type, name, new Version(version));
    }

    public Set<String> getArtifactLocations(String filename) {
        return this.repository.getArtifactLocations(filename);
    }
}
