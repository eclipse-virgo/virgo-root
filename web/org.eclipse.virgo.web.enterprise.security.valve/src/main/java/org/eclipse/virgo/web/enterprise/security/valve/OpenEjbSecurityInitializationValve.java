/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.security.valve;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Assembler;
import org.eclipse.virgo.web.enterprise.security.StandardSecurityService;

public class OpenEjbSecurityInitializationValve extends ValveBase {

    private StandardSecurityService securityService;

    public OpenEjbSecurityInitializationValve() {
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        Object oldState = null;
        Wrapper wrapper = (Wrapper) request.getMappingData().wrapper;

        if (getSecurityService() != null && wrapper != null) {
            oldState = this.securityService.enterWebApp(wrapper, request.getPrincipal(), wrapper.getRunAs());
        }

        try {
            getNext().invoke(request, response);
        } finally {
            if (this.securityService != null) {
                this.securityService.exitWebApp(oldState);
            }
        }

    }

    private StandardSecurityService getSecurityService() {
        if (this.securityService == null) {
            Assembler assembler = SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
            this.securityService = (StandardSecurityService) assembler.getSecurityService();
        }
        return this.securityService;
    }

}
