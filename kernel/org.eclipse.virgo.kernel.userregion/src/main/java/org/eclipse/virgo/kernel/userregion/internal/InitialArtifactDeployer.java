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

package org.eclipse.virgo.kernel.userregion.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;


import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.StringUtils;


/**
 * <code>InitialArtifactDeployer</code> is responsible for deploying the configured
 * set of initial artifacts.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class InitialArtifactDeployer implements EventHandler {
    
    private static final String THREAD_NAME_SYSTEM_ARTIFACTS = "system-artifacts";

	private static final String THREAD_NAME_USER_ARTIFACTS = "user-artifacts";

	private static final DeploymentOptions ARTIFACT_DEPLOYMENT_OPTIONS = new DeploymentOptions(false, false, true);
    
    private static final String TOPIC_SYSTEM_ARTIFACTS_DEPLOYED = "org/eclipse/virgo/kernel/userregion/systemartifacts/DEPLOYED";
    
    private static final String TOPIC_USER_ARTIFACTS_DEPLOYED = "org/eclipse/virgo/kernel/userregion/userartifacts/DEPLOYED";    
    
    private final ApplicationDeployer deployer;
    
    private final DeployUriNormaliser uriNormaliser;
    
    private final EventAdmin eventAdmin;
    
    private final String systemArtifactsProperty;
    
    private final String userArtifactsProperty;
    
    private final EventLogger eventLogger;
    
    private final Shutdown shutdown;
    
    private final KernelStartedAwaiter startAwaiter;

    InitialArtifactDeployer(KernelStartedAwaiter startAwaiter, ApplicationDeployer deployer, Object systemArtifactsProperty, Object userArtifactsProperty, DeployUriNormaliser uriNormaliser, EventAdmin eventAdmin, EventLogger eventLogger, Shutdown shutdown) {
        this.deployer = deployer;
        this.uriNormaliser = uriNormaliser;
        this.eventAdmin = eventAdmin;
        if (systemArtifactsProperty != null) {
        	this.systemArtifactsProperty = systemArtifactsProperty.toString();
        } else {
        	this.systemArtifactsProperty = null;
        }
        if (userArtifactsProperty != null) {
        	this.userArtifactsProperty = userArtifactsProperty.toString();
        } else {
        	this.userArtifactsProperty = null;
        }
        this.eventLogger = eventLogger;
        this.shutdown = shutdown;
        this.startAwaiter = startAwaiter;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
    	if (TOPIC_SYSTEM_ARTIFACTS_DEPLOYED.equals(event.getTopic())) {
    	    this.eventLogger.log(UserRegionLogEvents.SYSTEM_ARTIFACTS_DEPLOYED);
    		deployUserArtifacts();
    	}
    }
    
    void deployArtifacts() throws InterruptedException {
        
        this.startAwaiter.awaitKernelStarted();
        
    	List<URI> systemArtifacts = getSystemArtifacts();
    	deployArtifacts(systemArtifacts, THREAD_NAME_SYSTEM_ARTIFACTS, TOPIC_SYSTEM_ARTIFACTS_DEPLOYED);    	
    }
    
    private void deployUserArtifacts(){
    	List<URI> userArtifacts = getUserArtifacts();
    	deployArtifacts(userArtifacts, THREAD_NAME_USER_ARTIFACTS, TOPIC_USER_ARTIFACTS_DEPLOYED);
    }
    
    private List<URI> getUserArtifacts() {    	
    	return getRepositoryUrisForArtifacts(this.userArtifactsProperty);
    }
    
    private List<URI> getSystemArtifacts() {
    	return getRepositoryUrisForArtifacts(this.systemArtifactsProperty);
    }
    
    private List<URI> getRepositoryUrisForArtifacts(String artifactsProperty) {
    	String[] artifacts = StringUtils.commaDelimitedListToStringArray(artifactsProperty);
        
        List<URI> repositoryUris = new ArrayList<URI>();
        
        for (String artifact : artifacts) {
            repositoryUris.add(URI.create(artifact.trim()));
        }
        
        return repositoryUris;
    }

    private void deployArtifacts(List<URI> artifacts, String threadName, String completionEventTopic) {
    	Runnable artifactDeployingRunnable = new ArtifactDeployingRunnable(artifacts, completionEventTopic);    	
        Thread deployThread = new Thread(artifactDeployingRunnable, threadName);
        deployThread.start();
    }
    
    private final class ArtifactDeployingRunnable implements Runnable {
    	
    	private final List<URI> artifacts;
    	
    	private final String completionEventTopic;
    	
    	private ArtifactDeployingRunnable(List<URI> artifacts, String completionEventTopic) {
    		this.artifacts = artifacts;
    		this.completionEventTopic = completionEventTopic;
    	}
    	
		public void run() {
			try {
				validateArtifacts();      
				deployArtifacts();
				eventAdmin.postEvent(new Event(this.completionEventTopic, (Map<String, ?>)null));
			} catch (DeploymentException de) {
				eventLogger.log(UserRegionLogEvents.INITIAL_ARTIFACT_DEPLOYMENT_FAILED);
				shutdown.shutdown();
			}
		}
		
		private void deployArtifacts() throws DeploymentException {
	        for (URI artifact : this.artifacts) {
	            deployer.deploy(artifact, ARTIFACT_DEPLOYMENT_OPTIONS);
	        }
	    }
		
	    private void validateArtifacts() throws DeploymentException {
	        boolean normaliseFailed = false;
	        
	        for (URI uri : this.artifacts) {
	            try {
	                uriNormaliser.normalise(uri);
	            } catch (DeploymentException de) {
	                normaliseFailed = true;
	            }
	        }
	        
	        if (normaliseFailed) {
	            throw new DeploymentException("Validation of artifacts failed");
	        }
	    }
    }
}
