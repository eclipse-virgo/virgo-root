package org.eclipse.virgo.nano.config.internal;

import java.util.Dictionary;

import org.eclipse.virgo.nano.core.ConfigurationExporter;
import org.osgi.service.cm.Configuration;

/**
 * ConfigurationExporter 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * threadsafe
 *  
 */
public class StandardConfigurationExporter implements ConfigurationExporter {
	
	private Configuration userregionConfiguration;
	
	private Configuration kernelregionConfiguration;
	
	public StandardConfigurationExporter (Configuration userregionConfiguration, Configuration kernelregionConfiguraion) {
		this.userregionConfiguration = userregionConfiguration;
		this.kernelregionConfiguration = kernelregionConfiguraion;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.virgo.nano.config.internal.ConfigurationExporter#getUserregionConfiguration()
	 */
	@Override
	public Dictionary getUserRegionConfigurationProperties() {
		return userregionConfiguration.getProperties();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.virgo.nano.config.internal.ConfigurationExporter#getKernelregionConfiguration()
	 */
	@Override
	public Dictionary getKernelRegionConfigurationProperties() {
		return kernelregionConfiguration.getProperties();
	}
}
