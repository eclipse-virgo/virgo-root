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

package org.eclipse.virgo.medic.eventlog.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import org.junit.Test;
import org.eclipse.virgo.medic.eventlog.impl.LocaleResolver;
import org.eclipse.virgo.medic.eventlog.impl.MessageResolver;
import org.eclipse.virgo.medic.eventlog.impl.PropertyResourceBundleResolver;
import org.eclipse.virgo.medic.eventlog.impl.StandardMessageResolver;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

public class StandardMessageResolverTests {

    private final LocaleResolver localeResolver = createMock(LocaleResolver.class);

    private final PropertyResourceBundleResolver resourceBundleResolver = createMock(PropertyResourceBundleResolver.class);

    private final StubBundle primaryBundle = new StubBundle();

    private final StubBundle secondaryBundle = new StubBundle();

    private final MessageResolver messageResolver = new StandardMessageResolver(this.localeResolver, this.resourceBundleResolver, this.primaryBundle,
        this.secondaryBundle);

    @Test
    public void resolve() throws FileNotFoundException, IOException {
        try (InputStream messageProperties = new FileInputStream("src/test/resources/messages.properties")) {
            expect(this.localeResolver.getLocale()).andReturn(Locale.GERMAN);
            expect(this.resourceBundleResolver.getResourceBundles(this.primaryBundle, "EventLogMessages_de.properties")).andReturn(
                Arrays.asList(new PropertyResourceBundle(messageProperties)));
            replay(this.localeResolver, this.resourceBundleResolver);
            assertEquals("Bar", this.messageResolver.resolveLogEventMessage("ABC123"));
            verify(this.localeResolver, this.resourceBundleResolver);
        }
    }

    @Test
    public void resolveWithLocale() throws FileNotFoundException, IOException {
        try (InputStream messageProperties = new FileInputStream("src/test/resources/messages.properties")) {
            expect(this.resourceBundleResolver.getResourceBundles(this.primaryBundle, "EventLogMessages_it.properties")).andReturn(
                Arrays.asList(new PropertyResourceBundle(messageProperties)));
            replay(this.localeResolver, this.resourceBundleResolver);
            assertEquals("Bar", this.messageResolver.resolveLogEventMessage("ABC123", Locale.ITALIAN));
            verify(this.localeResolver, this.resourceBundleResolver);
        }
    }

    @Test
    public void resolveWithMissingResourceBundle() {
        expect(this.resourceBundleResolver.getResourceBundles(eq(this.primaryBundle), isA(String.class))).andReturn(
            Collections.<PropertyResourceBundle> emptyList()).atLeastOnce();
        replay(this.localeResolver, this.resourceBundleResolver);
        assertNull(this.messageResolver.resolveLogEventMessage("ABC123", Locale.FRANCE));
        verify(this.localeResolver, this.resourceBundleResolver);
    }
}
