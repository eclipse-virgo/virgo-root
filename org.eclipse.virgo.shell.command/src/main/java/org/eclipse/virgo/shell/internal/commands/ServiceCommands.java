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

package org.eclipse.virgo.shell.internal.commands;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.ServiceCommandFormatter;
import org.eclipse.virgo.shell.internal.util.QuasiServiceUtil;
import org.eclipse.virgo.shell.internal.util.ServiceHolder;


@Command("service")
public final class ServiceCommands {

    private final ServiceCommandFormatter formatter;
    
	private QuasiServiceUtil quasiServiceUtil;

    public ServiceCommands(QuasiServiceUtil quasiServiceUtil) {
        this.quasiServiceUtil = quasiServiceUtil;
        this.formatter = new ServiceCommandFormatter();
    }

    @Command("list")
    public List<String> list() {
        return this.formatter.formatList(this.quasiServiceUtil.getAllServices());
    }

    @Command("examine")
    public List<String> examine(long serviceId) {
    	ServiceHolder service = this.quasiServiceUtil.getService(serviceId);
        if (service == null) {
            return Arrays.asList(String.format("No service with id '%s' was found", serviceId));
        } else {
            return this.formatter.formatExamine(service);
        }
    }
    
}
