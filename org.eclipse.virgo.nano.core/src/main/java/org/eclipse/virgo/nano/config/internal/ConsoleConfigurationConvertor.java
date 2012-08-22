
package org.eclipse.virgo.nano.config.internal;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Dictionary;
import java.util.Hashtable;

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
    
    private static final String TELNET_SERVICE = "telnet";
    
    private static final String SSH_SERVICE = "ssh";
    
    private static final Object monitor = new Object();

    ConsoleConfigurationConvertor(BundleContext context, ConfigurationAdmin configAdmin) {
        this.context = context;
        this.configAdmin = configAdmin;
    }

    public void start() {
        Dictionary<String, String> consoleProperties = new Hashtable<String, String>();
        consoleProperties.put(Constants.SERVICE_PID, CONSOLE_PID);
        synchronized (ConsoleConfigurationConvertor.monitor) {
            this.configuratorRegistration = this.context.registerService(ManagedService.class, new ConsoleConfigurator(), consoleProperties);
        }
    }

    private void updateConfiguration(String pid, String host, String port, String enabled) {
    	boolean isPortAvailable;
    	if (pid.contains(TELNET_SERVICE)) {
    		isPortAvailable = checkPortAvailability(port, enabled, TELNET_SERVICE);
    	} else {
    		isPortAvailable = checkPortAvailability(port, enabled, SSH_SERVICE);
    	}
    	
    	if(!isPortAvailable) {
    		return;
    	}
    	
        try {
            Configuration configuration = this.configAdmin.getConfiguration(pid, null);
            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put(HOST, host);
            properties.put(PORT, port);
            properties.put(ENABLED, enabled);
            configuration.update(properties);
        } catch (IOException e) {
            String message = String.format("Unable to update configuration with pid '%s'", pid);
            this.logger.error(message, e);
        }
    }
    
    public void stop() {
    	deleteConfiguration(TELNET_PID);
    	deleteConfiguration(SSH_PID);
    	deleteConfiguration(CONSOLE_PID);
    }

    private void deleteConfiguration(String pid) {
        try {
  			Configuration configuration = configAdmin.getConfiguration(pid, null);
			configuration.delete();
    	} catch (IOException e) {
			String message = String.format("Unable to delete configuration with pid: " + pid);
            this.logger.error(message, e);
		}
    }
    
    private boolean checkPortAvailability(String portStr, String enabled, String service) {
    	if ("false".equalsIgnoreCase(enabled)) {
    		return true;
    	}
    	int port = Integer.parseInt(portStr);
    	ServerSocket socket = null;
    	try {
    		socket = new ServerSocket(port);
    		return true;
    	} catch (BindException e) {
    		String message = "Port " + port + " already in use; " + service + " access to console will not be available";
    		this.logger.error(message, e);
    	} catch (IOException e) {
			// do nothing
		} finally {
    		if (socket != null) {
    			try {
					socket.close();
				} catch (IOException e) {
					// do nothing
				}
    		}
    	}
    	return false;
    }

    class ConsoleConfigurator implements ManagedService {

        private Dictionary<String,String> properties;

        @SuppressWarnings("unchecked")
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

            String telnetHost = this.properties.get(TELNET_HOST);
            String telnetPort = this.properties.get(TELNET_PORT);
            String telnetEnabled = this.properties.get(TELNET_ENABLED);
            updateConfiguration(TELNET_PID, telnetHost, telnetPort, telnetEnabled);

            String sshHost = this.properties.get(SSH_HOST);
            String sshPort = this.properties.get(SSH_PORT);
            String sshEnabled = this.properties.get(SSH_ENABLED);
            updateConfiguration(SSH_PID, sshHost, sshPort, sshEnabled);
        }
    }
}