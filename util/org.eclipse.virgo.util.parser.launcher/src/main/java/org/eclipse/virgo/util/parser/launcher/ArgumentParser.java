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

package org.eclipse.virgo.util.parser.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Utility class for parsing command-line arguments for the OSGi launcher.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class ArgumentParser {

    private static final int MAXIMUM_BUNDLE_DECLARATION_COMPONENTS = 2;

    private static final int MAX_PROPERTY_DECLARATION_COMPONENTS = 2;

    private static final String MARKER_PROPERTY = "-F";

    private static final String MARKER_BUNDLE = "-B";

    private static final String MARKER_CONFIG = "-config";

    private static final String PROPERTY_DELIMITER = "=";

    private static final String BUNDLE_PATH_DELIMITER = "@";

    private static final String START_FLAG = "start";
    
    public static final String GRADLE_CACHE_RELATIVE = File.separator + ".gradle"
            + File.separator + "caches"
            + File.separator + "modules-2"
            + File.separator + "files-2.1";

    public LaunchCommand parse(String[] args) {
        LaunchCommand command = new LaunchCommand();
        for (int x = 0; x < args.length;) {
            String arg = args[x];
            if (arg.startsWith(MARKER_BUNDLE)) {
                BundleEntry entry = parseBundleMarker(arg);
                command.declareBundle(entry.getURI(), entry.isAutoStart());
                x++;
            } else if (arg.startsWith(MARKER_PROPERTY)) {
                parseFrameworkProperty(arg, command);
                x++;
            } else if (MARKER_CONFIG.equals(arg.trim())) {
                x++; // consume the -config arg
                if (x == args.length) {
                    throw new ParseException("Option -config supplied without path.");
                }
                String configPath = args[x];
                parseConfigProperties(configPath, command);
                x++;
            } else {
            	command.declareUnrecognizedArgument(arg);         
            	x++;
            }
        }
        return command;
    }

    /**
     * Parses a comma-separated list of bundle entries in the form &lt;uri&gt;[@start].
     * @param entryList comma-separated list of bundle entry declarations
     * @return an array of bundle entries parsed from the list
     * 
     * @see #parseBundleEntry(String)
     */
    public BundleEntry[] parseBundleEntries(String entryList) {
        String[] entries = entryList.split(",");
        BundleEntry[] result = new BundleEntry[entries.length];
        for (int x = 0; x < result.length; x++) {
            result[x] = parseBundleEntry(entries[x]);
        }
        return result;
    }

    /**
     * Parses bundle entry in the form &lt;uri&gt;[@start].
     * @param decl string to parse
     * @return bundle entry denoted by the string
     */
    public BundleEntry parseBundleEntry(String decl) {
        String[] components = parseCommandComponents(decl, BUNDLE_PATH_DELIMITER, MAXIMUM_BUNDLE_DECLARATION_COMPONENTS);

        String path = components[0];
        path = processGradleCachePlaceholder(path);
        URI uri = pathToURI(path);

        boolean autoStart = false;
        if (components.length == MAXIMUM_BUNDLE_DECLARATION_COMPONENTS) {
            String bundleCommand = components[1];
            if (START_FLAG.equals(bundleCommand)) {
                autoStart = true;
            } else {
                throw new ParseException("Unrecognized bundle command '" + bundleCommand + "' in '" + decl + "'.");
            }
        }
        return new BundleEntry(uri, autoStart);
    }

    private String processGradleCachePlaceholder(String path) {
        return path.replace("%gradle.cache%", System.getProperty("user.home") + GRADLE_CACHE_RELATIVE);
    }

    private void parseConfigProperties(String configPath, LaunchCommand command) {
        File file = new File(configPath);
        if (!file.exists()) {
            throw new ParseException("Config path '" + file.getAbsolutePath() + "' does not exist.");
        }
        Properties props = new Properties();
        try (InputStream stream = new FileInputStream(file)) {
            props.load(stream);
        } catch (IOException e) {
            throw new ParseException("Unable to read config properties file '" + file.getAbsolutePath() + "'.", e);
        }
        command.setConfigProperties(props);
    }

    /**
     * Parses a framework property of the form <code>-F&lt;name&gt;=&lt;value&gt;</code>.
     */
    private void parseFrameworkProperty(String arg, LaunchCommand command) {
        String decl = arg.substring(MARKER_PROPERTY.length());

        String[] components = parseCommandComponents(decl, PROPERTY_DELIMITER, MAX_PROPERTY_DECLARATION_COMPONENTS);

        String value = (components.length == MAX_PROPERTY_DECLARATION_COMPONENTS ? components[1] : "");

        command.declareProperty(components[0], value);
    }

    /**
     * Parses a bundle declaration of the form <code>-B&lt;path&gt;[@start]</code>
     */
    private BundleEntry parseBundleMarker(String arg) {
        String decl = arg.substring(MARKER_BUNDLE.length());
        return parseBundleEntry(decl);
    }

    private String[] parseCommandComponents(String decl, String delimiter, int maxComponents) {
        String[] components = decl.split(delimiter);

        if (components.length > maxComponents) {
            throw new ParseException("Invalid declaration: '" + decl + "'. Too many occurrences of '" + delimiter + "'.");
        }

        return components;
    }

    private URI pathToURI(String path) {

        URI uri = null;

        // see if the path is a valid file first
        File f = new File(path);
        if (f.exists()) {
            uri = f.getAbsoluteFile().toURI();
        }

        if (uri == null) {
            // now try URI; it should contain only slashes
        	path = replaceBackslashWithSlash(path);
            try {
                URI u = new URI(path);
                if (u.isAbsolute()) {
                    uri = u;
                }
            } catch (URISyntaxException ignored) {
            }
        }
        
        if(uri == null) {
            throw new ParseException("Path '" + path +"' is not a valid URI or file path");
        }
        
        return uri;
    }
    
    private String replaceBackslashWithSlash(String path) {
    	return path.replace('\\', '/');
    }
}
