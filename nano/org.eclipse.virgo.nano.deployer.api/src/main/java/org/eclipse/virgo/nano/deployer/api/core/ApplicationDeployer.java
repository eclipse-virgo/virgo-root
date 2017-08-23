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

package org.eclipse.virgo.nano.deployer.api.core;

import java.net.URI;
import java.util.List;

import org.osgi.framework.Version;

/**
 * The ApplicationDeployer interface is the programmatic interface to the deployer subsystem for deploying applications
 * and libraries.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ApplicationDeployer {

    /**
     * Deploys the artifact are the supplied <code>location</code>. The supplied <code>options</code> govern how the
     * installed artifact is handled by the deployed, e.g. if it is recovered and re-installed upon warm restart.
     * 
     * @param uri The location of the artifact
     * @param options The options for the installation
     * @return The identity of the installed artifact
     * @throws DeploymentException if installation fails.
     */
    DeploymentIdentity install(URI uri, DeploymentOptions options) throws DeploymentException;

    /**
     * Deploys the artifact are the supplied <code>location</code>. The supplied <code>options</code> govern how the
     * installed artifact is handled by the deployed, e.g. if it is recovered and re-installed upon warm restart.
     * <p />
     * This method is equivalent to calling <code>install(location, new DeploymentOptions())</code>.
     * 
     * @param uri The location of the artifact
     * @param options The options for the installation
     * @return The identity of the installed artifact
     * @throws DeploymentException if installation fails.
     */
    DeploymentIdentity install(URI uri) throws DeploymentException;

    /**
     * Deploy an application which may be either an OSGi application or a legacy application such as a WAR. This is used
     * by admin. and hot deployment. A successfully deployed application is always recovered on warm restart but the
     * application at the given location is not deleted when the application is undeployed. <br/>
     * <p />
     * This method is equivalent to calling <code>deploy(location, new DeploymentOptions())</code>.
     * 
     * @param uri the location of the application JAR file
     * @return the {@link DeploymentIdentity} of the deployed application
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(URI uri) throws DeploymentException;

    /**
     * Deploys a bunch of deployable artifacts altogether so that if there are dependencies in between,they are resolved
     * successfully. Note that this may not always be the case if they are deployed one by one through some of the other
     * deploy methods. Therefore the method is convenient to solve dependency resolution issues that are caused due to
     * the order in which deployable bundles are deployed.
     * 
     * @param uris the location paths of the deployable artifacts
     * @param options the options for this deployment
     * @return an array of the successfully deployed applications's {@link DeploymentIdentity}.
     * @throws DeploymentException
     */
    DeploymentIdentity[] bulkDeploy(List<URI> uris, DeploymentOptions options) throws DeploymentException;

    /**
     * Deploy an application which may be either an OSGi application or a legacy application such as a WAR. This is used
     * by admin. and hot deployment.
     * 
     * @param uri location of the artifact
     * @param options the options for this deployment
     * @return the {@link DeploymentIdentity} of the deployed application
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(URI uri, DeploymentOptions options) throws DeploymentException;

    /**
     * Deploy an artifact from the repository with the given type, name, and version. A successfully deployed
     * application is always recovered on warm restart. <br/>
     * <p />
     * This method is equivalent to calling <code>deploy(type, name, version, new DeploymentOptions())</code>.
     * 
     * @param type the type of the artifact to deploy
     * @param name the name of the artifact to deploy
     * @param version the {@link Version} of the artifact to deploy
     * @return the {@link DeploymentIdentity} of the deployed application
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(String type, String name, Version version) throws DeploymentException;

    /**
     * Deploy an artifact from the repository with the given type, name, and version.<br/>
     * <p />
     * If the given {@link DeploymentOptions} specify the artifact is owned by the deployer, then deployment fails and a
     * deployment exception is thrown. This is to avoid the deployer deleting the artifact from the repository store.
     * 
     * @param type the type of the artifact to deploy
     * @param name the name of the artifact to deploy
     * @param version the {@link Version} of the artifact to deploy
     * @param options of the deployment
     * @return the {@link DeploymentIdentity} of the deployed application which must return <code>false</code> from
     *         <code>getDeployerOwned</code>
     * @throws DeploymentException
     */
    DeploymentIdentity deploy(String type, String name, Version version, DeploymentOptions options) throws DeploymentException;

    /**
     * Undeploy an application with a given symbolic name and version.
     * 
     * WARNING: there can be ambiguity if applications of distinct types have the same symbolic name and version, so
     * this method is deprecated.
     * 
     * @param applicationSymbolicName the symbolic name of the application
     * @param version the version of the application in string form
     * @throws DeploymentException
     * @deprecated in favour of the undeploy(String, String, String) which takes artifact type as first parameter
     */
    @Deprecated
    void undeploy(String applicationSymbolicName, String version) throws DeploymentException;

    /**
     * Undeploy an application with a given type, name, and version.
     * 
     * @param type the type of the application
     * @param name the symbolic name of the application
     * @param version the version of the application in string form
     * @throws DeploymentException
     */
    void undeploy(String type, String name, String version) throws DeploymentException;

    /**
     * Undeploy an application with a given {@link DeploymentIdentity}. The application may be either an OSGi
     * application or a legacy application such as a WAR.
     * <p>
     * This method is equivalent to calling <code>undeploy(DeploymentIdentity, boolean)</code> with <code>false</code>.
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the application
     * @throws DeploymentException
     */
    void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException;

    /**
     * Undeploy an application with a given {@link DeploymentIdentity}. The application may be either an OSGi
     * application or a legacy application such as a WAR.
     * <p>
     * The deleted parameter indicates whether the undeployment is a consequence of the artifact having been deleted.
     * This affects the processing of "deployer owned" artifacts which undeploy would normally delete automatically. If
     * the undeploy is a consequence of the artifact having been deleted, then undeploy must not delete the artifact
     * automatically since this may actually delete a "new" artifact which has arrived shortly after the "old" artifact
     * was deleted.
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the application
     * @param deleted <code>true</code> if and only if undeploy is being driven as a consequence of the artifact having
     *        been deleted
     * @throws DeploymentException
     */
    void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) throws DeploymentException;

    /**
     * Refresh a single bundle of the application (PAR or bundle) which was deployed from the given URI and return the
     * deployment identity of the deployed application. <br/>
     * If the refresh is promoted to a redeploy then the DeploymentIdentity of the newly deployed application is
     * returned.
     * 
     * @param uri location of the artifact
     * @param symbolicName the bundle symbolic name of the bundle to be refreshed
     * @return the {@link DeploymentIdentity} of the deployed application
     * @throws DeploymentException
     */
    DeploymentIdentity refresh(URI uri, String symbolicName) throws DeploymentException;

    /**
     * Refresh any bundle with the given symbolic name and version and any bundles cloned from a bundle with the given
     * symbolic name and version. If no bundles or cloned bundles match the given symbolic name and version, simply
     * return and do not throw an exception. <br/>
     * <p/>
     * Certain bundles which are critical to the operation of the system may not be refreshed. If an attempt is made to
     * refresh one of these bundles, a warning message is logged, the bundle is not refreshed, but any clones of the
     * bundle are refreshed.
     * 
     * @param bundleSymbolicName the symbolic name of the bundle
     * @param bundleVersion the version of the bundle
     * @throws DeploymentException
     */
    void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException;

    /**
     * Get a list of deployed applications.
     * 
     * @return Array of deployed application identities.
     */
    DeploymentIdentity[] getDeploymentIdentities();

    /**
     * Get the {@link DeploymentIdentity} of an application deployed from the given location. If no such application was
     * found, return <code>null</code>. <br/>
     * <p />
     * Although the deployer currently prevents two applications being deployed from the same location without
     * un-deploying the first application, this restriction may be lifted in the future in which case this method will
     * return one of potentially many applications deployed from the given location.
     * 
     * @param uri location of the artifact
     * @return a <code>DeploymentIdentity</code> or <code>null</code>
     */
    DeploymentIdentity getDeploymentIdentity(URI uri);

    /**
     * Determine whether or not the given artifact is already deployed. Return <code>true</code> if the given artefact
     * at its file's last modified time is already deployed. Returns <code>false</false> in case 
     * 1. The artefact is not deployed at all. 
     * 2. There was an offline update of the deployed artefact.
     * The last modified time is only taken into account if the file was deployed on a previous run of the Server.
     * 
     * @param uri location of the artifact
     * @return <code>true</code> if and only if the given artifact at its file's last modified time is already deployed
     */
    boolean isDeployed(URI uri);
}
