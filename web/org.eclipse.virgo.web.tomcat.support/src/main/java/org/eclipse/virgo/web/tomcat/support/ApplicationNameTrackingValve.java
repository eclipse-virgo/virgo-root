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

package org.eclipse.virgo.web.tomcat.support;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public final class ApplicationNameTrackingValve extends ValveBase  {

    private final ApplicationNameTrackingDelegate delegate = ApplicationNameTrackingDelegate.getInstance();

    public ApplicationNameTrackingValve() {
        super(true);
    }
    
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        delegate.setApplicationNameForContextPath(request.getContextPath());
        getNext().invoke(request, response);
        delegate.clearName();
    }


}
