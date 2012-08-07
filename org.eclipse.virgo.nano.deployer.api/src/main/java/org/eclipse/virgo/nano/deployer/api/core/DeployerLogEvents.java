/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api.core;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * Defines all the {@link LogEvent LogEvents} for the deployer subsystem.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementation is immutable.
 * 
 */
public enum DeployerLogEvents implements LogEvent {

    INSTALLING(0, Level.INFO), //
    INSTALLED(1, Level.INFO), //
    INSTALL_FAILURE(2, Level.ERROR), //
    INSTALL_FAILED(3, Level.ERROR), //
    STARTING(4, Level.INFO), //
    STARTED(5, Level.INFO), //
    START_FAILED(6, Level.ERROR), //
    REFRESHING(7, Level.INFO), //
    REFRESHED(8, Level.INFO), //
    REFRESH_FAILED(9, Level.ERROR), //
    STOPPING(10, Level.INFO), //
    STOPPED(11, Level.INFO), //
    STOP_FAILED(12, Level.ERROR), //
    UNINSTALLING(13, Level.INFO), //
    UNINSTALLED(14, Level.INFO), //
    UNINSTALL_FAILED(15, Level.ERROR), //
    
    INSTALL_ARTIFACT_REFRESH_NOT_SUPPORTED(50, Level.WARNING), //
    
    NESTED_SCOPES_NOT_SUPPORTED(60, Level.ERROR), //
    
    CANNOT_REFRESH_BUNDLE_IDENTITY_CHANGED(70, Level.WARNING), //
    CANNOT_REFRESH_BUNDLE_AS_SCOPED_AND_EXPORTS_CHANGED(71, Level.WARNING),

    INSTALL_ARTIFACT_DAG_NOT_SUPPORTED(80, Level.ERROR), //
    
    WATCHED_REPOSITORY_REFRESH_FAILED(90, Level.WARNING), //
    WATCHED_REPOSITORIES_REFRESH_FAILED(91, Level.WARNING), //
 
    RECOVERY_FAILED(200, Level.ERROR), //

    DUPLICATE_PACKAGE_DURING_SCOPING(300, Level.ERROR), //
    DUPLICATE_BSN_IN_SCOPE(301, Level.ERROR), //
    CONFIG_FILE_ERROR(302, Level.ERROR), //   

    DISCARDING_BUNDLE_UPDATE_LOCATION(400, Level.WARNING), //
    MISSING_BUNDLE_SYMBOLIC_NAME(401, Level.ERROR), //
    BUNDLE_MANIFEST_NOT_FOUND(402, Level.ERROR), //

    UNABLE_TO_SATISFY_CONSTRAINTS(500, Level.ERROR), //
    UNSUPPORTED_URI_SCHEME(501, Level.ERROR), //
    START_TIMED_OUT(502, Level.ERROR), //    
    REFRESH_REQUEST_URI_NOT_FOUND(503, Level.ERROR), //
    REFRESH_REQUEST_COMPLETED(504, Level.INFO), //
    REFRESH_REQUEST_FAILED(505, Level.ERROR), //
    UNDEPLOY_ARTEFACT_NOT_FOUND(506, Level.ERROR), //
    REFRESH_ARTEFACT_NOT_FOUND(507, Level.ERROR), //
    START_ABORTED(508, Level.INFO), //    

    JAR_UNPACK_ERROR(600, Level.ERROR), //

    ARTIFACT_NOT_FOUND(700, Level.ERROR), //
    INDETERMINATE_ARTIFACT_TYPE(701, Level.ERROR), //
    MISSING_ARTIFACT_FACTORY(702, Level.ERROR), //

    REPOSITORY_DEPLOYMENT_URI_MALFORMED(800, Level.ERROR), //
    REPOSITORY_DEPLOYMENT_INVALID_VERSION(801, Level.ERROR);

    private static final String PREFIX = "DE";

    private final LogEventDelegate delegate;

    private DeployerLogEvents(int code, Level level) {
        this.delegate = new LogEventDelegate(PREFIX, code, level);
    }

    /**
     * {@inheritDoc}
     */
    public String getEventCode() {
        return this.delegate.getEventCode();
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel() {
        return this.delegate.getLevel();
    }

}
