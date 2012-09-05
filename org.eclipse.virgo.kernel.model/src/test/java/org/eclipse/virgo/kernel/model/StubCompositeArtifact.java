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

package org.eclipse.virgo.kernel.model;

import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.osgi.framework.Version;

public class StubCompositeArtifact implements CompositeArtifact {

    private final String name;
    
    private final String type;
    
    private final Region region;
    
    public StubCompositeArtifact() {
        this("test-type", "test-name", new StubRegion("test-region", null));
    }

    public StubCompositeArtifact(String type, String name, Region region) {
        this.type = type;
        this.name = name;
        this.region = region;
    }
    
    public Set<Artifact> getDependents() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public ArtifactState getState() {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        return type;
    }

    public Version getVersion() {
        return Version.emptyVersion;
    }

    public Region getRegion() {
        return this.region;
    }

    public boolean refresh() {
        throw new UnsupportedOperationException();
    }

    public void start() {
        throw new UnsupportedOperationException();
    }

    public void stop() {
        throw new UnsupportedOperationException();
    }

    public void uninstall() {
        throw new UnsupportedOperationException();
    }

    public boolean isAtomic() {
        throw new UnsupportedOperationException();
    }

    public boolean isScoped() {
        throw new UnsupportedOperationException();
    }

    public Map<String, String> getProperties() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
    	return "StubCompositeRegion type:" + this.type + " name:" + this.name + " region:" + this.region.getName();
    }

}
