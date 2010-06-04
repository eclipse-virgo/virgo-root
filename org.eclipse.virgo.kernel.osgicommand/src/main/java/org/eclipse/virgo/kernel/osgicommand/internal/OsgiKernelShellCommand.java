/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgicommand.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.virgo.kernel.shell.CommandExecutor;

/**
 * This {@link CommandProvider} extends the osgi.console with the command "vsh ..." which accesses the kernel shell commands.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 * @author Steve Powell
 */
public final class OsgiKernelShellCommand implements CommandProvider {
    CommandExecutor commandExecutor;
    
    public OsgiKernelShellCommand(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
    
    public void _vsh(CommandInterpreter commandInterpreter) {
        commandInterpreter.println("_vsh called");
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getHelp() {
        return "vsh - execute kernel shell commands; 'vsh help' to list available commands";
    }

}
