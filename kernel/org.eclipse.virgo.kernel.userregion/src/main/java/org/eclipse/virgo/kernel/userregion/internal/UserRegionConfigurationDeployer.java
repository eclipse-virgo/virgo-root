
package org.eclipse.virgo.kernel.userregion.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.virgo.nano.deployer.api.config.ConfigurationDeployer;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * This service is registered in the user region so that it has access to the configuration admin in the user region.
 * The kernel region can use it as a proxy to access the configuration admin in the user region.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
public class UserRegionConfigurationDeployer implements ConfigurationDeployer {

    private ConfigurationAdmin configurationAdmin;

    private Object monitor = new Object();

    public UserRegionConfigurationDeployer(BundleContext context) {
        this.configurationAdmin = OsgiFrameworkUtils.getService(context, ConfigurationAdmin.class).getService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishConfiguration(String pid, Properties configurationProperties) throws IOException {
        synchronized (monitor) {
            Configuration configuration = this.configurationAdmin.getConfiguration(pid, null);
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            for (String prop : configurationProperties.stringPropertyNames()) {
            	properties.put(prop, configurationProperties.get(prop));
            }
            configuration.update(properties);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteConfiguration(String pid) throws IOException {
        synchronized (monitor) {
            Configuration configuration = this.configurationAdmin.getConfiguration(pid, null);
            configuration.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getConfiguration(String pid) throws IOException {
        synchronized (monitor) {
            Configuration configuration = this.configurationAdmin.getConfiguration(pid, null);
            if (configuration != null) {
                Properties properties = new Properties();
                Dictionary<?, ?> props = configuration.getProperties();
                Enumeration<?> keys = props.keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = props.get(key);
                    properties.put(key, value);
                }
                return properties;
            } else {
                return null;
            }
        }
    }

}
