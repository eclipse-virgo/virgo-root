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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;

/**
 */
final public class StubArtifactAccessorAndPointer implements ArtifactAccessor, ArtifactAccessorPointer {
    
    private final String testType;
    
    private final String testName;
    
    private final String testVersion;
    
    private final String testState;
    
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    public StubArtifactAccessorAndPointer(String testType, String testName, String testVersion, String testState) {
        this.testType = testType;
        this.testName = testName;
        this.testVersion = testVersion;
        this.testState = testState;

        this.attributes.put("State", testState);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getType() {
        return this.testType;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.testName;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return this.testVersion;
    }

    /**
     * {@inheritDoc}
     */
    public String getState() {
        return this.testState;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        return new HashMap<String, String>();
    }

    /**
     * {@inheritDoc}
     */
    public Set<ArtifactAccessorPointer> getDependents() {
        return new HashSet<ArtifactAccessorPointer>();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(ArtifactAccessorPointer o) {
        return 0;
    }
}
