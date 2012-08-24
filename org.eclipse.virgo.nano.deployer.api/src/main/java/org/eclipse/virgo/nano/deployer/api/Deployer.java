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

package org.eclipse.virgo.nano.deployer.api;

import javax.management.MXBean;
import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;


/**
 * Definition of the Deployer control used to allow the Server to support remote deployment.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 * 
 */
@MXBean
public interface Deployer {

    /**
     * Deploy an application located at the given URI.
     * 
     * @param uri the URI string of the location of the application
     * @return the {@link DeploymentIdentity} of the deployed application as a {@link CompositeData} object.
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(String uri) throws DeploymentException;

    /**
     * Deploy an application located at the given URI.
     * 
     * @param uri the URI string of the location of the application.
     * @param recoverable whether or not the application should be recovered on warm restart.
     * @return the {@link DeploymentIdentity} of the deployed application as a {@link CompositeData} object.
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(String uri, boolean recoverable) throws DeploymentException;

    /**
     * Undeploy an application with a given symbolic name and version.
     * 
     * @param applicationSymbolicName the symbolic name of the application
     * @param version the version of the application in string form
     * @throws DeploymentException
     */
    void undeploy(String applicationSymbolicName, String version) throws DeploymentException;

    /**
     * Refresh a single module of the application which was deployed from the given URI.
     * 
     * @param uri the URI string from which the application was deployed
     * @param symbolicName the bundle symbolic name of the module to be refreshed
     * @throws DeploymentException if the refresh failed
     */
    void refresh(String uri, String symbolicName) throws DeploymentException;

    /**
     * Refresh any bundle with the given symbolic name and version and any bundles cloned from a bundle with the given
     * symbolic name and version. If no bundles or cloned bundles match the given symbolic name and version, simply
     * return and do not throw an exception. <br>
     * <p/> Certain bundles which are critical to the operation of the system may not be refreshed. If an attempt is
     * made to refresh one of these bundles, a warning message is logged, the bundle is not refreshed, but any clones of
     * the bundle are refreshed.
     * 
     * @param bundleSymbolicName the symbolic name of the bundle
     * @param bundleVersion the version of the bundle
     * @throws DeploymentException
     */
    void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException;
    
    //  TODO: Implement install, start, stop and uninstall on Deployer
    //  The following methods are a draft for the new methods to allow install, start, stop, and
    //  uninstall to be driven separately over JMX, as opposed to deploy (install and start),
    //  and undeploy (stop and uninstall).
    //
    //  NOTE: These methods have not yet been implemented, calling them will result in an
    //  UnsupportedOperationException being thrown.
    //
    	
    /**
	 * Installs the artifact identified by the supplied <code>artifactUri</code>. The artifact
	 * will be recovered upon warm restart, i.e. equivalent to calling {@link #install(String, boolean)
	 * install(artifactUri, true)}. If the artifact is already present this method has no effect, and
	 * the identity of the existing artifact is returned.
	 * 
	 * @param artifactUri The uri, as a <code>String</code>, of the artifact to be installed.
	 * 
	 * @return The {@link ArtifactIdentity} of the installed artifact as {@link CompositeData}.
	 * 
	 * @throws DeploymentException if the install fails
	 */
	ArtifactIdentity install(String artifactUri) throws DeploymentException;
    
    /**
	 * Installs the artifact identified by the supplied <code>artifactUri</code>, telling the
	 * deployer whether or not the artifact should be recovered upon warm restart.  If the artifact is
	 * already present this method has no effect, and the identity of the existing artifact
	 * is returned.
	 * 
	 * @param artifactUri The uri, as a <code>String</code>, of the artifact to be installed.
	 * @param recover <code>true</code> if the artifact should be recovered, <code>false</code> if it should not.
	 * 
	 * @return The {@link ArtifactIdentity} of the installed artifact as {@link CompositeData}.
	 * 
	 * @throws DeploymentException if the install fails
	 */
	ArtifactIdentity install(String artifactUri, boolean recover) throws DeploymentException;
	
