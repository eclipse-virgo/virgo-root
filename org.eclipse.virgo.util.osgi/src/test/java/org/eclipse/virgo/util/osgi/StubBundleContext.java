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

package org.eclipse.virgo.util.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * TODO Document StubBundleContext
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of StubBundleContext
 *
 */
public class StubBundleContext implements BundleContext {

    /**
     * {@inheritDoc}
     */
    public void addBundleListener(BundleListener arg0) {
        // System.out.println("1");
    }

    /**
     * {@inheritDoc}
     */
    public void addFrameworkListener(FrameworkListener arg0) {
        // System.out.println("2");
    }

    /**
     * {@inheritDoc}
     */
    public void addServiceListener(ServiceListener arg0) {
        // System.out.println("3");
    }

    /**
     * {@inheritDoc}
     */
    public void addServiceListener(ServiceListener arg0, String arg1) throws InvalidSyntaxException {
        // System.out.println("4");
    }

    /**
     * {@inheritDoc}
     */
    public Filter createFilter(String arg0) throws InvalidSyntaxException {
        // System.out.println("5");
        return new Filter() {

            public boolean match(ServiceReference<?> arg0) {
                return true;
            }

            public boolean match(Dictionary<String, ?> arg0) {
                return true;
            }

            public boolean matchCase(Dictionary<String, ?> arg0) {
                return true;
            }

            public boolean matches(Map<String, ?> map) {
                return true;
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    public ServiceReference<?>[] getAllServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
        // System.out.println("6");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        // System.out.println("7");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle(long arg0) {
        // System.out.println("8");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle[] getBundles() {
        // System.out.println("9");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public File getDataFile(String arg0) {
        // System.out.println("10");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getProperty(String arg0) {
        // System.out.println("11");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <S> S getService(ServiceReference<S> arg0) {
        // System.out.println("12");
        return (S) new ServiceObject();
    }

    /**
     * {@inheritDoc}
     */
    public ServiceReference<?> getServiceReference(String arg0) {
        // System.out.println("13");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ServiceReference<?>[] getServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
        // System.out.println("14");
        ServiceReference<?>[] refs = new ServiceReference[1];
        refs[0] = new ServiceReference<ServiceObject>() {

            public int compareTo(Object arg0) {
                return 0;
            }

            public Bundle getBundle() {
                return null;
            }

            public Object getProperty(String arg0) {
                return null;
            }

            public String[] getPropertyKeys() {
                return null;
            }

            public Bundle[] getUsingBundles() {
                return null;
            }

            public boolean isAssignableTo(Bundle arg0, String arg1) {
                return false;
            }

        };
        return refs;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle installBundle(String arg0) throws BundleException {
        // System.out.println("15");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle installBundle(String arg0, InputStream arg1) throws BundleException {
        // System.out.println("16");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ServiceRegistration<?> registerService(String[] arg0, Object arg1, Dictionary<String, ?> arg2) {
        // System.out.println("17");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ServiceRegistration<?> registerService(String arg0, Object arg1, Dictionary<String, ?> arg2) {
        // System.out.println("18");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void removeBundleListener(BundleListener arg0) {
        // System.out.println("19");
    }

    /**
     * {@inheritDoc}
     */
    public void removeFrameworkListener(FrameworkListener arg0) {
        // System.out.println("20");
    }

    /**
     * {@inheritDoc}
     */
    public void removeServiceListener(ServiceListener arg0) {
        // System.out.println("21");
    }

    /**
     * {@inheritDoc}
     */
    public boolean ungetService(ServiceReference<?> arg0) {
        // System.out.println("22");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle(String location) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        return null;
    }

    @Override
    public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> arg0) {
        return null;
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> arg0, ServiceFactory<S> arg1, Dictionary<String, ?> arg2) {
        return null;
    }

}
