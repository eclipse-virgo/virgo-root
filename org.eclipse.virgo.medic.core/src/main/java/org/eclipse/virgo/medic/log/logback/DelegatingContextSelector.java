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

package org.eclipse.virgo.medic.log.logback;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.medic.log.impl.logback.ContextSelectorDelegate;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;

public class DelegatingContextSelector implements ContextSelector {

    private static volatile ContextSelectorDelegate delegate;

    private static volatile LoggerContext defaultLoggerContext;

    private static volatile boolean defaultContextConfigured;

    public DelegatingContextSelector(LoggerContext loggerContext) {
        DelegatingContextSelector.setDefaultLoggerContext(loggerContext);
    }

    private static void setDefaultLoggerContext(LoggerContext loggerContext) {
        if (delegate != null) {
            configureDefaultContextIfNecessary(delegate, loggerContext);
        }
        DelegatingContextSelector.defaultLoggerContext = loggerContext;
    }

    private static void configureDefaultContextIfNecessary(ContextSelectorDelegate delegate, LoggerContext defaultLoggerContext) {
        if (!defaultContextConfigured) {
            delegate.configureDefaultContext(defaultLoggerContext);
            defaultContextConfigured = true;
        }
    }

    public LoggerContext detachLoggerContext(String loggerContextName) {
        if (delegate == null) {
            return DelegatingContextSelector.defaultLoggerContext;
        }
        return delegate.detachLoggerContext(loggerContextName);
    }

    public List<String> getContextNames() {
        if (delegate == null) {
            return Arrays.asList(defaultLoggerContext.getName());
        }
        return delegate.getContextNames();
    }

    public LoggerContext getDefaultLoggerContext() {
        return DelegatingContextSelector.defaultLoggerContext;
    }

    public LoggerContext getLoggerContext() {
        if (delegate == null) {
            return defaultLoggerContext;
        }

        LoggerContext loggerContext = delegate.getLoggerContext();
        if (loggerContext != null) {
            return loggerContext;
        }

        return defaultLoggerContext;
    }

    public LoggerContext getLoggerContext(String name) {
        if (name.equals(defaultLoggerContext.getName())) {
            return defaultLoggerContext;
        }
        return delegate.getLoggerContext(name);
    }

    public static void setDelegate(ContextSelectorDelegate delegate) {
        if (delegate != null) {
            if (defaultLoggerContext != null) {
                configureDefaultContextIfNecessary(delegate, defaultLoggerContext);
            }
        }
        DelegatingContextSelector.delegate = delegate;
    }
}
