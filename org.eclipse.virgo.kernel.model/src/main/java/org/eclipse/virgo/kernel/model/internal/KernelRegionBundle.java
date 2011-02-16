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

package org.eclipse.virgo.kernel.model.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.osgi.framework.Version;


/**
 * TODO Delete this class when the RAM is upgraded (Bug 
 * 
 * KernelRegionBundle is a placeholder for dependencies that cross the Region boundary in to the Kernel Region.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * KernelRegionBundle is ThreadSafe
 */
public class KernelRegionBundle implements Artifact{

    private static final String TYPE = "bundle";
    
    private static final String NAME = "org.eclipse.virgo.kernel";
    
    private final Version version;
    
    public KernelRegionBundle(Version version) {
        this.version = version;
    }
    
    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public boolean refresh() {
        // no-op
        return false;
    }

    @Override
    public void uninstall() {
        // no-op
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public ArtifactState getState() {
        return ArtifactState.ACTIVE;
    }

    @Override
    public Set<Artifact> getDependents() {
        return new HashSet<Artifact>();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public String toString() {
        return String.format("type: %s, name: %s, version: %s", TYPE, NAME, this.version.toString());
    }

}
