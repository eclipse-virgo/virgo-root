package org.eclipse.virgo.kernel.core;

import java.util.Dictionary;

/**
 * This Service exports the user region and kernel region configurations, so that 
 * they are available to bundles in the user region.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations should be threadsafe.
 */
public interface ConfigurationExporter {

	/**
	 * Retrieve user region configuration properties
	 */
	Dictionary getUserRegionConfigurationProperties();

	/**
	 * Retrieve kernel region configuration properties
	 */
	Dictionary getKernelRegionConfigurationProperties();

}