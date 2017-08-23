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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.nano.deployer.api.config.ConfigurationDeployer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.util.io.IOUtils;

public final class ConfigLifecycleEngine implements StartEngine, RefreshEngine, StopEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BundleContext context;
    private ConfigurationDeployer configurationDeployer;

    public ConfigLifecycleEngine(BundleContext context) {
    	this.context = context;
    }

    public void start(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws StartException {
    	
    	initialiseConfigurationDeployer();
		
        try {
            updateConfiguration(artifactIdentity, artifactFS);
        } catch (IOException e) {
            String message = String.format("Unable to start configuration '%s' with '%s'", artifactIdentity.getName(), artifactFS);
            logger.error(message);
            throw new StartException(message, e);
        }
    }

	private void initialiseConfigurationDeployer() {
		ServiceReference<ConfigurationDeployer> configurationImporterRef = context.getServiceReference(ConfigurationDeployer.class);
		this.configurationDeployer = context.getService(configurationImporterRef);
	}

    public void refresh(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws RefreshException {
        try {
            updateConfiguration(artifactIdentity, artifactFS);
        } catch (IOException e) {
            String message = String.format("Unable to refresh configuration '%s' with '%s'", artifactIdentity.getName(), artifactFS);
            logger.error(message);
            throw new RefreshException(message, e);
        }
    }

    private void updateConfiguration(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = artifactFS.getEntry("").getInputStream();
            configurationDeployer.publishConfiguration(artifactIdentity.getName(), getProperties(inputStream));
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private Properties getProperties(InputStream inputSteam) throws IOException {
        Properties p = new Properties();
        p.load(inputSteam);
        return p;
    }

    public void stop(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws StopException {
        try {
        	configurationDeployer.deleteConfiguration(artifactIdentity.getName());
        } catch (IOException e) {
            String message = String.format("Unable to stop configuration '%s'", artifactIdentity.getName());
            logger.error(message);
            throw new StopException(message, e);
        }
    }

}
