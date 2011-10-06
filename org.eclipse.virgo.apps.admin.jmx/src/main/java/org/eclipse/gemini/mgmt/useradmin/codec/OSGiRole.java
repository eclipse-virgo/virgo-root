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
import org.osgi.service.useradmin.Role;

/** 
 * 
 */
public class OSGiRole {

	/**
	 * The role name
	 */
	protected String name;
	/**
	 * The type of the role
	 */
	protected int type;

	/**
	 * Construct and instance from the composite data representation
	 * 
	 * @param data
	 */
	@SuppressWarnings("boxing")
	public OSGiRole(CompositeData data) {
		name = (String) data.get(UserAdminMBean.NAME);
		type = (Integer) data.get(UserAdminMBean.TYPE);
	}

	/**
	 * Construct and instance from the supplied OSGi role
	 * 
	 * @param role
	 */
	public OSGiRole(Role role) {
		name = role.getName();
		type = role.getType();
	}

	/**
	 * Convert the receiver into the composite data that represents it
	 * 
	 * @return the
	 * @throws OpenDataException
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() throws OpenDataException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(UserAdminMBean.NAME, name);
		items.put(UserAdminMBean.TYPE, type);
		return new CompositeDataSupport(UserAdminMBean.ROLE_TYPE, items);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
}
