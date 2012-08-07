package org.eclipse.virgo.nano.deployer.support;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.hot.HotDeployer;
import org.eclipse.virgo.medic.eventlog.EventLogger;

public class HotDeployerEnabler {

    private final ApplicationDeployer appDeployer;
    private final EventLogger eventLogger;
    private final DeployerConfiguration deployerConfiguration;
    private HotDeployer hotDeployer = null;
    
    public HotDeployerEnabler(ApplicationDeployer appDeployer, DeployerConfiguration deployerConfiguration, EventLogger eventLogger) {
        this.appDeployer = appDeployer;
        this.eventLogger = eventLogger;
        this.deployerConfiguration = deployerConfiguration;
    }
    
    public void startHotDeployer() {
        this.hotDeployer = new HotDeployer(this.deployerConfiguration, this.appDeployer, this.eventLogger);
        this.hotDeployer.doStart();
    }
    
    public void stopHotDeployer() {
        if (this.hotDeployer != null) {
            this.hotDeployer.doStop();
        }
    }

}
