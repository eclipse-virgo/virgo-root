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

package org.eclipse.virgo.nano.deployer.hot;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.io.FileSystemEvent;
import org.eclipse.virgo.util.io.FileSystemListener;
import org.eclipse.virgo.util.io.PathReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FileSystemListener} that monitors a pickup directory for file system events. When a file is created it is
 * passed to the {@link ApplicationDeployer} for deployment. When a file is modified, it is re-deployed. When a file is
 * deleted, the application is undeployed.
 * <p />
 * The <code>ApplicationDeployer</code> is responsible for recovering the files deployed via this route and is given
 * ownership of these files so that undeploying one of them, e.g. by name and version, will delete the corresponding
 * file in the pickup directory.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class HotDeploymentFileSystemListener implements FileSystemListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EventLogger eventLogger;

    private final ApplicationDeployer deployer;

    /**
     * Creates a new <code>HotDeploymentFileSystemListener</code>.
     * 
     * @param deployer the {@link ApplicationDeployer} to deploy to.
     * @param eventLogger where to log events
     */
    public HotDeploymentFileSystemListener(@NonNull ApplicationDeployer deployer, EventLogger eventLogger) {
        this.deployer = deployer;
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     * 
     * Reacts to changes in the pickup directory and calls the {@link ApplicationDeployer} as appropriate.
     */
    @Override
    public void onChange(String path, FileSystemEvent event) {
        String fileName = new PathReference(path).getName();
        this.eventLogger.log(HotDeployerLogEvents.HOT_DEPLOY_PROCESSING_FILE, event, fileName);
        try {
            if (event == FileSystemEvent.CREATED) {
                this.logger.info("ApplicationDeploying path '{}'.", path);
                deploy(path);
            } else if (event == FileSystemEvent.MODIFIED) {
                this.logger.info("Redeploying path '{}'.", path);
                deploy(path);
            } else if (event == FileSystemEvent.DELETED) {
                this.logger.info("ApplicationUndeploying path '{}'.", path);
                undeploy(path);
            } else if (event == FileSystemEvent.INITIAL) {
                this.logger.info("ApplicationConditionallyDeploying path '{}'.", path);
                deployIfNotDeployed(path, fileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            determineFailureAndLogMessage(event, fileName, ex);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Reacts to initial event in the pickup directory and calls the bulk deploy method in {@link ApplicationDeployer}.
     */
    @Override
    public void onInitialEvent(List<String> paths) {
        this.eventLogger.log(HotDeployerLogEvents.HOT_DEPLOY_PROCESSING_FILE, FileSystemEvent.INITIAL, getConcatenatedPaths(paths));
        try {
            bulkDeployIfNotDeployed(paths);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            determineFailureAndLogMessage(FileSystemEvent.INITIAL, getConcatenatedPaths(paths), ex);
        }
    }

    private String getConcatenatedPaths(List<String> paths) {
        StringBuilder sb = new StringBuilder("");
        for (String path : paths) {
            sb.append(new PathReference(path).getName()).append("; ");
        }
        return new String(sb);
    }

    /**
     * Returns only the source artifacts' URIs that need to be deployed or updated.
     */
    private List<URI> getUrisToDeploy(List<String> sourceArtefacts) {
        List<URI> resultUris = new ArrayList<URI>();
        for (String sourceArtefact : sourceArtefacts) {
            if (!isDeployed(sourceArtefact)) {
                resultUris.add(getDefinitiveUri(sourceArtefact));
                this.logger.info("ApplicationConditionallyDeploying path '{}'.", sourceArtefact);
            } else {
                this.eventLogger.log(HotDeployerLogEvents.HOT_DEPLOY_SKIPPED, sourceArtefact);
            }
        }
        return resultUris;
    }

    /**
     * Triggers bulk deployment of source artifacts that are not yet deployed.
     * 
     * @param sourceArtefacts the source artifacts URI strings
     * @throws DeploymentException
     */
    private void bulkDeployIfNotDeployed(List<String> sourceArtefacts) throws DeploymentException {
        this.deployer.bulkDeploy(getUrisToDeploy(sourceArtefacts), new DeploymentOptions(true, true, false));
    }

    /**
     * Determines the {@link LogEvent} that corresponds the {@link FileSystemEvent}.
     */
    private void determineFailureAndLogMessage(FileSystemEvent event, String fileName, Exception ex) {
        switch (event) {
            case CREATED: // fall through
            case INITIAL:
                this.eventLogger.log(HotDeployerLogEvents.HOT_DEPLOY_FAILED, ex, fileName);
                break;
            case MODIFIED:
                this.eventLogger.log(HotDeployerLogEvents.HOT_REDEPLOY_FAILED, ex, fileName);
                break;
            case DELETED:
                this.eventLogger.log(HotDeployerLogEvents.HOT_UNDEPLOY_FAILED, ex, fileName);
                break;
        }
    }

    /**
     * Undeploys the application that corresponds to the supplied source artefact URI.
     * 
     * @param sourceArtefact the source artefact URI string
     * @throws DeploymentException
     */
    private void undeploy(String sourceArtefact) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = getDeploymentIdentity(sourceArtefact);
        if (deploymentIdentity != null) {
            this.deployer.undeploy(deploymentIdentity, true);
        }
    }

    /**
     * Get the {@link DeploymentIdentity} of the given artefact. Return <code>null</code> if the given artefact is not
     * currently deployed.
     * 
     * @param sourceArtefact the source artefact URI string
     * @return the <code>DeploymentIdentity</code> of the given artefact or <code>null</code>
     */
    private DeploymentIdentity getDeploymentIdentity(String sourceArtefact) {
        return this.deployer.getDeploymentIdentity(getDefinitiveUri(sourceArtefact));
    }

    /**
     * Determine whether or not the given artefact is already deployed. Return <code>true</code> if the given artefact
     * at its file's last modified time is already deployed.
     * 
     * @param sourceArtefact the source artefact URI string
     * @return <code>true</code> if and only if the given artefact at its file's last modified time is already deployed
     */
    private boolean isDeployed(String sourceArtefact) {
        return this.deployer.isDeployed(getDefinitiveUri(sourceArtefact));
    }

    /**
     * Converts a string URI to a URI with a predictable format, particularly in the case where the string URI ends in a
     * file separator.
     * 
     * @param sourceArtefact the URI string
     * @return
     */
    private URI getDefinitiveUri(String sourceArtefact) {
        URI baseUri = new File(sourceArtefact).toURI();
        if (sourceArtefact.endsWith(File.separator) && !baseUri.toString().endsWith("/")) {
            try {
                baseUri = new URI(baseUri.toString() + "/");
            } catch (URISyntaxException e) {
                throw new FatalDeploymentException("Unexpected URI syntax problem.", e);
            }
        }
        return baseUri;
    }

    /**
     * Deploys the application at the supplied PathReference asynchronously.
     * 
     * @param sourceArtefact the source artefact URI string
     * @throws DeploymentException
     */
    private void deploy(String sourceArtefact) throws DeploymentException {
        this.deployer.deploy(getDefinitiveUri(sourceArtefact), new DeploymentOptions(true, true, false));
    }

    /**
     * Deploys the application at the supplied PathReference if it is not already deployed.
     * 
     * @param sourceArtefact the source artefact URI string
     * @param fileName the artefact file name
     * @throws DeploymentException
     */
    private void deployIfNotDeployed(String sourceArtefact, String fileName) throws DeploymentException {
        if (!isDeployed(sourceArtefact)) {
            deploy(sourceArtefact);
        } else {
            this.eventLogger.log(HotDeployerLogEvents.HOT_DEPLOY_SKIPPED, fileName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Hot Deploy Listener";
    }
}
