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

package org.eclipse.virgo.kernel.deployer.core;

import java.net.URI;

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
     * {@link DeploymentOptions} provides a collection of deployment options.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * This class is immutable and therefore thread safe.
     */
    public class DeploymentOptions {

        public static final DeploymentOptions DEFAULT_DEPLOYMENT_OPTIONS = new DeploymentOptions();

        private final boolean recoverable;

        private final boolean deployerOwned;

        private final boolean synchronous;

        /**
         * Create default deployment options.
         */
        public DeploymentOptions() {
            this.recoverable = true;
            this.deployerOwned = false;
            this.synchronous = true;
        }

        /**
         * Create deployment options with the given recoverability, ownership, and synchronisation.
         * 
         * @param recoverable <code>true</code> if and only if the application is to persist across Server restarts
         * @param deployerOwned <code>true</code> if and only if the application at the location specified on deployment
         *        is to be deleted when the application is undeployed
         * @param synchronous <code>true</code> if and only if the application should be deployed synchronously
         */
        public DeploymentOptions(boolean recoverable, boolean deployerOwned, boolean synchronous) {
            this.recoverable = recoverable;
            this.deployerOwned = deployerOwned;
            this.synchronous = synchronous;
        }

        /**
         * Get the recoverability option.
         * 
         * @return <code>true</code> if and only if the application is to persist across Server restarts
         */
        public boolean getRecoverable() {
            return this.recoverable;
        }

        /**
         * Get the ownership option.
         * 
         * @return <code>true</code> if and only if the application at the location specified on deployment is to be
         *         deleted when the application is undeployed
         */
        public boolean getDeployerOwned() {
            return this.deployerOwned;
        }

        /**
         * Get the synchronisation option which is <code>true</code> if and only if the application should be deployed
         * synchronously.
         * <p/>
         * Deploying synchronously means that control does not return to the caller of the
         * {@link org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer#deploy(URI, DeploymentOptions) deploy}
         * method until any application contexts for the application have been built and published in the service
         * registry or deployment fails or times out.
         * <p/>
         * Deploying asynchronously means that control returns to the caller of the
         * {@link org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer#deploy(URI, DeploymentOptions) deploy}
         * method once the application has been started, but not necessarily before any application contexts have been
         * built and published.
         * 
         * @return <code>true</code> if and only if the application should be deployed synchronously
         */
        public boolean getSynchronous() {
            return this.synchronous;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (deployerOwned ? 1231 : 1237);
            result = prime * result + (recoverable ? 1231 : 1237);
            result = prime * result + (synchronous ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DeploymentOptions other = (DeploymentOptions) obj;
            if (deployerOwned != other.deployerOwned)
                return false;
            if (recoverable != other.recoverable)
                return false;
            if (synchronous != other.synchronous)
                return false;
            return true;
        }
    }

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
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the application
     * @throws DeploymentException
     */
    void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException;

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
     * at its file's last modified time is already deployed. The last modified time is only taken into account if the
     * file was deployed on a previous run of the Server.
     * 
     * @param uri location of the artifact
     * @return <code>true</code> if and only if the given artifact at its file's last modified time is already deployed
     */
    boolean isDeployed(URI uri);

}
