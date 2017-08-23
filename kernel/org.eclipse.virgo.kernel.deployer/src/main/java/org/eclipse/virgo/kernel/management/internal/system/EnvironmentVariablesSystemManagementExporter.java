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

package org.eclipse.virgo.kernel.management.internal.system;

import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

/**
 * An implementation that exports the starting environment variables.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class EnvironmentVariablesSystemManagementExporter extends
        AbstractSystemManagementExporter<EnvironmentVariablesSystemManagementExporter.EnvironmentVariablesMBean> {

	private static final String ENVIRONMENT_VARIABLES = "Environment Variables";

	@Override
	EnvironmentVariablesMBean getBean() {
		return new EnvironmentVariablesMBean();
	}

	@Override
	String getName() {
		return ENVIRONMENT_VARIABLES;
	}

	public /*TODO: was private but Eclipse 3.5 objected */ static class EnvironmentVariablesMBean implements DynamicMBean {

		private final Map<String, String> environmentVariables = System.getenv();

		public Object getAttribute(String name) {
			return environmentVariables.get(name);
		}

		public AttributeList getAttributes(String[] names) {
			AttributeList attributeList = new AttributeList();
			for (String name : names) {
				attributeList.add(new Attribute(name, getAttribute(name)));
			}
			return attributeList;
		}

		public MBeanInfo getMBeanInfo() {
			MBeanAttributeInfo[] infos = new MBeanAttributeInfo[environmentVariables.size()];

			int i = 0;
			for (String name : environmentVariables.keySet()) {
				infos[i++] = new MBeanAttributeInfo(name, "java.lang.String", "", true, false, false);
			}

			return new MBeanInfo(this.getClass().getCanonicalName(), "", infos, new MBeanConstructorInfo[0], new MBeanOperationInfo[0],
			        new MBeanNotificationInfo[0]);
		}

		public Object invoke(String method, Object[] arguments, String[] argumentTypes) throws MBeanException, ReflectionException {
			throw new UnsupportedOperationException();
		}

		public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
		        ReflectionException {
			throw new UnsupportedOperationException();
		}

		public AttributeList setAttributes(AttributeList attributes) {
			throw new UnsupportedOperationException();
		}

	}

}
