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

package org.eclipse.virgo.web.core.internal;

import java.io.File;
import java.util.Locale;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;

/**
 * {@link WebArtifactIdentityDeterminer} detects the types of web application artifacts.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class WebArtifactIdentityDeterminer implements ArtifactIdentityDeterminer {

    private static final String WAR_EXTENSION = ".war";
    
    private final BundleBridge bundleBridge;
    
    public WebArtifactIdentityDeterminer(HashGenerator hashGenerator) {
        this.bundleBridge = new BundleBridge(hashGenerator);
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactIdentity determineIdentity(File file, String scopeName) {
        
        String fileName = file.getName();
        
        if (fileName != null && fileName.toLowerCase(Locale.ENGLISH).endsWith(WAR_EXTENSION)) {
            ArtifactDescriptor artifactDescriptor = null;
            try {
                artifactDescriptor = this.bundleBridge.generateArtifactDescriptor(file);
            } catch (ArtifactGenerationException ignored) {
            }
            
            if (artifactDescriptor == null) {
                return new ArtifactIdentity(ArtifactIdentityDeterminer.BUNDLE_TYPE, trimExtension(file), Version.emptyVersion, null);         
            } else {                                
                return new ArtifactIdentity(artifactDescriptor.getType(), artifactDescriptor.getName(), artifactDescriptor.getVersion(), scopeName);
            }
        }
        return null;
    }
     
    private String trimExtension(File file) {
        String filename = file.getName();
        
        int lastIndexOf = filename.lastIndexOf('.');
        
        if (lastIndexOf > 0) {
            return filename.substring(0, lastIndexOf);
        } else {
            return filename;
        }
    }
    
}
