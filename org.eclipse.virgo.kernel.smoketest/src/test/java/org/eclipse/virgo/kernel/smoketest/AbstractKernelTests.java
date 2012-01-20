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

package org.eclipse.virgo.kernel.smoketest;

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

public class AbstractKernelTests {

	private static MBeanServerConnection connection = null;
	private static String binDir = null;
	private static String configDir = null;
	private static String watchRepoDir = null;
	private static String srcDir = "src/test/resources";
	private static String srcFileName = "org.eclipse.virgo.repository.properties";
	private static String bundlesDir = "src/test/resources/bundles";
	private static String[] bundleNames = new String[] { "org.springframework.dmServer.testtool.incoho.domain-1.0.0.RELEASE.jar" };

	private static Process process = null;
	private static ProcessBuilder pb = null;
	private static File startup = null;
	private static String startupFileName = null;
	private static File shutdown = null;
	private static String shutdownFileName = null;
	private static File startupURI = null;
	private static File shutdownURI = null;
	private static OperatingSystemMXBean os = ManagementFactory
			.getOperatingSystemMXBean();
	public static final long HALF_SECOND = 500;

	public static final long TWO_MINUTES = 120 * 1000;
	public static final String STATUS_STARTED = "STARTED";
	public static final String STATUS_STARTING = "STARTING";

	protected static MBeanServerConnection getMBeanServerConnection()
			throws Exception {
		String severDir = null;
		Map<String, String[]> env = new HashMap<String, String[]>();

		File testExpanded = new File("./target/test-expanded/");
		for (File mainDir : testExpanded.listFiles()) {
			if (mainDir.isDirectory()) {
				severDir = new File(mainDir.toURI()).getCanonicalPath();

			}
		}
		String[] creds = { "admin", "springsource" };
		env.put(JMXConnector.CREDENTIALS, creds);

		System.setProperty("javax.net.ssl.trustStore", severDir
				+ "/configuration/keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

		JMXServiceURL url = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi");
		connection = JMXConnectorFactory.connect(url, env)
				.getMBeanServerConnection();
		return connection;
	}

	protected static String getKernelStartUpStatus() throws Exception {
		String kernelStartupStatus = (String) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=KernelStatus"),
						"Status");
		return kernelStartupStatus;
	}

	private static String getKernelBinDir() throws IOException {
		if (binDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					binDir = new File(candidate, "bin").getCanonicalPath();
					break;
				}
			}
		}
		return binDir;
	}

	protected static String getKernelConfigDir() throws IOException {
		if (configDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					configDir = new File(candidate, "config")
							.getCanonicalPath();
					break;
				}
			}
		}
		return configDir;
	}

	protected static void configureWatchRepoWithDefaultConfiguration(
			String destDir) {
		try {
			FileCopyUtils.copy(new File(srcDir, srcFileName), new File(destDir,
					srcFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		createWatchedRepoDir();
	}

	protected static void addBundlesToWatchedRepository(String watchRepoDir) {
		for (String bundleName : bundleNames) {
			try {
				FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
						watchRepoDir, bundleName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	protected static String getWatchedRepoDir() throws IOException {
		if (watchRepoDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File mainDir : testExpanded.listFiles()) {
				if (mainDir.isDirectory()) {
					File repositoryDir = new File(mainDir, "repository")
							.getCanonicalFile();
					if (repositoryDir.isDirectory()) {
						watchRepoDir = new File(repositoryDir, "watched-repo")
								.getCanonicalPath();
						break;
					}

				}
			}
		}
		return watchRepoDir;
	}

	protected static void createWatchedRepoDir() {
		File testExpanded = new File("./target/test-expanded/");
		for (File candidate : testExpanded.listFiles()) {
			if (candidate.isDirectory()) {
				File repoDir = new File(candidate, "repository");
				if (repoDir.mkdir()) {
					new File(repoDir, "watched-repo").mkdir();
				}
			}
		}
	}

	public static void waitForKernelStartFully() throws Exception {
		waitForKernelStartFully(TWO_MINUTES, HALF_SECOND);
	}

	public static void waitForKernelShutdownFully() throws Exception {
		waitForKernelShutdownFully(TWO_MINUTES, HALF_SECOND);
	}

	private static void waitForKernelStartFully(long duration, long interval) throws Exception {
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < duration) {
			try {
				if (getKernelStartUpStatus().equals(STATUS_STARTED)) {
					return;
				} else if (getKernelStartUpStatus().equals(STATUS_STARTING)) {
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

	private static void waitForKernelShutdownFully(long duration, long interval) throws Exception {
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < duration) {
			try {
				if (!getKernelStartUpStatus().equals(STATUS_STARTED)) {
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

	protected static class KernelStartUpThread implements Runnable {
		public KernelStartUpThread() {
		}

		public void run() {
			String[] args = null;
			try {
				if (os.getName().contains("Windows")) {
					startup = new File(getKernelBinDir(), "startup.bat");
					startupURI = new File(startup.toURI());
					startupFileName = startupURI.getCanonicalPath();

				} else {
					startup = new File(getKernelBinDir(), "startup.sh");
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

	protected static class KernelShutdownThread implements Runnable {
		public KernelShutdownThread() {
		}

		public void run() {
			String[] args = null;
			try {
				if (os.getName().contains("Windows")) {
					shutdown = new File(getKernelBinDir(), "shutdown.bat");
					shutdownURI = new File(shutdown.toURI());
					shutdownFileName = shutdownURI.getCanonicalPath();
				} else {
					shutdown = new File(getKernelBinDir(), "shutdown.sh");
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
