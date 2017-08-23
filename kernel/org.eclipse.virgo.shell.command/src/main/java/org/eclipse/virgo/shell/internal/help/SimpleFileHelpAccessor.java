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

package org.eclipse.virgo.shell.internal.help;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.virgo.util.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link HelpAccessor} which searches for a simple text file resource in the bundle of the class.
 * <p/>
 * The name of the file resource is the fully-qualified class name followed by "<code>.help</code>". <br/>
 * Lines beginning with a hash (pound) sign '#', as the first character, are comment lines; all other lines are
 * 'content' lines. <br/>
 * Comment lines are ignored. <br/>
 * All strings returned are terminated by a newline character.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class SimpleFileHelpAccessor implements HelpAccessor {

    private static final char HELP_FILE_COMMENT_CHARACTER = '#';

    private static final String HELP_ACCESSOR_RESOURCE_EXTENSION = ".help";

    private static final Logger logger = LoggerFactory.getLogger(SimpleFileHelpAccessor.class);

    /**
     * {@inheritDoc}
     */
    public List<String> getDetailedHelp(Class<?> clazz) {
        try (BufferedReader readFileIn = this.helpResourceReader(clazz)) {
            if (readFileIn != null) {
                return readAllButFirstHelpLines(readFileIn);
            }
        } catch (IOException ioe) {
            logger.error(String.format("Exception reading help resource for class '%s'.", clazz.getCanonicalName()), ioe);
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public String getSummaryHelp(Class<?> clazz) {
        try (BufferedReader readFileIn = this.helpResourceReader(clazz)) {
            if (readFileIn != null) {
                return readFirstHelpLine(readFileIn);
            }
        } catch (IOException ioe) {
            logger.error(String.format("Exception reading help resource for class '%s'.", clazz.getCanonicalName()), ioe);
        }
        return null;
    }

    private final String readFirstHelpLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            if (contentLine(line)) {
                return line;
            }
            line = reader.readLine();
        }
        return line;
    }

    private final List<String> readAllButFirstHelpLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<String>();

        String line = reader.readLine();
        boolean skipLine = true;
        while (line != null) {
            if (contentLine(line)) {
                if (!skipLine) {
                    lines.add(line);
                } else {
                    skipLine = false;
                }
            }
            line = reader.readLine();
        }

        return lines;
    }

    private static final boolean contentLine(String line) {
        if (line == null)
            return false;
        return (line.length() == 0 || line.charAt(0) != HELP_FILE_COMMENT_CHARACTER);
    }

    private final BufferedReader helpResourceReader(Class<?> clazz) {
        BufferedReader readFileIn = null;
        String className = clazz.getCanonicalName();
        if (className != null) {
            String fileResourceName = new StringBuffer(className).append(HELP_ACCESSOR_RESOURCE_EXTENSION).toString();
            URL resourceUrl = this.helpResourceUrl(clazz, fileResourceName);
            if (resourceUrl != null) {
                InputStream resourceIn = null;
                try {
                    resourceIn = resourceUrl.openStream();
                    readFileIn = new BufferedReader(new InputStreamReader(resourceIn, UTF_8));
                } catch (IOException ioe) {
                    logger.error(String.format("Exception reading help resource '%s'.", resourceUrl), ioe);
                    IOUtils.closeQuietly(resourceIn);
                    return null;
                }
            }
        }
        return readFileIn;
    }

    protected URL helpResourceUrl(Class<?> clazz, String fileResourceName) {
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        try {
            if (bundle != null) {
                return bundle.getResource(fileResourceName);
            }
        } catch (IllegalStateException ise) {
            logger.error(String.format("Exception obtaining help resource file '%s'.", fileResourceName), ise);
        }
        return null;
    }
}
