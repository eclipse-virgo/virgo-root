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

public class RefreshException extends DeploymentException {

    private static final long serialVersionUID = -7828141025340833008L;

    public RefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshException(String message) {
        super(message);
    }

}
