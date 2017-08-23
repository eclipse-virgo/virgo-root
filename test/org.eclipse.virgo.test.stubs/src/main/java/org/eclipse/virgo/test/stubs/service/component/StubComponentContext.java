/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.test.stubs.service.component;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

/**
 * 
 * A stub testing implementation of {@link ComponentContext} as defined in section 112.5.9 of the OSGi Service Platform Compendium
 * Specification.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public class StubComponentContext implements ComponentContext {

    public final String DEFAULT_PROP_KEY = "key";

    public final String DEFAULT_PROP_VALUE = "value";

    private final Dictionary<String, String> props = new Hashtable<String, String>();

    private final BundleContext bundleContext;

    public StubComponentContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        populateDefaultProperties();
    }
    
    /**
     * {@inheritDoc}
     */
    public Dictionary<String, String> getProperties() {
        return this.props;
    }

    private void populateDefaultProperties() {
        this.props.put(this.DEFAULT_PROP_KEY, this.DEFAULT_PROP_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public Object locateService(String name) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public Object locateService(String name, ServiceReference reference) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public Object[] locateServices(String name) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getUsingBundle() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public ComponentInstance getComponentInstance() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public void enableComponent(String name) {
    }

    /**
     * {@inheritDoc}
     */
    public void disableComponent(String name) {
    }

    /**
     * {@inheritDoc}
     */
    public ServiceReference getServiceReference() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
