package org.eclipse.virgo.nano.deployer.api.core;

import java.net.URI;

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
     * {@link org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer#deploy(URI, DeploymentOptions) deploy}
     * method until any application contexts for the application have been built and published in the service
     * registry or deployment fails or times out.
     * <p/>
     * Deploying asynchronously means that control returns to the caller of the
     * {@link org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer#deploy(URI, DeploymentOptions) deploy}
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