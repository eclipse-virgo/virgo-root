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

package org.eclipse.virgo.kernel.userregion.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;

import org.eclipse.virgo.nano.shim.scope.Scope;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.eclipse.virgo.kernel.userregion.internal.ServiceScopingStrategy;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 */
public class ServiceScopingStrategyTests {

    private static final String SCOPE_NAME = "application scope";

    private static final String CLASS_NAME = "Class";

    private static final String FILTER = "Filter";

    private ServiceScopingStrategy serviceScopingStrategy;

    private ScopeFactory scopeFactory;

    private ScopeServiceRepository scopeServiceRepository;

    private Scope globalScope;

    private ServiceReference<?> unscopedServiceReference;

    private StubBundleContext unscopedBundleContext;

    private Scope appScope;

    private ServiceReference<?> scopedServiceReference;

    private StubBundleContext scopedBundleContext;

    @Before
    public void setUp() throws Exception {
        this.scopeFactory = createMock(ScopeFactory.class);
        this.scopeServiceRepository = createMock(ScopeServiceRepository.class);
        this.serviceScopingStrategy = new ServiceScopingStrategy(this.scopeFactory, this.scopeServiceRepository);

        this.globalScope = createMock(Scope.class);
        expect(this.globalScope.isGlobal()).andReturn(true).anyTimes();
        expect(this.scopeFactory.getGlobalScope()).andReturn(this.globalScope).anyTimes();

        this.appScope = createMock(Scope.class);
        expect(this.appScope.isGlobal()).andReturn(false).anyTimes();
        expect(this.appScope.getScopeName()).andReturn(SCOPE_NAME).anyTimes();

        replay(this.globalScope, this.appScope);
    }

    private void setUpScopedBundleContext() {
        this.scopedBundleContext = new StubBundleContext();
        expect(this.scopeFactory.getBundleScope(eq(this.scopedBundleContext.getBundle()))).andReturn(this.appScope).anyTimes();
    }

    private void setUpScopedServiceReference() {
        this.scopedServiceReference = createMock(ServiceReference.class);
        expect(this.scopeFactory.getServiceScope(eq(this.scopedServiceReference))).andReturn(this.appScope).anyTimes();
    }

    private void setUpUnscopedBundleContext() {
        this.unscopedBundleContext = new StubBundleContext();
        expect(this.scopeFactory.getBundleScope(eq(this.unscopedBundleContext.getBundle()))).andReturn(this.globalScope).anyTimes();
    }

    private void setUpUnscopedServiceReference() {
        this.unscopedServiceReference = createMock(ServiceReference.class);
        expect(this.scopeFactory.getServiceScope(eq(this.unscopedServiceReference))).andReturn(this.globalScope).anyTimes();
    }

    @After
    public void tearDown() {
        verify(this.globalScope, this.appScope);
    }

    @Test
    public void testMatchesScopeUnscopedServiceUnscopedApplication() {
        setUpUnscopedServiceReference();
        setUpUnscopedBundleContext();
        replay(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
        assertTrue(this.serviceScopingStrategy.isPotentiallyVisible(this.unscopedServiceReference, this.unscopedBundleContext));
        verify(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
    }

    @Test
    public void testMatchesScopeScopedServiceScopedApplication() {
        setUpScopedServiceReference();
        setUpScopedBundleContext();
        replay(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
        assertTrue(this.serviceScopingStrategy.isPotentiallyVisible(this.scopedServiceReference, this.scopedBundleContext));
        verify(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
    }

    @Test
    public void testMatchesScopeScopedServiceUnscopedApplication() {
        setUpScopedServiceReference();
        setUpUnscopedBundleContext();
        replay(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
        assertFalse(this.serviceScopingStrategy.isPotentiallyVisible(this.scopedServiceReference, this.unscopedBundleContext));
        verify(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
    }

    @Test
    public void testMatchesScopeUnscopedServiceScopedApplication() {
        setUpUnscopedServiceReference();
        setUpScopedBundleContext();
        replay(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
        assertTrue(this.serviceScopingStrategy.isPotentiallyVisible(this.unscopedServiceReference, this.scopedBundleContext));
        verify(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
    }

    @Test
    public void testScopeReferencesUnscopedServiceUnscopedApplication() throws InvalidSyntaxException {
        setUpUnscopedServiceReference();
        setUpUnscopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(false).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.unscopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.unscopedBundleContext, CLASS_NAME, FILTER);
        assertTrue(references.contains(this.unscopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
    }

    @Test
    public void testScopeReferencesScopedServiceUnscopedApplication() throws InvalidSyntaxException {
        setUpScopedServiceReference();
        setUpUnscopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(false).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.scopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.unscopedBundleContext, CLASS_NAME, FILTER);
        assertFalse(references.contains(this.unscopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
    }

    @Test
    public void testScopeReferencesScopedServiceInModelScopedApplication() throws InvalidSyntaxException {
        setUpScopedServiceReference();
        setUpScopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(true).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.scopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.scopedBundleContext, CLASS_NAME, FILTER);
        assertTrue(references.contains(this.scopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
    }

    @Test
    public void testScopeReferencesScopedServiceNotInModelScopedApplication() throws InvalidSyntaxException {
        setUpScopedServiceReference();
        setUpScopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(false).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.scopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.scopedBundleContext, CLASS_NAME, FILTER);
        assertTrue(references.contains(this.scopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.scopedServiceReference);
    }

    @Test
    public void testScopeReferencesUnscopedUnshadowedServiceScopedApplication() throws InvalidSyntaxException {
        setUpUnscopedServiceReference();
        setUpScopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(false).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.unscopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.scopedBundleContext, CLASS_NAME, FILTER);
        assertTrue(references.contains(this.unscopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
    }

    @Test
    public void testScopeReferencesUnscopedShadowedServiceScopedApplication() throws InvalidSyntaxException {
        setUpUnscopedServiceReference();
        setUpScopedBundleContext();
        expect(this.scopeServiceRepository.scopeHasMatchingService(eq(SCOPE_NAME), eq(CLASS_NAME), eq(FILTER))).andReturn(true).anyTimes();
        replay(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);

        Collection<ServiceReference<?>> references = new ShrinkableSet(this.unscopedServiceReference);
        this.serviceScopingStrategy.scopeReferences(references, this.scopedBundleContext, CLASS_NAME, FILTER);
        assertFalse(references.contains(this.unscopedServiceReference));

        verify(this.scopeFactory, this.scopeServiceRepository, this.unscopedServiceReference);
    }
    
    /**
     * This test uses a collection that does not support addition in order to place the
     * same constraints on the implementation as the service registry find hook.
     */
    private static final class ShrinkableSet extends HashSet<ServiceReference<?>> {

        private static final long serialVersionUID = 1L;

        public ShrinkableSet(ServiceReference<?> e) {
            super();
            super.add(e);
        }

        @Override
        public boolean add(ServiceReference<?> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends ServiceReference<?>> c) {
            throw new UnsupportedOperationException();
        }
        
    }

}
