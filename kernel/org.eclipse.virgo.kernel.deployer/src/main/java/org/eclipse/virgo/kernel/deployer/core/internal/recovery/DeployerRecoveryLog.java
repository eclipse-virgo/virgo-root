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

package org.eclipse.virgo.kernel.deployer.core.internal.recovery;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.util.io.PathReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeployerRecoveryLog} maintains the deployer's recoverable state across restarts.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class DeployerRecoveryLog {

    private static final String REDEPLOY_FILE_NAME = "deployed";

    private static final String REDEPLOY_COMPRESSION_FILE_NAME = "deployed.compress";

    private static final int INITIAL_REDEPLOY_DATA_SIZE = 32 * 1024;

    private static final int COMPRESSION_THRESHOLD = 10;

    private static final int COMMAND_LENGTH = 3;

    private static final String UNDEPLOY_URI_COMMAND = "---";

    private static final String URI_SEPARATOR = ";";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PathReference redeployDataset;

    private final PathReference redeployCompressionDataset;

    private final long redeployFileLastModified;

    DeployerRecoveryLog(PathReference workArea) {
        PathReference recoveryArea = workArea.newChild("recovery");
        recoveryArea.createDirectory();

        this.redeployDataset = recoveryArea.newChild(REDEPLOY_FILE_NAME);
        this.redeployFileLastModified = this.redeployDataset.toFile().lastModified();
        this.redeployCompressionDataset = recoveryArea.newChild(REDEPLOY_COMPRESSION_FILE_NAME);

        // Recover from a crash during compression
        if (!this.redeployDataset.exists() && this.redeployCompressionDataset.exists()) {
            this.redeployCompressionDataset.copy(this.redeployDataset);
            if (!this.redeployCompressionDataset.delete()) {
                logger.warn("Could not delete '%s' in recovery after compression failure.", this.redeployCompressionDataset);
            }
        }
    }

    /**
     * Get the URIs that need to be recovered along with their deployment options.
     * 
     * @return a map of URI to deployment options
     */
    public Map<URI, DeploymentOptions> getRecoveryState() {
        Map<URI, DeploymentOptions> redeploySet = new LinkedHashMap<URI, DeploymentOptions>(20);

        String redeployData = readRedployData();

        int recordCount = 0;
        int undeployCount = 0;

        for (String uriCommandString : redeployData.split(URI_SEPARATOR)) {
            recordCount++;
            // Skip short command strings as there will typically be one
            // at the end of the dataset.
            if (uriCommandString.length() >= COMMAND_LENGTH) {
                String uriCommand = uriCommandString.substring(0, COMMAND_LENGTH);
                String uriString = uriCommandString.substring(COMMAND_LENGTH);
                try {
                    URI uri = new URI(uriString);
                    if (UNDEPLOY_URI_COMMAND.equals(uriCommand)) {
                        undeployCount++;
                        redeploySet.remove(uri);
                    } else {
                        char[] commands = uriCommand.toCharArray();
                        DeploymentOptions options = new DeploymentOptions(fromCommandOption(commands[0]), fromCommandOption(commands[1]),
                            fromCommandOption(commands[2]));

                        redeploySet.put(uri, options);
                    }
                } catch (URISyntaxException e) {
                    logger.error("Invalid URI in command string '%s' read from redeploy dataset", e, uriCommandString);
                    // skip and carry on
                }
            }
        }

        // If there is a significant amount of wasted space in the redeploy
        // dataset, rewrite it.
        if (COMPRESSION_THRESHOLD * undeployCount > recordCount) {
            rewriteRedeploySet(redeploySet);
        }

        return redeploySet;
    }

    private String readRedployData() {
        StringBuffer redeployData = new StringBuffer(INITIAL_REDEPLOY_DATA_SIZE);
        Reader redeployDataReader = null;
        try {
            redeployDataReader = new BufferedReader(new InputStreamReader(new FileInputStream(redeployDataset.toFile()), UTF_8));
            try {
                char[] chars = new char[INITIAL_REDEPLOY_DATA_SIZE];
                int numRead;
                while (-1 != (numRead = redeployDataReader.read(chars))) {
                    redeployData.append(String.valueOf(chars, 0, numRead));
                }
            } catch (IOException e) {
                logger.error("Problem reading redeploy dataset", e);
            } finally {
                try {
                    redeployDataReader.close();
                } catch (IOException e) {
                    logger.error("Problem closing redeploy dataset", e);
                }
            }
        } catch (FileNotFoundException e) {
            // Ignore - this is acceptable if there are no deployed applications
        }
        return redeployData.toString();
    }

    /**
     * Write the given set of URIs to the redeploy dataset. To avoid corruption if a crash occurs, write to a redeploy
     * compression file and then switch this for the redeploy dataset.
     * 
     * @param redeploySet the URIs to be written
     */
    private void rewriteRedeploySet(Map<URI, DeploymentOptions> redeploySet) {
        this.redeployCompressionDataset.delete();
        try (Writer redeployDataWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.redeployCompressionDataset.toFile()),
            UTF_8))) {
            for (Entry<URI, DeploymentOptions> redeployEntry : redeploySet.entrySet()) {
                recordUriCommand(redeployDataWriter, redeployEntry.getKey(), getCommandString(redeployEntry.getValue()));
            }
            redeployDataWriter.close();
        } catch (IOException e) {
            logger.warn("Problem while rewriting redeploy dataset", e);
            // Return without replacing the redeploy dataset.
        }
        // Now switch the files
        this.redeployDataset.delete();
        this.redeployCompressionDataset.moveTo(this.redeployDataset);
    }

    /**
     * Add the given location and deployment options to the recovery state.
     * 
     * @param location
     * @param deploymentOptions
     */
    void add(URI location, DeploymentOptions deploymentOptions) {
        recordUriCommand(location, getCommandString(deploymentOptions));
    }

    /**
     * Return the command string for tagging the type of a log record based on the given deployment options.
     * 
     * @param deploymentOptions
     * @return the command string
     */
    private String getCommandString(DeploymentOptions deploymentOptions) {
        // boolean recoverable, boolean deployerOwned, boolean synchronous
        StringBuilder command = new StringBuilder().append(toCommandOption(deploymentOptions.getRecoverable())).append(
            toCommandOption(deploymentOptions.getDeployerOwned())).append(toCommandOption(deploymentOptions.getSynchronous()));
        return command.toString();
    }

    /**
     * Remove the given location and associated deployment options from the recovery state.
     * 
     * @param location
     */
    void remove(URI location) {
        recordUriCommand(location, UNDEPLOY_URI_COMMAND);
    }

    private void recordUriCommand(URI uri, String command) {
        try (Writer writer = new FileWriter(this.redeployDataset.toFile(), true)) {
            recordUriCommand(writer, uri, command);
            writer.close();
        } catch (IOException e) {
            throw new FatalDeploymentException("Failed to record (un)deployment", e);
        }

    }

    private static void recordUriCommand(Writer writer, URI uri, String command) throws IOException {
        writer.write(command);
        writer.write(uri.toString());
        writer.write(URI_SEPARATOR);
    }

    /**
     * Converts boolean deployment option flag to a string representation for the logged command option
     * 
     * @param deploymentOption
     * @return
     */
    private static char toCommandOption(boolean deploymentOption) {
        return deploymentOption ? 'Y' : 'N';
    }

    /**
     * Converts from a String command option to a boolean deployment option flag
     * 
     * @param commandOption
     * @return
     */
    private static boolean fromCommandOption(char commandOption) {
        return 'Y' == commandOption ? true : false;
    }

    /**
     * Get the last modified time of the deployer's recovery file. Any applications in the pickup directory with a later
     * last modified time will need to be redeployed.
     * 
     * @return the last modified time of the deployer's recovery file
     */
    public long getRedeployFileLastModified() {
        return redeployFileLastModified;
    }

    /**
     * Update the last modified time of the deployer's recovery file.
     * 
     * @return <code>true</code> iff the operation succeeded
     */
    // TODO Make package private
    public boolean setRedeployFileLastModified() {
        return this.redeployDataset.touch();
    }

}
