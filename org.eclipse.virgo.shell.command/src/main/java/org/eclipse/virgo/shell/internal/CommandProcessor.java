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

package org.eclipse.virgo.shell.internal;

/**
 * A command shell can create and maintain a number of command sessions.
 */
public interface CommandProcessor {

    /**
     * Create a new command session associated with err streams.
     * <p/>
     * The session is bound to the life cycle of the bundle getting this
     * service. The session will be automatically closed when this bundle is
     * stopped or the service is returned.
     * <p/>
     * The shell will provide any available commands to this session and
     * can set additional variables.
     *
     * @return A new session.
     */
    CommandSession createSession();
}
