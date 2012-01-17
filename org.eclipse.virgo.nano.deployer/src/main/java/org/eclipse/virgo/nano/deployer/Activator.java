package org.eclipse.virgo.nano.deployer;

import java.io.File;

import org.eclipse.virgo.kernel.core.KernelConfig;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeployerConfiguration;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.hot.HotDeployerEnabler;
import org.eclipse.virgo.nano.deployer.internal.StandardApplicationDeployer;
import org.eclipse.virgo.nano.deployer.internal.StandardDeployerConfiguration;
import org.osgi.service.component.ComponentContext;

public class Activator {
    
    private EventLogger eventLogger;
    private KernelConfig kernelConfig;
    private HotDeployerEnabler hotDeployerEnabler = null;

    public void activate(ComponentContext context) throws Exception {
        ApplicationDeployer appDeployer = new StandardApplicationDeployer(context.getBundleContext(), this.eventLogger);
        
        initialiseHotDeployer(appDeployer);
        
        //TODO register the deployer MBean when the management classes are factored out in a new bundle.
        //Deployer deployerMBean = new StandardDeployer(appDeployer);
    }

    private void initialiseHotDeployer(ApplicationDeployer appDeployer) {
        int deployerTimeout = Integer.valueOf(this.kernelConfig.getProperty("deployer.timeout"));
        String pickupDirectory = this.kernelConfig.getProperty("deployer.pickupDirectory");
        DeployerConfiguration deployerConfiguration = new StandardDeployerConfiguration(deployerTimeout, new File(pickupDirectory));
        this.hotDeployerEnabler = new HotDeployerEnabler(appDeployer, deployerConfiguration, this.eventLogger);
        this.hotDeployerEnabler.startHotDeployer();
    }

    public void deactivate(ComponentContext context) throws Exception {
        if (this.hotDeployerEnabler != null) {
            this.hotDeployerEnabler.stopHotDeployer();
        }
    }
    
    public void bindEventLogger(EventLogger logger) {
        this.eventLogger = logger;
    }
    
    public void unbindEventLogger(EventLogger logger) {
        this.eventLogger = null;
    }
    
    public void bindKernelConfig(KernelConfig config) {
        this.kernelConfig = config;
    }
    
    public void unbindKernelConfig(KernelConfig config) {
        this.kernelConfig = null;
    }

}
