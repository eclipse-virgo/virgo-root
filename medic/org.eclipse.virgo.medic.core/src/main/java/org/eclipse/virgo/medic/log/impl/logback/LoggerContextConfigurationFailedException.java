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

package org.eclipse.virgo.medic.log.impl.logback;

class LoggerContextConfigurationFailedException extends Exception {

    private static final long serialVersionUID = -4259913727730630284L;

    public LoggerContextConfigurationFailedException(String message) {
        super(message);
    }

    public LoggerContextConfigurationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
