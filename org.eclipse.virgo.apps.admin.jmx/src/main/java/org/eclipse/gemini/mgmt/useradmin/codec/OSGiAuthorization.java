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

package org.eclipse.gemini.mgmt.useradmin.codec;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.useradmin.Authorization;

/** 
 * 
 */
public class OSGiAuthorization {
	/**
	 * The name of the authorization
	 */
	protected String name;
	/**
	 * The roles
	 */
	protected String[] roles;

	/**
	 * Construct the instance from the supplied composite data
	 * 
	 * @param data
	 */
	public OSGiAuthorization(CompositeData data) {
		if (data != null) {
			this.name = (String) data.get(UserAdminMBean.NAME);
			this.roles = (String[]) data.get(UserAdminMBean.ROLES);
		}
	}

	/**
	 * Construct an instance from the OSGi authorization instance
	 * 
	 * @param authorization
	 */
	public OSGiAuthorization(Authorization authorization) {
		this(authorization.getName(), authorization.getRoles());
	}

	/**
	 * Construct and instance using the supplied name and role names
	 * 
	 * @param name
	 * @param roles
	 */
	public OSGiAuthorization(String name, String[] roles) {
		this.name = name;
		this.roles = roles;
	}

	/**
	 * Convert the receiver into the composite data it represents
	 * 
	 * @return the composite data representation of the receiver
	 * @throws OpenDataException
	 */
	public CompositeData asCompositeData() throws OpenDataException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(UserAdminMBean.NAME, name);
		items.put(UserAdminMBean.ROLES, roles);
		return new CompositeDataSupport(UserAdminMBean.AUTORIZATION_TYPE, items);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the roles
	 */
	public String[] getRoles() {
		return roles;
	}
}
