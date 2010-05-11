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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;


/**
 * <p>
 * TODO Document StubArtifactBridge
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of StubArtifactBridge
 *
 */
public class StubArtifactBridge implements ArtifactBridge {

    private final String planType;
    
    private final String[] matchs;

    public StubArtifactBridge(String planType, String... matchs) {
        this.planType = planType;
        this.matchs = matchs;
    }

    /** 
     * {@inheritDoc}
     */
    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {
        boolean matched = false;
        for(String ending : this.matchs){
            if(artifactFile.getName().endsWith(ending)){
                matched = true;
            }
        }
        
        if(matched){
            return new ArtifactDescriptor() {
                
                public Version getVersion() {
                    return null;
                }
                
                public URI getUri() {
                    return null;
                }
                
                public String getType() {
                    return planType;
                }
                
                public String getName() {
                    return null;
                }
                
                public String getFilename() {
                    return null;
                }
                
                public Set<Attribute> getAttributes() {
                    return null;
                }
                
                public Set<Attribute> getAttribute(String name) {
                    return null;
                }
            };
            } 
        return null;
    }

}
