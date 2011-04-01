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

package org.eclipse.virgo.kernel.model.management.internal;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.Version;

/**
 * The default implementation of {@link RuntimeArtifactModelObjectNameCreator}. This implementation creates names based
 * on the following pattern:
 * <p />
 * <code>&lt;domain&gt;:type=Model,artifact-type=&lt;type&gt;,name=&lt;name&gt;,version=&lt;version&gt;</code>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class DefaultRuntimeArtifactModelObjectNameCreator implements RuntimeArtifactModelObjectNameCreator {

    private static final String USER_REGION_NAME = "org.eclipse.virgo.region.user";

    private static final String ARTIFACTS_FORMAT = "%s:type=Model,*";

    private static final String ARTIFACTS_OF_TYPE_FORMAT = "%s:type=Model,artifact-type=%s,*";

    private static final String ARTIFACTS_OF_TYPE_AND_NAME_FORMAT = "%s:type=Model,artifact-type=%s,name=%s,*";

    private static final String ARTIFACT_FORMAT = "%s:type=Model,artifact-type=%s,name=%s,version=%s";
    
    private static final String EXTENDED_ARTIFACT_FORMAT = "%s:type=RegionModel,artifact-type=%s,name=%s,version=%s,region=%s";

    private static final String KEY_TYPE = "artifact-type";

    private static final String KEY_NAME = "name";

    private static final String KEY_VERSION = "version";

    private final String domain;

    public DefaultRuntimeArtifactModelObjectNameCreator(String domain) {
        this.domain = domain;
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName create(@NonNull Artifact artifact) {
        Region region = artifact.getRegion();
        // Treat user region artifacts specially to preserve JMX compatibility with Virgo 2.1.0.
        if (region == null || USER_REGION_NAME.equals(region.getName())) {
            return create(artifact.getType(), artifact.getName(), artifact.getVersion());
        }
        return create(artifact.getType(), artifact.getName(), artifact.getVersion(), region);
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName create(String type, String name, Version version, Region region) {
        String regionName = region == null ? "?" : region.getName();
        return createObjectName(String.format(EXTENDED_ARTIFACT_FORMAT, this.domain, type, name, version, regionName));
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName create(String type, String name, Version version) {
        return createObjectName(String.format(ARTIFACT_FORMAT, this.domain, type, name, version));
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName createArtifactsOfTypeQuery(String type) {
        return createObjectName(String.format(ARTIFACTS_OF_TYPE_FORMAT, this.domain, type));
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName createArtifactVersionsQuery(String type, String name) {
        return createObjectName(String.format(ARTIFACTS_OF_TYPE_AND_NAME_FORMAT, this.domain, type, name));
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName createArtifactsQuery() {
        return createObjectName(String.format(ARTIFACTS_FORMAT, this.domain));
    }

    /**
     * {@inheritDoc}
     */
    public String getType(ObjectName objectName) {
        return objectName.getKeyProperty(KEY_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public String getName(ObjectName objectName) {
        return objectName.getKeyProperty(KEY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion(ObjectName objectName) {
        return objectName.getKeyProperty(KEY_VERSION);
    }

    private ObjectName createObjectName(String objectName) {
        try {
            return new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(String.format("Unable to create object name '%s'", objectName), e);
        } catch (NullPointerException e) {
            throw new RuntimeException(String.format("Unable to create object name '%s'", objectName), e);
        }
    }
}
