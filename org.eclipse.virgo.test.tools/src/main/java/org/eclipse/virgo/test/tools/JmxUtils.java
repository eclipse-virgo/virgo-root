
package org.eclipse.virgo.test.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.util.io.NetUtils;

// TODO - find a better name
// TODO - rework static usage of virgoHome
public class JmxUtils {

    public static final Duration HALF_SECOND = Duration.ofMillis(500);

    public static final Duration TEN_SECONDS = Duration.ofSeconds(10);

    public static final Duration THIRTY_SECONDS = Duration.ofSeconds(30);

    public static final Duration TWO_MINUTES = Duration.ofMinutes(2);

    private static final String ARTIFACT_BUNDLE = "bundle";

    private static MBeanServerConnection connection = null;

    public static File virgoHome;

    private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";

    private static final String KEYSTORE = "/configuration/keystore";

    private static final String KEYPASSWORD = "changeit";

    private static final String DEPLOYER_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=Deployer";

    private static final String[] DEPLOYMENT_IDENTITY_FIELDS = new String[] { "type", "symbolicName", "version" };

    public final static String STATUS_STARTED = "STARTED";

    public final static String STATUS_STARTING = "STARTING";

    public static void waitForVirgoServerStartFully() {
        waitForVirgoServerStartFully(THIRTY_SECONDS, HALF_SECOND);
    }

    private static void waitForVirgoServerStartFully(Duration duration, Duration interval) {
        Instant start = Instant.now();
        while (start.plus(duration).isAfter(Instant.now())) {
            try {
                if (getKernelStatus().equals(STATUS_STARTED)) {
                    return;
                } else if (getKernelStatus().equals(STATUS_STARTING)) {
                    continue;
                }
            } catch (Exception e) {
                // ignore and try again
            }
            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        // allow some more time to finish startup
        try {
            Thread.sleep(TEN_SECONDS.toMillis());
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public static void waitForVirgoServerShutdownFully() {
        waitForVirgoServerShutdownFully(THIRTY_SECONDS, HALF_SECOND);
    }

    private static void waitForVirgoServerShutdownFully(Duration duration, Duration interval) {
        Instant start = Instant.now();
        while (start.plus(duration).isAfter(Instant.now())) {
            try {
                if (!getKernelStatus().equals(STATUS_STARTED)) {
                    if (NetUtils.isPortAvailable(9875)) {
                        return;
                    }
                }
            } catch (Exception e) {
                // ignore and try again
            }
            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        // allow some more time to finish shutdown
        try {
            Thread.sleep(TEN_SECONDS.toMillis());
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public static void waitForMBean(String mBeanName) throws Exception {
        waitForMBean(mBeanName, HALF_SECOND, TWO_MINUTES);
    }

    public static void waitForMBean(String mBeanName, Duration sleepDuration, Duration duration) throws Exception {
        Instant start = Instant.now();
        boolean mbeanStatus = false;
        while (start.plus(duration).isAfter(Instant.now())) {
            try {
                ObjectName objectName = new ObjectName(mBeanName);
                System.out.println("Looking for JMX object '" + objectName + "'");
                mbeanStatus = getMBeanServerConnection().isRegistered(objectName);
                if (mbeanStatus) {
                    return;
                }
            } catch (IOException e) {
                // swallow and retry
            }
            Thread.sleep(sleepDuration.toMillis());
        }
        fail(String.format("After %d s and %d ns, artifact %s mbean Status was", duration.getSeconds(), duration.getNano(), mBeanName) + mbeanStatus);
    }

    public static void waitForArtifactInUserRegion(String type, String name, String version, long interval, long duration) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean mbeanStatus = false;
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                ObjectName objectName = getObjectName(type, name, version);
                System.out.println("Looking for JMX object '" + objectName + "'");
                mbeanStatus = getMBeanServerConnection().isRegistered(objectName);
                if (mbeanStatus) {
                    return;
                }
            } catch (IOException e) {
                // swallow and retry
            }
            Thread.sleep(interval);
        }
        fail(String.format("After %d ms, artifact %s mbean Status was", duration, name) + mbeanStatus);
    }

    public static void waitForArtifactInKernelRegion(String type, String name, String version, long interval, long duration) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean mbeanStatus = false;
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                ObjectName objectName = getObjectNameInKernelRegion(type, name, version);
                System.out.println("Looking for JMX object '" + objectName + "'");
                mbeanStatus = getMBeanServerConnection().isRegistered(objectName);
                if (mbeanStatus) {
                    return;
                }
            } catch (IOException e) {
                // swallow and retry
            }
            Thread.sleep(interval);
        }
        fail(String.format("After %d ms, artifact %s mbean Status was", duration, name) + mbeanStatus);
    }

