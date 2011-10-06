/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.management.openmbean.CompositeData;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import org.eclipse.gemini.mgmt.framework.codec.BundleBatchActionResult;
import org.eclipse.gemini.mgmt.framework.codec.BundleBatchInstallResult;

import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

/** 
 * 
 */
public class Framework implements FrameworkMBean {
	public Framework(BundleContext bc, PackageAdmin admin, StartLevel sl) {
		this.bc = bc;
		this.admin = admin;
		this.sl = sl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#getFrameworkStartLevel()
	 */
	public int getFrameworkStartLevel() throws IOException {
		return sl.getStartLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#getInitialBundleStartLevel()
	 */
	public int getInitialBundleStartLevel() throws IOException {
		return sl.getInitialBundleStartLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#installBundle(java.lang.String)
	 */
	public long installBundle(String location) throws IOException {
		try {
			return bc.installBundle(location).getBundleId();
		} catch (Throwable e) {
			throw new IOException("Unable to install bundle: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#installBundle(java.lang.String,
	 * java.lang.String)
	 */
	public long installBundleFromURL(String location, String url)
			throws IOException {
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			return bc.installBundle(location, is).getBundleId();
		} catch (Throwable e) {
			throw new IOException("Unable to install bundle: " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#installBundles(java.lang.String[])
	 */
	public CompositeData installBundles(String[] locations) throws IOException {
		if (locations == null) {
			throw new IOException("locations must not be null");
		}
		long ids[] = new long[locations.length];
		for (int i = 0; i < locations.length; i++) {
			try {
				ids[i] = bc.installBundle(locations[i]).getBundleId();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(ids, 0, completed, 0, completed.length);
				String[] remaining = new String[locations.length - i - 1];
				System.arraycopy(locations, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchInstallResult(e.toString(), completed,
						locations[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchInstallResult(ids).asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#installBundles(java.lang.String[],
	 * java.lang.String[])
	 */
	public CompositeData installBundlesFromURL(String[] locations, String[] urls)
			throws IOException {
		if (locations == null) {
			throw new IOException("locations must not be null");
		}
		if (urls == null) {
			throw new IOException("urls must not be null");
		}
		long ids[] = new long[locations.length];
		for (int i = 0; i < locations.length; i++) {
			InputStream is = null;
			try {
				is = new URL(urls[i]).openStream();
				ids[i] = bc.installBundle(locations[i], is).getBundleId();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(ids, 0, completed, 0, completed.length);
				String[] remaining = new String[locations.length - i - 1];
				System.arraycopy(locations, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchInstallResult(e.toString(), completed,
						locations[i], remaining).asCompositeData();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return new BundleBatchInstallResult(ids).asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#refreshPackages(long)
	 */
	public void refreshBundle(long bundleIdentifier) throws IOException {
		admin.refreshPackages(new Bundle[] { bundle(bundleIdentifier) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#refreshPackages(long[])
	 */
	public void refreshBundles(long[] bundleIdentifiers) throws IOException {
		Bundle[] bundles = null;

		if (bundleIdentifiers != null) {
			bundles = new Bundle[bundleIdentifiers.length];
			for (int i = 0; i < bundleIdentifiers.length; i++) {
				try {
					bundles[i] = bundle(bundleIdentifiers[i]);
				} catch (Throwable e) {
					IOException iox = new IOException(
							"Unable to refresh packages");
					iox.initCause(e);
					throw iox;
				}
			}
		}
		try {
			admin.refreshPackages(bundles);
		} catch (Throwable e) {
			IOException iox = new IOException("Unable to refresh packages");
			iox.initCause(e);
			throw iox;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#resolveBundle(long)
	 */
	public boolean resolveBundle(long bundleIdentifier) throws IOException {
		return admin.resolveBundles(new Bundle[] { bundle(bundleIdentifier) });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#resolveBundles(long[])
	 */
	public boolean resolveBundles(long[] bundleIdentifiers) throws IOException {
		Bundle[] bundles = null;
		if (bundleIdentifiers != null) {
			bundles = new Bundle[bundleIdentifiers.length];
			for (int i = 0; i < bundleIdentifiers.length; i++) {
				bundles[i] = bundle(bundleIdentifiers[i]);
			}
		}
		return admin.resolveBundles(bundles);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#restartFramework()
	 */
	public void restartFramework() throws IOException {
		try {
			bundle(0).update();
		} catch (BundleException e) {
			throw new IOException("Unable to restart framework: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#setBundleStartLevel(long, int)
	 */
	public void setBundleStartLevel(long bundleIdentifier, int newlevel)
			throws IOException {
		try {
			sl.setBundleStartLevel(bundle(bundleIdentifier), newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#setBundleStartLevels(long[], int[])
	 */
	public CompositeData setBundleStartLevels(long[] bundleIdentifiers,
			int[] newlevels) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		if (newlevels == null) {
			throw new IOException("new start levels must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				sl.setBundleStartLevel(bundle(bundleIdentifiers[i]),
						newlevels[i]);
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#setFrameworkStartLevel(int)
	 */
	public void setFrameworkStartLevel(int newlevel) throws IOException {
		try {
			sl.setStartLevel(newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#setInitialBundleStartLevel(int)
	 */
	public void setInitialBundleStartLevel(int newlevel) throws IOException {
		try {
			sl.setInitialBundleStartLevel(newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#shutdownFramework()
	 */
	public void shutdownFramework() throws IOException {
		try {
			bundle(0).stop();
		} catch (Throwable be) {
			throw new IOException(
					"Shutting down not implemented in this framework: " + be);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#startBundle(long)
	 */
	public void startBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).start();
		} catch (Throwable e) {
			throw new IOException("Unable to start bundle: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#startBundles(long[])
	 */
	public CompositeData startBundles(long[] bundleIdentifiers)
			throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).start();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#stopBundle(long)
	 */
	public void stopBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).stop();
		} catch (Throwable e) {
			throw new IOException("Unable to stop bundle: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#stopBundles(long[])
	 */
	public CompositeData stopBundles(long[] bundleIdentifiers)
			throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).stop();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#uninstallBundle(long)
	 */
	public void uninstallBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).uninstall();
		} catch (BundleException e) {
			throw new IOException("Unable to uninstall bundle: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#uninstallBundles(long[])
	 */
	public CompositeData uninstallBundles(long[] bundleIdentifiers)
			throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).uninstall();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#updateBundle(long)
	 */
	public void updateBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).update();
		} catch (Throwable e) {
			throw new IOException("Unable to update bundle: " + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#updateBundleFromUrl(long,
	 * java.lang.String)
	 */
	public void updateBundleFromURL(long bundleIdentifier, String url)
			throws IOException {
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			bundle(bundleIdentifier).update(is);
		} catch (Throwable e) {
			throw new IOException("Unable to update bundle: " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#updateBundles(long[])
	 */
	public CompositeData updateBundles(long[] bundleIdentifiers)
			throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).update();
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#updateBundlesFromUrls(long[],
	 * java.lang.String[])
	 */
	public CompositeData updateBundlesFromURL(long[] bundleIdentifiers,
			String[] urls) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			InputStream is = null;
			try {
				is = new URL(urls[i]).openStream();
				bundle(bundleIdentifiers[i]).update(is);
			} catch (Throwable e) {
				long[] completed = new long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0,
						completed.length);
				long[] remaining = new long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0,
						remaining.length);
				return new BundleBatchActionResult(e.toString(), completed,
						bundleIdentifiers[i], remaining).asCompositeData();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.jmx.core.FrameworkMBean#updateFramework()
	 */
	public void updateFramework() throws IOException {
		try {
			bundle(0).update();
		} catch (BundleException be) {
			throw new IOException(
					"Update of the framework is not implemented: " + be);
		}
	}

	protected Bundle bundle(long bundleIdentifier) throws IOException {
		Bundle b = bc.getBundle(bundleIdentifier);
		if (b == null) {
			throw new IOException("Bundle <" + bundleIdentifier
					+ "> does not exist");
		}
		return b;
	}

	protected BundleContext bc;
	protected StartLevel sl;
	protected PackageAdmin admin;

}
