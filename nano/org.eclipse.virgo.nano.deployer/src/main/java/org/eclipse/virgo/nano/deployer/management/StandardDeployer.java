/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.management;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.virgo.nano.deployer.api.ArtifactIdentity;
import org.eclipse.virgo.nano.deployer.api.Deployer;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;


/**
 * Standard implementation of the deployer's control.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe assuming that the ApplicationDeployer is thread-safe.
 * 
 */
public class StandardDeployer implements Deployer {

    private final ApplicationDeployer applicationDeployer;

    public StandardDeployer(ApplicationDeployer applicationDeployer) {
        this.applicationDeployer = applicationDeployer;
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(String uri) throws DeploymentException {
        return this.applicationDeployer.deploy(URI.create(uri));
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(String uri, boolean recoverable) throws DeploymentException {
        return this.applicationDeployer.deploy(URI.create(uri), new DeploymentOptions(recoverable, false, true));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public void undeploy(String applicationSymbolicName, String version) throws DeploymentException {
        this.applicationDeployer.undeploy(applicationSymbolicName, version);
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(String uri, String symbolicName) throws DeploymentException {
        this.applicationDeployer.refresh(URI.create(uri), symbolicName);
    }

    /**
     * {@inheritDoc}
     */
    public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
        this.applicationDeployer.refreshBundle(bundleSymbolicName, bundleVersion);
    }

    public ArtifactIdentity install(String artifactUri) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = this.applicationDeployer.install(createURI(artifactUri), new DeploymentOptions());
        return new ArtifactIdentity(deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(), deploymentIdentity.getVersion());
    }

    public ArtifactIdentity install(String artifactUri, boolean recover) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = this.applicationDeployer.install(createURI(artifactUri), new DeploymentOptions(recover, false, true));
        return new ArtifactIdentity(deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(), deploymentIdentity.getVersion());
    }

    public ArtifactIdentity install(String type, String name, String version) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented, use deploy instead of install and start");
    }

    public ArtifactIdentity install(String type, String name, String version, boolean recover) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented, use deploy instead of install and start");
    }

    public void start(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException {
        throw new UnsupportedOperationException("Not yet implemented, use deploy instead of install and start");

    }

    public void start(String type, String name, String version) throws DeploymentException, IllegalStateException {
        throw new UnsupportedOperationException("Not yet implemented, use deploy instead of install and start");
    }

    public void stop(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException {
        throw new UnsupportedOperationException("Not yet implemented, use undeploy instead of stop and uninstall");
    }

    public void stop(String type, String name, String version) throws DeploymentException, IllegalStateException {
        throw new UnsupportedOperationException("Not yet implemented, use undeploy instead of stop and uninstall");
    }

    public void uninstall(ArtifactIdentity artifactIdentity) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented, use undeploy instead of stop and uninstall");
    }

    public void uninstall(String type, String name, String version) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented, use undeploy instead of stop and uninstall");
    }

    private URI createURI(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException urise) {
            throw new IllegalArgumentException(String.format("The location '%s' is not a valid URI", uriString));
        }

        if ("file".equals(uri.getScheme())) {
            uri = new File(uri.getSchemeSpecificPart()).toURI();
        }

        return uri;
    }
}
