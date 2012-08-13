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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;

public class StopException extends DeploymentException {

    private static final long serialVersionUID = -1125558729622608583L;

    public StopException(String message, Throwable cause) {
        super(message, cause);
    }

    public StopException(String message) {
        super(message);
    }

}
