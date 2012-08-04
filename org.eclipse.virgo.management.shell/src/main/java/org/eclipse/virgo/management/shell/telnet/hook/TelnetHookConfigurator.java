/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.telnet.hook;

import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;

import org.eclipse.virgo.osgi.console.telnet.hook.TelnetHook;

public class TelnetHookConfigurator implements HookConfigurator {

    public void addHooks(HookRegistry hookRegistry) {
        hookRegistry.addAdaptorHook(new TelnetHook());
    }

}
