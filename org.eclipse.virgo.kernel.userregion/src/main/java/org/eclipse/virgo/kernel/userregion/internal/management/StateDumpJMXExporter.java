package org.eclipse.virgo.kernel.userregion.internal.management;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 */
public class StateDumpJMXExporter {
    private final Logger logger = LoggerFactory.getLogger(StateDumpJMXExporter.class);

    private static final String DOMAIN = "org.eclipse.virgo.kernel";
    
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

	private ObjectInstance registeredMBean;

    /**
     * 
     * @param serverHome
     */
	public StateDumpJMXExporter(QuasiFrameworkFactory quasiFrameworkFactory) {
		try {
			ObjectName dumpMBeanName = new ObjectName(String.format("%s:type=Medic,name=StateDumpInspector", DOMAIN));
			registeredMBean = this.server.registerMBean(new QuasiStateDump(quasiFrameworkFactory), dumpMBeanName);
		} catch (Exception e) {
			logger.error("Unable to register the DumpInspectorMBean", e);
		} 
	}
	
	/**
	 * 
	 */
	public void close(){
		ObjectInstance localRegisteredMBean = this.registeredMBean;
		if(localRegisteredMBean != null){
			try {
				this.server.unregisterMBean(localRegisteredMBean.getObjectName());
				this.registeredMBean = null;
			} catch (Exception e) {
				logger.error("Unable to unregister MBean", e);
			} 
		}
	}
}
