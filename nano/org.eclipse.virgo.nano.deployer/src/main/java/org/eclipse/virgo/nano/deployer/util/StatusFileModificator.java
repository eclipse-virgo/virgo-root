/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.virgo.util.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StatusFileModificator class should be used as a utility class to manipulate with deployable status files.
 * Deployable status files are located in a '.state' folder created in the autodeployment folder (a.k.a the pickup
 * folder). Status files names are constructed as follows: <deployable_name>.<operation>.<operation_status>, where
 * deployable_name is the name of the deployable archive without the file extension (such as jar, war); operation - the
 * operation which result is reported through the status file, possible values: deploy | undeploy; operation_status -
 * status of the operation, possible values: ok | error; Status files include information about the bundleid and the
 * lastmodified timestamp of the deployable archive. The latter can be used to check if there have been any offline
 * updates of the deployable archive (while app server is stopped).
 * 
 * Not Thread-safe.
 */
public class StatusFileModificator {

    private static final char DOT = '.';

    private static final char NEW_LINE = '\n';

    public static final String SUCCESS_MARK = "ok";

    public static final String ERROR_MARK = "error";

    public static final String OP_DEPLOY = "deploy";

    public static final String OP_UNDEPLOY = "undeploy";

    private static final String STATE_DIR_NAME = ".state";

    private static final String BUNDLE_ID_RECORD = "bundle-id";

    private static final String LAST_MODIFIED = "last-modified";

    private static final String DELIMITER = "=";

    private static final String EMPTY_STRING = "";

    private static final Logger logger = LoggerFactory.getLogger(StatusFileModificator.class);

    private static final String[] STATUS_FILENAMES_SUFFEXES = { DOT + OP_DEPLOY + DOT + SUCCESS_MARK, DOT + OP_DEPLOY + DOT + ERROR_MARK,
        DOT + OP_UNDEPLOY + DOT + SUCCESS_MARK, DOT + OP_UNDEPLOY + DOT + ERROR_MARK };

    // pattern that matches the deploy status files - ".deploy.(ok|error)"
    private static final String STATUS_FILENAMES_DEPLOY_PATTERN = DOT + OP_DEPLOY + DOT + '(' + SUCCESS_MARK + '|' + ERROR_MARK + ')';

    /**
     * Deletes the current status file (if any) for the given deployable archive
     */
    public static void deleteStatusFile(String deployableName, File pickupDir) {
        final File stateDir = new File(pickupDir, STATE_DIR_NAME);
        if (stateDir.exists()) {
            for (String fileNameSuffix : STATUS_FILENAMES_SUFFEXES) {
                deleteFile(new File(stateDir, deployableName + fileNameSuffix));
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("State directory [" + stateDir.getAbsolutePath() + "] does not exist. Therefore, there is no status file to delete.");
            }
        }
    }

    /**
     * Deletes the first occurrence of a status file from deploy operation that matches the given war name pattern (if
     * any). There should be one match only.
     * 
     * @return the war name prefix of the deleted status file or empty string, if there's no match found
     */
    public static String deleteStatusFileByNamePattern(String warNamePattern, File pickupDir) {
        final File stateDir = new File(pickupDir, STATE_DIR_NAME);
        if (stateDir.exists()) {
            String[] statusFileNames = stateDir.list();
            if (statusFileNames != null) {
                for (String stateFileName : statusFileNames) {
                    if (stateFileName.matches(warNamePattern + STATUS_FILENAMES_DEPLOY_PATTERN)) {
                        deleteFile(new File(stateDir, stateFileName));
                        return stateFileName.substring(0, stateFileName.lastIndexOf(OP_DEPLOY) - 1);
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("State directory [" + stateDir.getAbsolutePath() + "] not listable. Therefore, there is no status file to delete.");
                }
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("State directory [" + stateDir.getAbsolutePath() + "] does not exist. Therefore, there is no status file to delete.");
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Cannot delete any status file, as no status file matches the given warNamePattern [" + warNamePattern + "].");
        }
        return EMPTY_STRING;
    }

    /**
     * Create status file about the given deplayable archive.
     * 
     * @param deployableName - the name of the deplayable artifact
     * @param pickupDir - auto deployment fodler where the deployable artifact is located. In this folder .state folder
     *        with the status file is created
     * @param operation - operation for which status is recorded in status file (i.e. StatusFileModificator.OP_DEPLOY,
     *        StatusFileModificator.OP_UNDEPLOY)
     * @param status - true if the operation was executed successfully
     * @param bundleId - bundleId of the deployable archive
     * @param lastModified - the deployable archive's latest lastmodified timestamp
     */
    public static void createStatusFile(String deployableName, File pickupDir, String operation, boolean status, long bundleId, long lastModified) {
        final File stateDir = new File(pickupDir, STATE_DIR_NAME);
        if (!stateDir.exists() && !stateDir.mkdirs()) {
            logger.error("Cannot create state directory [" + stateDir.getAbsolutePath() + "]. Status file for the operation cannot be created.");
            return;
        }
        final File statusFile = new File(stateDir, deployableName + DOT + operation + DOT + (status ? SUCCESS_MARK : ERROR_MARK));
        writeStatusFileRecord(statusFile, bundleId, lastModified);
    }

    /**
     * Returns the lastmodified timestamp if there is a successful status file for the given deployable archive.
     * Otherwise -1 is returned.
     * 
     * @param deployableName
     * @param pickupDir
     * @return
     */
    public static long getLastModifiedFromStatusFile(String deployableName, File pickupDir) {
        final File stateDir = new File(pickupDir, STATE_DIR_NAME);
        if (!stateDir.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("Checking if last stored state of [" + deployableName + "]." + "The state directory does not exist.");
            }
            return -1;
        } else {
            // there is no point to check lastmodified in case of other status files
            File statusFile = new File(stateDir, deployableName + DOT + OP_DEPLOY + DOT + SUCCESS_MARK);
            if (!statusFile.exists()) {
                if (logger.isInfoEnabled()) {
                    logger.info("The status file [" + statusFile.getAbsolutePath() + "] does not exist.");
                }
                return -1;
            }
            Properties props = loadProperties(statusFile);
            if (props != null) {
                String lastModifiedStr = props.getProperty(LAST_MODIFIED);
                if (lastModifiedStr != null && !EMPTY_STRING.equals(lastModifiedStr)) {
                    return Long.parseLong(lastModifiedStr);
                }
            }
        }
        return -1;
    }

    private static void writeStatusFileRecord(File statusFile, long bundleId, long lastModified) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(statusFile, true);
            fw.write(BUNDLE_ID_RECORD + DELIMITER + bundleId);
            fw.write(NEW_LINE);
            fw.write(LAST_MODIFIED + DELIMITER + lastModified);
            fw.write(NEW_LINE);
            fw.flush();
        } catch (IOException e) {
            logger.error("Cannot update the status of operation.", e);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    private static void deleteFile(File file) {
        if (file.exists() && !file.delete()) {
            logger.error("Cannot delete file [" + file.getAbsolutePath() + "].");
        }
    }

    private static Properties loadProperties(File statusFile) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(statusFile);
            props.load(fis);
            return props;
        } catch (IOException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Cannot load file with name [" + statusFile.getAbsolutePath() + "].", e);
            }
            return null;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
