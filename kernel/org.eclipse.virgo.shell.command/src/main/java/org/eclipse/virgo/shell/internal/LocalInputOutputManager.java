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

package org.eclipse.virgo.shell.internal;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.medic.log.DelegatingPrintStream;


/**
 * <p>
 * LocalInputOutputManager can grab the system input and output streams from the service registry and block and release them
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * LocalInputOutputManager is thread safe
 *
 */
public final class LocalInputOutputManager {
    
    private static final String SERVICE_FILTER_SYSERR_DELEGATE = "(org.eclipse.virgo.medic.log.printStream=delegating.System.err)";

    private static final String SERVICE_FILTER_SYSOUT_DELEGATE = "(org.eclipse.virgo.medic.log.printStream=delegating.System.out)";
    
    private final InputStream in;
    
    private final PrintStream out;
    
    private final PrintStream err;         
    
    private final DelegatingPrintStream delegatingSysOut;
    
    private final DelegatingPrintStream delegatingSysErr;
    
    public LocalInputOutputManager(BundleContext bundleContext) {
        this.in = new FileInputStream(FileDescriptor.in);
        
        this.out = getPrintStreamFromServiceRegistry(bundleContext, "(org.eclipse.virgo.medic.log.printStream=System.out)");
        this.err = getPrintStreamFromServiceRegistry(bundleContext, "(org.eclipse.virgo.medic.log.printStream=System.err)");
        
        this.delegatingSysOut = getDelegatingPrintStreamFromServiceRegistry(bundleContext,SERVICE_FILTER_SYSOUT_DELEGATE);
        this.delegatingSysErr = getDelegatingPrintStreamFromServiceRegistry(bundleContext,SERVICE_FILTER_SYSERR_DELEGATE);            
    }
    
    /**
     * 
     */
    public void grabSystemIO() {
        this.delegatingSysOut.setDelegate(null);
        this.delegatingSysErr.setDelegate(null);                
    }
    
    /**
     * 
     */
    public void releaseSystemIO() {
        this.delegatingSysOut.setDelegate(this.out);
        this.delegatingSysErr.setDelegate(this.err);        
    }

    /**
     * Get the original {@link PrintStream} for the local command line
     * @return PrintStream
     */
    public PrintStream getErr() {
        return err;
    }

    /**
     * Get the original {@link PrintStream} for the local command line
     * @return PrintStream
     */
    public PrintStream getOut() {
        return out;
    }

    /**
     * Get the original {@link InputStream} for the local command line
     * @return InputStream
     */
    public InputStream getIn() {
        return in;
    }

    private static PrintStream getPrintStreamFromServiceRegistry(BundleContext bundleContext, String filter) {
        return (PrintStream) getService(PrintStream.class, bundleContext, filter);
    }
    
    private static DelegatingPrintStream getDelegatingPrintStreamFromServiceRegistry(BundleContext bundleContext, String filter) {
        return (DelegatingPrintStream) getService(DelegatingPrintStream.class, bundleContext, filter);
    }
    
    private static Object getService(Class<?> clazz, BundleContext bundleContext, String filter) {
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = bundleContext.getServiceReferences(clazz.getName(), filter);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException", e);
        }
        if (serviceReferences != null && serviceReferences.length > 0) {
            return bundleContext.getService(serviceReferences[0]);
        } else {
            return null;
        }
    }    
}
