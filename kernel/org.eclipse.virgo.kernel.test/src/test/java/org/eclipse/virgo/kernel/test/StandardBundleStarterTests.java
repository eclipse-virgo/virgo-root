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

package org.eclipse.virgo.kernel.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.virgo.nano.core.BundleStarter;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class StandardBundleStarterTests extends AbstractKernelIntegrationTest {

    private BundleStarter monitor;

    @Before
    public void before() {
        ServiceReference<BundleStarter> serviceReference = this.kernelContext.getServiceReference(BundleStarter.class);
        this.monitor = this.kernelContext.getService(serviceReference);
        assertNotNull(this.monitor);                
    }

    @Test
    public void success() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor/success").toURI().toString());
        
        TestSignal signal = new TestSignal();
        
        this.monitor.start(bundle, signal);
        
        signal.assertSuccessfulCompletionSignalled(5000);        
    }
    
    @Test
    public void successBlueprint() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor-blueprint/success").toURI().toString());
        
        TestSignal signal = new TestSignal();
        
        this.monitor.start(bundle, signal);
        
        signal.assertSuccessfulCompletionSignalled(5000);        
    }

    @Test
    public void failure() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor/failure").toURI().toString());
        
        TestSignal signal = new TestSignal();
        
        this.monitor.start(bundle, signal);
        
        signal.assertFailureSignalled(5000);
    }
    
    @Test
    public void failureBlueprint() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor-blueprint/failure").toURI().toString());
        
        TestSignal signal = new TestSignal();
        
        this.monitor.start(bundle, signal);
        
        signal.assertFailureSignalled(5000);
    }

    @Test
    public void nonDm() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor/nondm").toURI().toString());
        TestSignal signal = new TestSignal();
        
        this.monitor.start(bundle, signal);
        
        signal.assertSuccessfulCompletionSignalled(5000);
    }
    
    @Test
    public void signalDelayedSuccess() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor/delay").toURI().toString());
        TestSignal signal = new TestSignal();
        this.monitor.start(bundle, signal);
        
        signal.assertSuccessfulCompletionSignalled(20000);
        
    }
    
    @Test
    public void signalDelayedSuccessBlueprint() throws Exception {
        Bundle bundle = this.context.installBundle(new File("src/test/resources/monitor-blueprint/delay").toURI().toString());
        TestSignal signal = new TestSignal();
        this.monitor.start(bundle, signal);
        
        signal.assertSuccessfulCompletionSignalled(20000);
        
    }
}
