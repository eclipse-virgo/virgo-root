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

package org.eclipse.gemini.mgmt.cm;

import static org.eclipse.gemini.mgmt.codec.OSGiProperties.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.openmbean.TabularData;

import org.osgi.framework.InvalidSyntaxException;

import org.osgi.jmx.service.cm.ConfigurationAdminMBean;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/** 
 * 
 */
public class ConfigAdminManager implements ConfigurationAdminMBean {

	protected ConfigurationAdmin admin;
	private static final Logger log = Logger.getLogger(ConfigAdminManager.class
			.getCanonicalName());

	public ConfigAdminManager(ConfigurationAdmin admin) {
		this.admin = admin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#createFactoryConfiguration
	 * (java.lang.String)
	 */
	public String createFactoryConfiguration(String factoryPid)
			throws IOException {
		if (factoryPid == null) {
			throw new IOException("Factory PID must not be null");
		}
		Configuration c = admin.createFactoryConfiguration(factoryPid);
		c.setBundleLocation(null);
		return c.getPid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#createFactoryConfiguration
	 * (java.lang.String, java.lang.String)
	 */
	public String createFactoryConfigurationForLocation(String factoryPid,
			String location)
			throws IOException {
		if (factoryPid == null) {
			throw new IOException("Factory PID must not be null");
		}
		Configuration c = admin.createFactoryConfiguration(factoryPid);
		c.setBundleLocation(location);
		return c.getPid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#delete(java.lang.String)
	 */
	public void delete(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, null).delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#delete(java.lang.String,
	 * java.lang.String)
	 */
	public void deleteForLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, location).delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#deleteConfigurations(
	 * java.lang.String)
	 */
	public void deleteConfigurations(String filter) throws IOException {
		Configuration[] confs;
		try {
			confs = admin.listConfigurations(filter);
		} catch (InvalidSyntaxException e) {
			log.log(Level.SEVERE, "Invalid filter argument: " + filter, e);
			throw new IOException("Invalid filter: " + e);
		}
		if (confs != null) {
			for (Configuration conf : confs) {
				conf.delete();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#getBundleLocation(java
	 * .lang.String)
	 */
	public String getBundleLocation(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, null).getBundleLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#getFactoryPid(java.lang
	 * .String)
	 */
	public String getFactoryPid(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, null).getFactoryPid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#getFactoryPid(java.lang
	 * .String, java.lang.String)
	 */
	public String getFactoryPidForLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, location).getFactoryPid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#getProperties(java.lang
	 * .String)
	 */
	@SuppressWarnings("unchecked")
	public TabularData getProperties(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		Dictionary properties = admin.getConfiguration(pid, null)
				.getProperties();
		return properties == null ? null : tableFrom(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#getProperties(java.lang
	 * .String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public TabularData getPropertiesForLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		Dictionary properties = admin.getConfiguration(pid, location)
				.getProperties();
		return properties == null ? null : tableFrom(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#listConfigurations(java
	 * .lang.String)
	 */
	public String[][] getConfigurations(String filter) throws IOException {
		ArrayList<String[]> pids = new ArrayList<String[]>();
		Configuration[] configurations;
		try {
			configurations = admin.listConfigurations(filter);
		} catch (InvalidSyntaxException e) {
			log.log(Level.SEVERE, "Invalid filter argument: " + filter, e);
			throw new IOException("Invalid filter: " + e);
		}
		if (configurations != null) {
			for (Configuration config : configurations) {
				pids.add(new String[] { config.getPid(),
						config.getBundleLocation() });
			}
		}
		return pids.toArray(new String[pids.size()][]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#setBundleLocation(java
	 * .lang.String, java.lang.String)
	 */
	public void setBundleLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid).setBundleLocation(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#update(java.lang.String,
	 * javax.management.openmbean.TabularData)
	 */
	public void update(String pid, TabularData table) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, null).update(propertiesFrom(table));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.jmx.compendium.ConfigAdminManagerMBean#update(java.lang.String,
	 * java.lang.String, javax.management.openmbean.TabularData)
	 */
	public void updateForLocation(String pid, String location, TabularData table)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, location).update(propertiesFrom(table));
	}

}
