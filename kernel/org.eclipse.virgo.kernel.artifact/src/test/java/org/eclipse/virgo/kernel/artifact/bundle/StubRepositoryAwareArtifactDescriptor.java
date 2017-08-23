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

package org.eclipse.virgo.kernel.artifact.bundle;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Version;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;

class StubRepositoryAwareArtifactDescriptor implements RepositoryAwareArtifactDescriptor {

    private final URI uri;
    
    private final String name;
    
    private final Version version;
    
    private final Set<Attribute> attributes;
    
    public StubRepositoryAwareArtifactDescriptor(URI uri, String name, Version version, Set<Attribute> attributes) {
        this.uri = uri;
        this.name  = name;
        this.version = version;
        this.attributes = attributes;
    }

    public Set<Attribute> getAttribute(String name) {
        Set<Attribute> matchingAttribs = new HashSet<Attribute>();
        for(Attribute attrib : this.attributes){
            if(attrib.getKey().equals(name)){
                matchingAttribs.add(attrib);
            }
        }
        return matchingAttribs;
    }

    public Set<Attribute> getAttributes() {
        return this.attributes;
    }

    public String getFilename() {
        return new File(this.uri).getName();
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return "test_type";
    }

    public URI getUri() {
        return this.uri;
    }

    public Version getVersion() {
        return this.version;
    }

    public String getRepositoryName() {
        return "test-repo";
    }

}
