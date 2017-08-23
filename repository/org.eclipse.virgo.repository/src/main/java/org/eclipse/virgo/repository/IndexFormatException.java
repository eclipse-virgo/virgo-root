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

package org.eclipse.virgo.repository;

import java.io.IOException;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public class IndexFormatException extends IOException {

    private static final long serialVersionUID = 2824258135162103881L;

    public IndexFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexFormatException(String message) {
        super(message);
    }
}
