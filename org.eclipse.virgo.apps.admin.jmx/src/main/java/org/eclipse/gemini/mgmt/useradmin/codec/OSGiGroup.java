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
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

/** 
 */
public class OSGiGroup extends OSGiUser {
	/**
	 * the members
	 */
	protected String[] members;
	/**
	 * the required members
	 */
	protected String[] requiredMembers;

	/**
	 * Construct an instance from the OSGi group
	 * 
	 * @param group
	 */
	public OSGiGroup(Group group) {
		super(group);
		Role[] m = group.getMembers();
		if (m != null) {
			members = new String[m.length];
			int i = 0;
			for (Role role : m) {
				members[i++] = role.getName();
			}
		} else {
			members = new String[0];
		}
		Role[] rm = group.getRequiredMembers();
		if (rm != null) {
			requiredMembers = new String[rm.length];
			int i = 0;
			for (Role role : rm) {
				requiredMembers[i++] = role.getName();
			}
		} else {
			requiredMembers = new String[0];
		}
	}

	/**
	 * Construct an instance from the supplied composite data
	 * 
	 * @param data
	 */
	public OSGiGroup(CompositeData data) {
		super(data);
		members = (String[]) data.get(UserAdminMBean.MEMBERS);
		requiredMembers = (String[]) data.get(UserAdminMBean.REQUIRED_MEMBERS);
	}

	/**
	 * Convert the receiver into the composite data representation
	 * 
	 * @return the composite data representation of the receiver
	 * @throws OpenDataException
	 */
	@SuppressWarnings("boxing")
	public CompositeData asCompositeData() throws OpenDataException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(UserAdminMBean.NAME, name);
		items.put(UserAdminMBean.TYPE, type);
		items.put(UserAdminMBean.MEMBERS, members);
		items.put(UserAdminMBean.REQUIRED_MEMBERS, requiredMembers);
		return new CompositeDataSupport(UserAdminMBean.GROUP_TYPE, items);
	}

	/**
	 * @return the members
	 */
	public String[] getMembers() {
		return members;
	}

	/**
	 * @return the requiredMembers
	 */
	public String[] getRequiredMembers() {
		return requiredMembers;
	}
}
