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

package org.eclipse.virgo.kernel.deployer.test;

import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;

public class AbstractRAMIntegrationTests extends AbstractDeployerIntegrationTest {
    
    public ManageableArtifact getManageableArtifact(DeploymentIdentity deploymentIdentity, Region region) {
        return getManageableArtifact(deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(), new Version(deploymentIdentity.getVersion()), region);
    }
    
    public ManageableArtifact getManageableArtifact(String type, String name, Version version, Region region) {
        RuntimeArtifactModelObjectNameCreator objectNameCreator = OsgiFrameworkUtils.getService(this.kernelContext, RuntimeArtifactModelObjectNameCreator.class).getService();
        ObjectName objectName = objectNameCreator.createArtifactModel(type, name, version, region);
        
        return getManageableArtifact(objectName);
    }
    
    private ManageableArtifact getManageableArtifact(ObjectName objectName) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();        
        ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
        return artifact;
    }
}
