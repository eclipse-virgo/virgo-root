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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;
import org.osgi.framework.Version;
import org.springframework.core.io.ClassPathResource;


import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.SpringConfigServiceModelScanner;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardScopeServiceRepository;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;

/**
 */
public class SpringConfigServiceModelScannerTests {

    private static final String TEST_SCOPE = "test";

    @Test
    public void testServicesRegistered() throws Exception {

        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        String location = "scoping/simpleService.xml";
        run(repository, location);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Serializable.class.getName(), null));
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, Integer.class.getName(), null));
    }

    @Test
    public void testComplexService() throws Exception {

        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        String location = "scoping/complexService.xml";
        run(repository, location);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Serializable.class.getName(), null));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Serializable.class.getName(), "(foo=bar)"));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Appendable.class.getName(), null));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Appendable.class.getName(), "(foo=bar)"));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Appendable.class.getName(), "(org.eclipse.gemini.blueprint.bean.name=service)"));
    }

    private final void run(ScopeServiceRepository repository, String configLocation) throws Exception {
        ClassPathResource resource = new ClassPathResource(configLocation);
        SpringConfigServiceModelScanner scanner = new SpringConfigServiceModelScanner(TEST_SCOPE, repository, new MockEventLogger());
        scanner.scanConfigFile("bundle", Version.emptyVersion, configLocation, resource.getInputStream());
    }
}
