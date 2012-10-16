package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.apache.openejb.config.DynamicDeployer;

public class DynamicDeployerHolder {

	static DynamicDeployer dynamicDeployer = null;
	
	static DynamicDeployer getDynamicDeployer() {
		return dynamicDeployer;
	}
	
	public void bindDynamicDeployer(DynamicDeployer deployer) {
		dynamicDeployer = deployer;
	}

	public void unbindDynamicDeployer(DynamicDeployer deployer) {
		dynamicDeployer = null;
	}
}
