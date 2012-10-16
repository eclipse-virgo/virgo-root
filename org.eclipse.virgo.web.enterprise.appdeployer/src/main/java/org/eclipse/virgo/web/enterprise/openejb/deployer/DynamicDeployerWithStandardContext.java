package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.config.DynamicDeployer;

public interface DynamicDeployerWithStandardContext extends DynamicDeployer {
	public void setStandardContext(StandardContext standardContext);
}
