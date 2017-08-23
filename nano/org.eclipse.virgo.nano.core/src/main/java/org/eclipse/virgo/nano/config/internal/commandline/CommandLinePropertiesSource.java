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

package org.eclipse.virgo.nano.config.internal.commandline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;


import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.config.internal.PropertiesSource;
import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;

public final class CommandLinePropertiesSource implements PropertiesSource {
	
	private static final String PROPERTY_USERREGION_COMMANDLINE_ARTIFACTS = "commandLineArtifacts";

	private static final String PROPERTY_UNRECOGNIZED_LAUNCHER_ARGUMENTS = "eclipse.commands";
	
	private static final String COMMAND_PREFIX = "-";
	
	private static final String COMMAND_PLAN = "plan";
	
	private static final String PID_KERNEL_REGION = "org.eclipse.virgo.kernel.userregion";
	
	private static final String TEMPLATE_VERSIONED_PLAN_REPOSITORY_URI = "repository:plan/%s/%s";
	
	private static final String TEMPLATE_UNVERSIONED_PLAN_REPOSITORY_URI = "repository:plan/%s";
	
	private final String unrecognizedArguments;
	
	private final EventLogger eventLogger;
	
	public CommandLinePropertiesSource(BundleContext bundleContext, EventLogger eventLogger) {
		this.unrecognizedArguments = bundleContext.getProperty(PROPERTY_UNRECOGNIZED_LAUNCHER_ARGUMENTS);
		this.eventLogger = eventLogger;
	}

	public Map<String, Properties> getConfigurationProperties() {
		
		Map<String, Properties> configuration = new HashMap<String, Properties>();
		
		if (this.unrecognizedArguments != null) {
			String[] components = this.unrecognizedArguments.split("\n");
			
			List<String> arguments = null;
			String command = null;
			
			for (int i = 0; i < components.length; i++) {
				if (components[i].startsWith(COMMAND_PREFIX)) {
					if (command != null) {
						processCommand(command, arguments, configuration);
					}					
					command = components[i].substring(COMMAND_PREFIX.length());
					arguments = new ArrayList<String>();					
				} else if (arguments != null) {
					arguments.add(components[i]);
				}
			}
			
			if (command != null) {
				processCommand(command, arguments, configuration);
			}
		}
		
		return configuration;
	}

	private void processCommand(String command, List<String> arguments, Map<String, Properties> configuration) {
		if (COMMAND_PLAN.equals(command)) {
			processPlanCommand(arguments, configuration);
		}
	}
	
	private void processPlanCommand(List<String> arguments, Map<String, Properties> configuration) {
		String repositoryUri = null;
		
		if (arguments.size() == 1) {
			repositoryUri = String.format(TEMPLATE_UNVERSIONED_PLAN_REPOSITORY_URI, arguments.get(0));
		} else if (arguments.size() == 2) {
			repositoryUri = String.format(TEMPLATE_VERSIONED_PLAN_REPOSITORY_URI, arguments.get(0), arguments.get(1));
		} else {
			this.eventLogger.log(KernelLogEvents.KERNEL_PLAN_ARGUMENTS_INCORRECT, arguments.size(), formatArgumentList(arguments));			
		}
		
		if (repositoryUri != null) {
			Properties properties = getProperties(PID_KERNEL_REGION, configuration);
			appendProperty(PROPERTY_USERREGION_COMMANDLINE_ARTIFACTS, repositoryUri, properties);
		}		
	}
	
	private String formatArgumentList(List<String> arguments) {
		if (arguments.size() == 0) {
			return "";
		}
		
		StringBuilder argumentsBuilder = new StringBuilder();
		
		for (int i = 0; i < arguments.size(); i++) {
			argumentsBuilder.append(arguments.get(i));
			if ((i + 1) < arguments.size()) {
				argumentsBuilder.append(", ");
			}
		}
		
		return argumentsBuilder.toString();
	}
	
	private Properties getProperties(String pid, Map<String, Properties> configuration) {
		Properties properties = configuration.get(pid);
		if (properties == null) {
			properties = new Properties();
			configuration.put(pid, properties);
		}
		return properties;
	}
	
	private void appendProperty(String key, String value, Properties properties) {
		String property = properties.getProperty(key);
		if (property != null) {
			property = property + "," + value;
		} else {
			property = value;
		}
		properties.put(key, property);
	}

}
