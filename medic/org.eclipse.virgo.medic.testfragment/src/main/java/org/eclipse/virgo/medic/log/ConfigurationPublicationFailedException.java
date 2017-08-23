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

package org.eclipse.virgo.medic.log;

/**
 * Thrown by {@link LoggingConfigurationPublisher} when a request to publish logging configuration fails.
 */
public class ConfigurationPublicationFailedException extends Exception {

    private static final long serialVersionUID = 4317804271280636565L;

    /**
     * Creates a new exception with the supplied message and cause.
     * 
     * @param message The exception's message
     * @param cause The exception's cause
     */
    public ConfigurationPublicationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