	/**
	 * Installs the artifact identified by the supplied <code>type</code>, <code>name</code>, and
	 * <code>version</code>. The artifact will be recovered upon warm restart, i.e. equivalent to calling
	 * {@link #install(String, String, String, boolean) install(type, name, version, true)}.  If the
	 * artifact is already present this method has no effect, and the identity of the existing
	 * artifact is returned.
	 * 
	 * @param artifactUri The uri, as a <code>String</code>, of the artifact to be installed.
	 * @param type The type of the artifact to be installed
	 * @param name The name of the artifact to be installed
	 * @param version The version of the artifact to be installed
	 * 
	 * @return The {@link ArtifactIdentity} of the installed artifact as {@link CompositeData}.
	 * 
	 * @throws DeploymentException if the install fails
	 */
	ArtifactIdentity install(String type, String name, String version) throws DeploymentException;
    
    /**
	 * Installs the artifact identified by the supplied <code>type</code>, <code>name</code>, and
	 * <code>version</code>, telling the deployer whether or not the artifact should be recovered
	 * upon warm restart. If the artifact is already present this method has no effect, and the
	 * identity of the existing artifact is returned.
	 * 
	 * @param type The type of the artifact to be installed
	 * @param name The name of the artifact to be installed
	 * @param version The version of the artifact to be installed
	 * @param recover <code>true</code> if the artifact should be recovered upon warm restart,
	 *        <code>false</code> if it should not.
	 *        
	 * @return The {@link ArtifactIdentity} of the installed artifact as {@link CompositeData}.
	 * 
	 * @throws DeploymentException if the install fails
	 */
	ArtifactIdentity install(String type, String name, String version, boolean recover) throws DeploymentException;
	
	/**
	 * Starts the artifact identified by the supplied <code>artifactIdentity</code>. If the artifact
	 * is already active, this method has no effect.
	 * 
	 * @param artifactIdentity The {@link ArtifactIdentity} of the artifact that is to be started.
	 * 
	 * @throws DeploymentException if the start fails
	 * @throws IllegalStateException If the identified artifact is not present
	 */
	void start(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException;
	
	/**
	 * Starts the artifact identified by the supplied <code>type</code>, <code>name</code>, and
	 * <code>version</code>. If the artifact is already active, this method has no effect.
	 * 
	 * @param type The type of the artifact to be started
	 * @param name The name of the artifact to be started
	 * @param version The version of the artifact to be started
	 * 
	 * @throws DeploymentException if the start fails
	 * @throws IllegalStateException If the artifact is not present
	 */
	void start(String type, String name, String version) throws DeploymentException, IllegalStateException;
		
	/**
	 * Stops the artifact identified by the supplied <code>artifactIdentity</code>. If the artifact is
	 * not active, this method has no effect.
	 * 
	 * @param artifactIdentity The {@link ArtifactIdentity} of the artifact that is to be started.
	 * 
	 * @throws DeploymentException if the stop fails
	 * @throws IllegalStateException If the artifact is not present
	 */
	void stop(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException;
	
	/**
	 * Stops the artifact identified by the supplied <code>type</code>, <code>name</code>, and
	 * <code>version</code>. If the artifact is not active, this method has no effect.
	 * 
	 * @param type The type of the artifact to be stopped
	 * @param name The name of the artifact to be stopped
	 * @param version The version of the artifact to be stopped
	 * 
	 * @throws DeploymentException if the stop fails
	 * @throws IllegalStateException If the identified artifact is not present
	 */
	void stop(String type, String name, String version) throws DeploymentException, IllegalStateException;
	
	/**
	 * Uninstalls the artifact identified by the supplied <code>artifactIdentity</code>. If
	 * the artifact is not present, this method has no effect. If the artifact is active an
	 * {@link IllegalStateException} is thrown, i.e. an active artifact cannot be uninstalled.
	 * 
	 * @param artifactIdentity The {@link ArtifactIdentity} of the artifact that is to be uninstalled.
	 * 
	 * @throws DeploymentException if the uninstall fails
	 * @throws IllegalStateException if the artifact is present and is active
	 */
	void uninstall(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException;
	
	/**
	 * Uninstalls the artifact identified by the supplied <code>type</code>, <code>name</code>,
	 * and <code>version</code>. If the artifact is not present, this method has no effect. If the
	 * artifact is active an {@link IllegalStateException} is thrown, i.e. an active artifact cannot
	 * be uninstalled.
	 * 
	 * @param type The type of the artifact to be uninstalled
	 * @param name The name of the artifact to be uninstalled
	 * @param version The version of the artifact to be uninstalled
	 * 
	 * @throws DeploymentException if the uninstall fails
	 * @throws IllegalStateException if the artifact is present and is started
	 */
	void uninstall(String type, String name, String version) throws DeploymentException, IllegalStateException;
}
