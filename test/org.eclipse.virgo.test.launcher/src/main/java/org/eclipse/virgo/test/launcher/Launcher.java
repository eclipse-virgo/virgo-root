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

package org.eclipse.virgo.test.launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.parser.launcher.ArgumentParser;
import org.eclipse.virgo.util.parser.launcher.BundleEntry;
import org.eclipse.virgo.util.parser.launcher.LaunchCommand;
import org.osgi.framework.BundleException;


public class Launcher {

	private static final char UNRECOGNIZED_ARGUMENT_SEPARATOR = ',';
	
	private static final String FRAMEWORK_PROPERTY_UNRECOGNIZED_ARGUMENTS = "org.eclipse.virgo.osgi.launcher.unrecognizedArguments";
	
	private static final String SYSTEM_PROPERTY_TMPDIR = "java.io.tmpdir";

	public static void main(String[] args) throws IOException {
	    ensureTmpDirExists();
	    
		ArgumentParser parser = new ArgumentParser();
		LaunchCommand command = parser.parse(args);
		
		FrameworkBuilder builder = new FrameworkBuilder(command.getConfigProperties());
		
		BundleEntry[] bundleDeclarations = command.getBundleEntries();
		for (BundleEntry bundleDeclaration : bundleDeclarations) {
			builder.addBundle(bundleDeclaration.getURI());
		}

		Map<String, String> declaredProperties = command.getDeclaredProperties();
		for (Map.Entry<String, String> entry : declaredProperties.entrySet()) {
			builder.addFrameworkProperty(entry.getKey(), entry.getValue());
		}
		
		builder.addFrameworkProperty(FRAMEWORK_PROPERTY_UNRECOGNIZED_ARGUMENTS, createUnrecognizedArgumentsProperty(command));

        try {
            builder.start();
        } catch (BundleException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
	
	static void ensureTmpDirExists() throws IOException {
	    String tmpDirProperty = System.getProperty(SYSTEM_PROPERTY_TMPDIR);
	    if (tmpDirProperty != null) {
	        File tmpDir = new File(tmpDirProperty);
	        if (!tmpDir.isDirectory() && !tmpDir.mkdirs()) {
	            throw new IOException("Failed to create tmp directory '" + tmpDir.getAbsolutePath() + "'");
	        }
	    }
	}
	
	private static String createUnrecognizedArgumentsProperty(LaunchCommand launchCommand) {
		List<String> unrecognizedArguments = launchCommand.getUnrecognizedArguments();
		
		StringBuilder propertyBuilder = new StringBuilder();
		
		for (int i = 0; i < unrecognizedArguments.size(); i++) {
			propertyBuilder.append(unrecognizedArguments.get(i));
			if ((i + 1) < unrecognizedArguments.size()) {
				propertyBuilder.append(UNRECOGNIZED_ARGUMENT_SEPARATOR);
			}
		}
		
		return propertyBuilder.toString();
	}
}
