package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.config.AppModule;


public interface ResourceOperator {
    public void processResources(AppModule appModule, StandardContext standardContext);
}