    public static MBeanServerConnection getMBeanServerConnection() {
        String severDir = null;

        String[] creds = { "admin", "admin" };
        Map<String, String[]> env = new HashMap<String, String[]>();

        try {
            severDir = virgoHome.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get canonical path of server directory '" + virgoHome + "'.");
        }

        env.put(JMXConnector.CREDENTIALS, creds);
        System.setProperty("javax.net.ssl.trustStore", severDir + KEYSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", KEYPASSWORD);
        JMXServiceURL url;
        try {
            url = new JMXServiceURL(JMXURL);
            connection = JMXConnectorFactory.connect(url, env).getMBeanServerConnection();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed to create JMX connection to '" + JMXURL + "'.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create JMX connection to '" + JMXURL + "'.", e);
        }
        return connection;
    }

    public static ObjectName getObjectName(String type, String name, String version) throws MalformedObjectNameException {
        return new ObjectName(String.format(
            "org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=%s,name=%s,version=%s,region=org.eclipse.virgo.region.user", type, name,
            version));
    }

    public static ObjectName getObjectNameInKernelRegion(String type, String name, String version) throws MalformedObjectNameException {
        return new ObjectName(String.format(
            "org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=%s,name=%s,version=%s,region=org.eclipse.equinox.region.kernel", type, name,
            version));
    }

    public static void deploy(File uploadedFile) throws Exception {
        deploy(java.util.Collections.singletonList(uploadedFile));
    }

    public static void deploy(List<File> uploadedFiles) throws Exception {
        waitForMBean(DEPLOYER_MBEAN_NAME, HALF_SECOND, TWO_MINUTES);
        ObjectName objectName = new ObjectName(DEPLOYER_MBEAN_NAME);
        for (File file : uploadedFiles) {
            URI uri = file.toURI();
            System.out.println("Deploying file '" + uri + "' on '" + objectName + "'");
            Object invoke = getMBeanServerConnection().invoke(objectName, "deploy", new Object[] { uri.toString() },
                new String[] { String.class.getName() });
            getDeploymentIdentity(invoke);
        }
    }

    // Taken from org.eclipse.virgo.management.console.UploadServlet.getDeploymentIdentity()
    private static String getDeploymentIdentity(Object deploymentIdentity) {
        StringBuilder builder = new StringBuilder();
        if (deploymentIdentity instanceof CompositeDataSupport) {
            CompositeDataSupport deploymentIdentityInstance = (CompositeDataSupport) deploymentIdentity;
            Object[] all = deploymentIdentityInstance.getAll(DEPLOYMENT_IDENTITY_FIELDS);
            builder.append(all[0]);
            builder.append(" - ").append(all[1]);
            builder.append(": ").append(all[2]);
        }
        return builder.toString();
    }

    public static void waitForArtifactState(String type, String name, String version, String state, long interval, long duration) throws Exception {
        long startTime = System.currentTimeMillis();
        String artifactstate = null;
        while (System.currentTimeMillis() - startTime < duration) {
            artifactstate = getMBeanServerConnection().getAttribute(getObjectName(type, name, version), "State").toString();
            if (artifactstate.equals(state)) {
                return;
            }
            Thread.sleep(interval);
        }
        fail(String.format("After %d ms, artifact %s state was", duration, name) + artifactstate);
    }

    public static void assertArtifactExists(String type, String name, String version) throws Exception {
        assertTrue(String.format("Artifact %s:%s:%s does not exist", type, name, version),
            getMBeanServerConnection().isRegistered(JmxUtils.getObjectName(type, name, version)));
    }

    public static void assertBundleArtifactExists(String name, String version) throws Exception {
        assertTrue(String.format("Artifact %s:%s:%s does not exist", ARTIFACT_BUNDLE, name, version),
            getMBeanServerConnection().isRegistered(JmxUtils.getObjectName(ARTIFACT_BUNDLE, name, version)));
    }

    public static void assertBundleArtifactExistsInKernelRegion(String name, String version) throws Exception {
        assertTrue(String.format("Artifact %s:%s:%s does not exist", ARTIFACT_BUNDLE, name, version),
            getMBeanServerConnection().isRegistered(JmxUtils.getObjectNameInKernelRegion(ARTIFACT_BUNDLE, name, version)));
    }

    public static void assertBundleArtifactState(String name, String version, String state) throws Exception {
        assertArtifactState(ARTIFACT_BUNDLE, name, version, state);
    }

    public static void assertBundleArtifactStateInKernelRegion(String name, String version, String state) throws Exception {
        assertArtifactStateInKernelRegion(ARTIFACT_BUNDLE, name, version, state);
    }

    public static void assertArtifactState(String type, String name, String version, String state) throws Exception {
        assertEquals(String.format("admin console plan artifact %s:%s:%s is not in state %s", type, name, version, state), state,
            getMBeanServerConnection().getAttribute(JmxUtils.getObjectName(type, name, version), "State"));
    }

    public static void assertArtifactStateInKernelRegion(String type, String name, String version, String state) throws Exception {
        assertEquals(String.format("admin console plan artifact %s:%s:%s is not in state %s", type, name, version, state), state,
            getMBeanServerConnection().getAttribute(JmxUtils.getObjectNameInKernelRegion(type, name, version), "State"));
    }

    public static void uninstall(String type, String name, String version) throws Exception {
        getMBeanServerConnection().invoke(getObjectName(type, name, version), "uninstall", new Object[] {}, new String[] {});
    }

    public static String getKernelStatus() {
        try {
            return (String) getMBeanServerConnection().getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"), "Status");
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        } catch (InstanceNotFoundException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        } catch (MBeanException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        } catch (ReflectionException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get server status.", e);
        }
    }

}
