package org.eclipse.virgo.nano.deployer;

import java.net.URI;

import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.osgi.framework.Bundle;


public interface SimpleDeployer {
    
    public final int HOT_DEPLOYED_ARTIFACTS_START_LEVEL = 5;

    public boolean deploy(URI path);
    
    public boolean update(URI path);
    
    public boolean undeploy(Bundle bundle);
    
    public boolean canServeFileType(String fileType);
    
    public boolean isDeployed(URI path);
    
    public DeploymentIdentity getDeploymentIdentity(URI path);
    
}
