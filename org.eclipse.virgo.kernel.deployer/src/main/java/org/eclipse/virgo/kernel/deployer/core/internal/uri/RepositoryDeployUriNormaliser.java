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

package org.eclipse.virgo.kernel.deployer.core.internal.uri;

import java.net.URI;

import org.osgi.framework.Version;


import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * A {@link DeployUriNormaliser} implementation that works with a {@link Repository}. Uris in the form
 * repository://type/name/version are normalised by querying the <code>Repository</code> for a matching
 * {@link ArtifactDescriptor} and, if one is found, returning its {@link ArtifactDescriptor#getUri() URI}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class RepositoryDeployUriNormaliser implements DeployUriNormaliser {

    private static final String SCHEME_REPOSITORY = "repository";
    
    private static final int COMPONENTS_MINIMUM = 2;
    
    private static final int COMPONENTS_MAXIMUM = 3;
    
    private final Repository repository;
    
    private final EventLogger eventLogger;
    
    RepositoryDeployUriNormaliser(@NonNull Repository repository, EventLogger eventLogger) {
        this.repository = repository;
        this.eventLogger = eventLogger;
    }

    public URI normalise(URI uri) throws DeploymentException {
        if (SCHEME_REPOSITORY.equals(uri.getScheme())) {
            return normaliseRepositoryUri(uri);
        }
        return null;
    }

    private URI normaliseRepositoryUri(URI uri) throws DeploymentException {
        String[] tnv = extractTypeNameAndVersion(uri);
        
        URI normalisedUri = null;
        if (tnv.length >= COMPONENTS_MINIMUM && tnv.length <= COMPONENTS_MAXIMUM) {
            String versionString = null;
            if (tnv.length == 3) {
                versionString = tnv[2];
            }
            ArtifactDescriptor artifactDescriptor = lookupArtifactDescriptor(tnv[0], tnv[1], versionString, uri);        
            if (artifactDescriptor != null) {
                normalisedUri = artifactDescriptor.getUri();
            }            
        } else {
            this.eventLogger.log(DeployerLogEvents.REPOSITORY_DEPLOYMENT_URI_MALFORMED, uri);
            throw new DeploymentException("The URI '" + uri + "' is malformed");
        }
        return normalisedUri;
    }

    private String[] extractTypeNameAndVersion(URI uri) {
        String tnv = uri.getSchemeSpecificPart();
        String[] tnvComponents = tnv.split("/");
        return tnvComponents;
    }
    
    private ArtifactDescriptor lookupArtifactDescriptor(String type, String name, String versionString, URI uri) throws DeploymentException {                
        try {
            VersionRange versionRange;
            
            if (versionString == null) {
                versionRange = VersionRange.NATURAL_NUMBER_RANGE;
            } else {
                Version version = new Version(versionString);
                versionRange = VersionRange.createExactRange(version);
            }
            
            ArtifactDescriptor artifactDescriptor = this.repository.get(type, name, versionRange);
            if (artifactDescriptor == null) {
                this.eventLogger.log(DeployerLogEvents.ARTIFACT_NOT_FOUND, type, name, versionRange, this.repository.getName());
                throw new DeploymentException("The URI '" + uri + "' references a non-existent artifact");
            }
            return artifactDescriptor;
        } catch (IllegalArgumentException iae) {
            this.eventLogger.log(DeployerLogEvents.REPOSITORY_DEPLOYMENT_INVALID_VERSION, versionString, uri);
            throw new DeploymentException("The version '" + versionString + "' is invalid");
        }            
    }
}
