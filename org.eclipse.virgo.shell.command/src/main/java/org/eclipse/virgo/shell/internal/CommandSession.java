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


import java.util.List;

/**
 * This can take a typed command and figure out the correct Command to call or Completer to use if the command is not recognised.
 * 
 * Implementations should be threadsafe
 * 
 */
interface CommandSession
{
    /**
     * Execute a program in this session.
     *
     * @param commandline
     * @return the result of the execution
     * @throws Exception 
     */
    List<String> execute(CharSequence commandline) throws Exception;

}
