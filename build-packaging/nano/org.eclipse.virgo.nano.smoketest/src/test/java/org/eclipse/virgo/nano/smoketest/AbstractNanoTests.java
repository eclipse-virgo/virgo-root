/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.smoketest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.rmi.ConnectIOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.NetUtils;

public class AbstractNanoTests {

    private MBeanServerConnection connection = null;

    private String srcDir = "src/test/resources";

    private File bundlesDir = null;

    private String binDir = null;

    private File pickupDir = null;

    private Process process = null;

    private ProcessBuilder pb = null;

    private File startup = null;

    private String startupFileName = null;

    private File shutdown = null;

    private String shutdownFileName = null;

    private File startupURI = null;

    private File shutdownURI = null;

    private OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    public final long HALF_SECOND = 500;

    public final long TWO_MINUTES = 120 * 1000;
    
    public final long THIRTY_SECONDS = 30 * 1000;

    public final String STATUS_STARTED = "STARTED";

    public final String STATUS_STARTING = "STARTING";

    protected MBeanServerConnection getMBeanServerConnection() throws Exception {
        Map<String, String[]> env = new HashMap<String, String[]>();

        File serverDir = new File("./target/test-expanded/");
        String[] creds = { "admin", "springsource" };
        env.put(JMXConnector.CREDENTIALS, creds);

        System.setProperty("javax.net.ssl.trustStore", serverDir + "/configuration/keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi");
        connection = JMXConnectorFactory.connect(url, env).getMBeanServerConnection();
        return connection;
    }

    protected String getNanoStartUpStatus() throws Exception {
        String nanoStartupStatus = (String) getMBeanServerConnection().getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"),
            "Status");
        return nanoStartupStatus;
    }

    private String getNanoBinDir() throws IOException {
        if (binDir == null) {
            File testExpanded = new File("./target/test-expanded/");
            binDir = new File(testExpanded, "bin").getCanonicalPath();
        }
        return binDir;
    }
    
    private File setupNanoPickupDir() throws IOException {
        if (pickupDir == null) {
            File testExpanded = new File("./target/test-expanded/");
            pickupDir = new File(testExpanded, "pickup");
        }
        return pickupDir;
    }
    
    private File setupBundleResourcesDir() throws IOException {
        if (bundlesDir == null) {
            File testExpanded = new File("./" + srcDir);
            bundlesDir = new File(testExpanded, "bundles");
        }
        return bundlesDir;
    }
    
    public void hotDeployTestBundles(String bundleName) throws IOException {
        setupNanoPickupDir();
        setupBundleResourcesDir();
        FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(pickupDir, bundleName));
    }

    public void waitForNanoStartFully() throws Exception {
        waitForNanoStartFully(THIRTY_SECONDS, HALF_SECOND);
    }

    public void waitForNanoShutdownFully() throws Exception {
        waitForNanoShutdownFully(THIRTY_SECONDS, HALF_SECOND);
    }

    private void waitForNanoStartFully(long duration, long interval) throws Exception {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                if (getNanoStartUpStatus().equals(STATUS_STARTED)) {
                    return;
                } else if (getNanoStartUpStatus().equals(STATUS_STARTING)) {
                    continue;
                }
            } catch (InstanceNotFoundException e) {
                continue;
            } catch (ConnectIOException e) {
                continue;
            } catch (IOException e) {
                continue;
            }
            Thread.sleep(interval);
        }
    }

    private void waitForNanoShutdownFully(long duration, long interval) throws Exception {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                if (!getNanoStartUpStatus().equals(STATUS_STARTED)) {
                    if (NetUtils.isPortAvailable(9875)) {
                        return;
                    }
                }
            } catch (InstanceNotFoundException e) {
                return;
            } catch (ConnectIOException e) {
                return;
            } catch (IOException e) {
                return;
            }
            Thread.sleep(interval);
        }
    }

    protected class NanoStartUpThread implements Runnable {

        public NanoStartUpThread() {
        }

        @Override
        public void run() {
            String[] args = null;
            try {
                if (os.getName().contains("Windows")) {
                    startup = new File(getNanoBinDir(), "startup.bat");
                    startupURI = new File(startup.toURI());
                    startupFileName = startupURI.getCanonicalPath();

                } else {
                    startup = new File(getNanoBinDir(), "startup.sh");
                    startupURI = new File(startup.toURI());
                    startupFileName = startupURI.getCanonicalPath();
                }
                args = new String[] { startupFileName };
                pb = new ProcessBuilder(args);
                pb.redirectErrorStream(true);
                Map<String, String> env = pb.environment();
                env.put("JAVA_HOME", System.getProperty("java.home"));

                process = pb.start();

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected class NanoShutdownThread implements Runnable {

        public NanoShutdownThread() {
        }

        @Override
        public void run() {
            String[] args = null;
            try {
                if (os.getName().contains("Windows")) {
                    shutdown = new File(getNanoBinDir(), "shutdown.bat");
                    shutdownURI = new File(shutdown.toURI());
                    shutdownFileName = shutdownURI.getCanonicalPath();
                } else {
                    shutdown = new File(getNanoBinDir(), "shutdown.sh");
                    shutdownURI = new File(shutdown.toURI());
                    shutdownFileName = shutdownURI.getCanonicalPath();
                }
                args = new String[] { shutdownFileName };
                pb = new ProcessBuilder(args);
                pb.redirectErrorStream(true);
                Map<String, String> env = pb.environment();
                env.put("JAVA_HOME", System.getProperty("java.home"));

                process = pb.start();

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
