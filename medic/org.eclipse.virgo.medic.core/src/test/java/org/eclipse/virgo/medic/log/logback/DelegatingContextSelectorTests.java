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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.medic.log.impl.logback.ContextSelectorDelegate;
import org.eclipse.virgo.medic.log.logback.DelegatingContextSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;

public class DelegatingContextSelectorTests {

    private final ContextSelectorDelegate delegate = createMock(ContextSelectorDelegate.class);

    private final LoggerContext loggerContext = new LoggerContext();

    private DelegatingContextSelector delegatingContextSelector;

    @Before
    public void resetDefaultContextConfiguredFlag() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        Field field = DelegatingContextSelector.class.getDeclaredField("defaultContextConfigured");
        field.setAccessible(true);
        field.setBoolean(null, false);
    }

    @After
    public void cleanup() {
        DelegatingContextSelector.setDelegate(null);
    }

    @Test
    public void detachLoggerContext() {
        LoggerContext detachedContext = new LoggerContext();

        expect(this.delegate.detachLoggerContext("foo")).andReturn(detachedContext);
        expect(this.delegate.detachLoggerContext("foo")).andReturn(null);
        this.delegate.configureDefaultContext(this.loggerContext);

        replay(this.delegate);

        this.delegatingContextSelector = new DelegatingContextSelector(this.loggerContext);
        DelegatingContextSelector.setDelegate(this.delegate);

        assertEquals(detachedContext, this.delegatingContextSelector.detachLoggerContext("foo"));
        assertNull(this.delegatingContextSelector.detachLoggerContext("foo"));

        verify(this.delegate);
    }

    @Test
    public void getContextNames() {
        List<String> contextNames = Arrays.asList("a", "b", "c");

        expect(this.delegate.getContextNames()).andReturn(contextNames);
        this.delegate.configureDefaultContext(this.loggerContext);

        replay(this.delegate);

        this.delegatingContextSelector = new DelegatingContextSelector(this.loggerContext);
        DelegatingContextSelector.setDelegate(this.delegate);

        assertEquals(contextNames, this.delegatingContextSelector.getContextNames());

        verify(this.delegate);
    }

    @Test
    public void getDefaultLoggerContext() {
        this.delegate.configureDefaultContext(this.loggerContext);
        replay(this.delegate);

        this.delegatingContextSelector = new DelegatingContextSelector(this.loggerContext);
        DelegatingContextSelector.setDelegate(this.delegate);

        verify(this.delegate);

        assertEquals(this.loggerContext, this.delegatingContextSelector.getDefaultLoggerContext());
    }

    @Test
    public void getLoggerContext() {
        LoggerContext context = new LoggerContext();

        expect(this.delegate.getLoggerContext()).andReturn(context);
        this.delegate.configureDefaultContext(this.loggerContext);

        replay(this.delegate);

        this.delegatingContextSelector = new DelegatingContextSelector(this.loggerContext);
        DelegatingContextSelector.setDelegate(this.delegate);

        assertEquals(context, this.delegatingContextSelector.getLoggerContext());

        verify(this.delegate);
    }

    @Test
    public void getNamedLoggerContext() {
        LoggerContext context = new LoggerContext();

        expect(this.delegate.getLoggerContext("foo")).andReturn(context);
        this.delegate.configureDefaultContext(this.loggerContext);

        replay(this.delegate);

        this.delegatingContextSelector = new DelegatingContextSelector(this.loggerContext);
        DelegatingContextSelector.setDelegate(this.delegate);

        assertEquals(context, this.delegatingContextSelector.getLoggerContext("foo"));

        verify(this.delegate);
    }
}
