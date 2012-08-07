package org.eclipse.virgo.nano.deployer;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.osgi.framework.Bundle;


public interface SimpleDeployer {
    
    public final int HOT_DEPLOYED_ARTIFACTS_START_LEVEL = 5;

    public boolean deploy(URI path);
    
    public boolean update(URI path);
    
    public boolean undeploy(Bundle bundle);
    
    public boolean canServeFileType(String fileType);
    
    public boolean isDeployed(URI path);
    
    public DeploymentIdentity getDeploymentIdentity(URI path);
    
    public List<String> getAcceptedFileTypes();
    
    public boolean isDeployFileValid(File file);
    
}
