
package org.eclipse.virgo.kernel.userregion.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class reads the merged shell configuration and registers it separated in the @link(ConfigurationAdmin).
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
public class ConsoleConfigurationConvertor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext context;

    private final ConfigurationAdmin configAdmin;

    private ServiceRegistration<ManagedService> configuratorRegistration;

    private static final String CONSOLE_PID = "osgi.console";

    private static final String TELNET_PID = "osgi.console.telnet";

    private static final String SSH_PID = "osgi.console.ssh";

    private static final String TELNET_PORT = "telnet.port";

    private static final String TELNET_HOST = "telnet.host";

    private static final String TELNET_ENABLED = "telnet.enabled";

    private static final String SSH_PORT = "ssh.port";

    private static final String SSH_HOST = "ssh.host";

    private static final String SSH_ENABLED = "ssh.enabled";

    private static final String HOST = "host";

    private static final String PORT = "port";

    private static final String ENABLED = "enabled";

    private static final Object monitor = new Object();

    ConsoleConfigurationConvertor(BundleContext context) {
        this.context = context;
        this.configAdmin = OsgiFrameworkUtils.getService(context, ConfigurationAdmin.class).getService();
    }

    public void start() {
        Dictionary<String, String> consoleProperties = new Hashtable<String, String>();
        consoleProperties.put(Constants.SERVICE_PID, CONSOLE_PID);
        synchronized (ConsoleConfigurationConvertor.monitor) {
            this.configuratorRegistration = this.context.registerService(ManagedService.class, new ConsoleConfigurator(), consoleProperties);
        }
    }

    private void updateConfiguration(String pid, String host, String port, String enabled) {
        try {
            Configuration configuration = this.configAdmin.getConfiguration(pid, null);
            Properties properties = new Properties();
            properties.put(HOST, host);
            properties.put(PORT, port);
            properties.put(ENABLED, enabled);
            configuration.update(properties);
        } catch (IOException e) {
            String message = String.format("Unable to update configuration with pid '%s'", pid);
            this.logger.error(message);
            this.logger.trace(message, e);
        }
    }

    class ConsoleConfigurator implements ManagedService {

        private Dictionary properties;

        @Override
        public void updated(Dictionary props) throws ConfigurationException {
            if (props != null) {
                this.properties = props;
                this.properties.put(Constants.SERVICE_PID, CONSOLE_PID);
            } else {
                return;
            }

            synchronized (ConsoleConfigurationConvertor.monitor) {
                ConsoleConfigurationConvertor.this.configuratorRegistration.setProperties(this.properties);
            }

            String telnetHost = (String) this.properties.get(TELNET_HOST);
            String telnetPort = (String) this.properties.get(TELNET_PORT);
            String telnetEnabled = (String) this.properties.get(TELNET_ENABLED);
            updateConfiguration(TELNET_PID, telnetHost, telnetPort, telnetEnabled);

            String sshHost = (String) this.properties.get(SSH_HOST);
            String sshPort = (String) this.properties.get(SSH_PORT);
            String sshEnabled = (String) this.properties.get(SSH_ENABLED);
            updateConfiguration(SSH_PID, sshHost, sshPort, sshEnabled);
        }
    }
}