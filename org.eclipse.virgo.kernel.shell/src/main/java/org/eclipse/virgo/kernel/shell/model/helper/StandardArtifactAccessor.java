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

package org.eclipse.virgo.kernel.shell.model.helper;

import java.util.Map;
import java.util.Set;



/**
 * <p>
 * StandardArtifactAccessor is the standard implementation of {@link ArtifactAccessor}. 
 * It represents an artifact and is backed by an MBean from the Runtime Artifact Model
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardArtifactAccessor is thread safe
 *
 */
final class StandardArtifactAccessor implements ArtifactAccessor {
    
    private static final String TYPE_ATTRIBUTE = "Type";

    private static final String NAME_ATTRIBUTE = "Name";

    private static final String VERSION_ATTRIBUTE = "Version";

    private static final String STATE_ATTRIBUTE = "state";
    
    private final String type;
    
    private final String name;
    
    private final String version;
    
    private final String state;

    private final Map<String, Object> attributes;

    private final Map<String, String> properties;
    
    private final Set<ArtifactAccessorPointer> dependents;

    public StandardArtifactAccessor(Map<String, Object> attributes, Map<String, String> properties, Set<ArtifactAccessorPointer> dependents) {
        this.type = (String) attributes.remove(TYPE_ATTRIBUTE);
        this.name = (String) attributes.remove(NAME_ATTRIBUTE);
        this.version = (String) attributes.remove(VERSION_ATTRIBUTE);
        this.state = (String) attributes.get(STATE_ATTRIBUTE);
        
        this.attributes = attributes;
        this.properties = properties;
        this.dependents = dependents;
    }

    /** 
     * {@inheritDoc}
     */
    public String getType() {
        return this.type;
    }

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersion() {
        return this.version;
    }

    /** 
     * {@inheritDoc}
     */
    public String getState() {
        return this.state;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /** 
     * {@inheritDoc}
     */
    public Set<ArtifactAccessorPointer> getDependents() {
        return this.dependents;
    }

    public int compareTo(ArtifactAccessorPointer o) {
        if(o == null) {
            return 0;
        }
        int typeResult = this.type.compareTo(o.getType());
        if (typeResult != 0) {
            return typeResult;
        }

        int nameResult = this.name.compareTo(o.getName());
        if (nameResult != 0) {
            return nameResult;
        }

        int versionResult = this.version.compareTo(o.getVersion());
        if (versionResult != 0) {
            return versionResult;
        }

        return 0;
    }

}
