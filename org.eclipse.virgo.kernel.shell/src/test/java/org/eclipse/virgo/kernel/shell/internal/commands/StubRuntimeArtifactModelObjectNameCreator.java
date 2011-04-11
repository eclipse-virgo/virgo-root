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

package org.eclipse.virgo.kernel.shell.internal.commands;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.osgi.region.Region;

public final class StubRuntimeArtifactModelObjectNameCreator implements RuntimeArtifactModelObjectNameCreator {

    public ObjectName create(Artifact artifact) {
        throw new UnsupportedOperationException();
    }

    public ObjectName create(String type, String name, Version version) {
        try {
            return new ObjectName("test:type=Model,artifact-type=" + type + ",name=" + name + ",version=" + version);
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public ObjectName create(String type, String name, Version version, Region region) {
        try {
            return new ObjectName("test:type=RegionModel,artifact-type=" + type + ",name=" + name + ",version=" + version + "region="
                + region.getName());
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

    public ObjectName createArtifactVersionsQuery(String type, String name) {
        try {
            return new ObjectName("test:type=Model,artifact-type=" + type + ",name=" + name + ",*");
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    public String getName(ObjectName objectName) {
        return objectName.getKeyProperty("name");
    }

    public String getType(ObjectName objectName) {
        return objectName.getKeyProperty("type");
    }

    public String getVersion(ObjectName objectName) {
        return objectName.getKeyProperty("version");
    }

}
