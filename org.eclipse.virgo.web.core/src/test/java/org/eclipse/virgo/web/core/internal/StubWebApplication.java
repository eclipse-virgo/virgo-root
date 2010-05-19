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

package org.eclipse.virgo.web.core.internal;

import javax.servlet.ServletContext;

import org.springframework.mock.web.MockServletContext;

import org.eclipse.gemini.web.core.WebApplication;

public class StubWebApplication implements WebApplication {
    
    private boolean started;
    
    private ServletContext servletContext;
    
    StubWebApplication(String contextPath) {
        MockServletContext mockServletContext = new MockServletContext();
        mockServletContext.setContextPath(contextPath);
        this.servletContext = mockServletContext;
    }

    /** 
     * {@inheritDoc}
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /** 
     * {@inheritDoc}
     */
    public void start() {
        started = true;
    }

    /** 
     * {@inheritDoc}
     */
    public void stop() {
        started = false;
    }
    
    boolean isStarted() {
        return this.started;
    }
    
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
