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

package org.eclipse.virgo.kernel.shell.internal;

/**
 * A command shell can create and maintain a number of command sessions.
 */
public interface CommandProcessor
{
    /**
     * The scope of commands provided by this service. This name can be used to distinguish
     * between different command providers with the same function names.
     */
    final static String COMMAND_SCOPE = "osgi.command.scope";

    /**
     * A list of method names that may be called for this command provider. A
     * name may end with a *, this will then be calculated from all declared public
     * methods in this service.
     * <p/>
     * Help information for the command may be supplied with a space as
     * separation.
     */
    final static String COMMAND_FUNCTION = "osgi.command.function";

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
