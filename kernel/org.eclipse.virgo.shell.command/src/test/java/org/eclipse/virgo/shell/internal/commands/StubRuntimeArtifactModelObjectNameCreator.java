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

package org.eclipse.virgo.shell.internal.commands;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.osgi.framework.Version;

public final class StubRuntimeArtifactModelObjectNameCreator implements RuntimeArtifactModelObjectNameCreator {
    
    public ObjectName createArtifactModel(Artifact artifact) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectName createArtifactModel(String type, String name, Version version, Region region) {
        try {
            return new ObjectName("test:type=ArtifactModel,artifact-type=" + type + ",name=" + name + ",version=" + version + ",region=" + region.getName());
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    public ObjectName createArtifactsOfTypeQuery(String type) {
        try {
            return new ObjectName("test:artifact-type=" + type + ",*");
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    public ObjectName createArtifactsQuery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectName createAllArtifactsQuery() {
        throw new UnsupportedOperationException();
    }

    public ObjectName createArtifactVersionsQuery(String type, String name) {
        try {
            return new ObjectName("test:type=ArtifactModel,artifact-type=" + type + ",name=" + name + ",*");
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    public String getName(ObjectName objectName) {
        return objectName.getKeyProperty("name");
    }

    public String getVersion(ObjectName objectName) {
        return objectName.getKeyProperty("version");
    }

	@Override
	public String getRegion(ObjectName objectName) {
        return objectName.getKeyProperty("region");
	}

}
