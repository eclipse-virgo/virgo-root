package org.eclipse.virgo.nano.deployer.api.config;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.virgo.nano.serviceability.NonNull;

/**
 * Publishes and Deletes configurations from the config admin that is present in the implementor's region
 *
 *
 */
public interface ConfigurationDeployer {
	
	/**
	 * Publishes configuration with the specified pid and configuration properties in the implementor's config admin
	 * @param pid - the pid of the published configuration
	 * @param configurationProperties
	 * @throws IOException - if the operation isn't successful
	 */
	void publishConfiguration(@NonNull String pid, @NonNull Properties configurationProperties) throws IOException;
	
	/**
	 * Deletes the configuration with the specified pid in the implementor's config admin
	 * @param pid - the pid of the configuration to delete
	 * @throws IOException - if the operation isn't successful
	 */
	void deleteConfiguration(@NonNull String pid) throws IOException;
	
	/**
	 * Gets the configuration with the specified pid in the implementor's config admin
	 * @param pid - the pid of the published configuration
	 * @return the configuration properties or null if the pid is not associated with any properties
	 * @throws IOException - if the operations isn't successful
	 */
	Properties getConfiguration(@NonNull String pid) throws IOException;
}
