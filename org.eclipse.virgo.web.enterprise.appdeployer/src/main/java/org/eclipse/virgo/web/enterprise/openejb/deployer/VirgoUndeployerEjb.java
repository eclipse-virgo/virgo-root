/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.deployer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class VirgoUndeployerEjb {
	private final String modulePath;
	private AppInfo appForUndeploy = null;
	private static final String OPENEJB_RESOURCES_SUBCONTEXT_NAME = "openejb/Resource/";
	
	public VirgoUndeployerEjb(String modulePath) {
		this.modulePath = modulePath;
		Assembler assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
		Collection<AppInfo> apps = assembler.getDeployedApplications();
		for (AppInfo appInfo : apps) {
			if (modulePath.equals(appInfo.path)) {
				appForUndeploy = appInfo;
				break;
			}
		}
	}
		
	public void undeploy() throws NoSuchApplicationException {
		if (appForUndeploy == null) {
			throw new NoSuchApplicationException("Application with path " + modulePath + "cannot be found");
		}
		
		// TODO: revision and remove because OpenEJB removes the resources from the application module when it installs them
		ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
		Context context = containerSystem.getJNDIContext();
		if (appForUndeploy != null) {
			Set<String> resources = appForUndeploy.resourceIds;
			for (String resourceId : resources) {
				try {
					context.unbind(resourceId);
				} catch (NamingException e) {
					// do nothing
				}
			}
		}
		
	}
	
	public void clearResources(String moduleId) throws UndeployException, NoSuchApplicationException {
		if (appForUndeploy == null) {
			throw new NoSuchApplicationException("Application with path " + modulePath + "cannot be found");
		}
		
		OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
		List<ResourceInfo> resources = config.facilities.resources;
		Iterator<ResourceInfo> iResourceInfos = resources.iterator();
		while (iResourceInfos.hasNext()) {
			ResourceInfo resourceInfo = iResourceInfos.next();
			if (resourceInfo.id.contains(appForUndeploy.appId)) {
				iResourceInfos.remove();
			}
		}
		
		Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
		
		try {			
			context.unbind(OPENEJB_RESOURCES_SUBCONTEXT_NAME + appForUndeploy.appId);
		} catch (NamingException e) {
			throw new UndeployException("Failed to unbind resorce", e);
		}
	}
}
