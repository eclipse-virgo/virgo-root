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

package org.eclipse.virgo.nano.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.service.event.Event;

import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.core.internal.BundleStartTracker;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;


/**
 */
public class BundleStartTrackerTests {
    
    @Test
    public void startOfBundleThatIsNotPoweredBySpringDm() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.setBundleContext(bundleContext);
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        bundle.start();
        
        assertEquals(1, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
        
        assertEquals(Bundle.ACTIVE, bundle.getState());
    }
    
    @Test
    public void startOfBundleThatIsPoweredBySpringDm() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.failures.size());
        
        bundle.start();
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));
        
        assertEquals(1, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    @Test
    public void startOfLazyActivationBundle() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Bundle-ActivationPolicy", "lazy");
        bundle.setBundleContext(bundleContext);
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        bundleStartTracker.trackStart(bundle, signal);
        
        List<BundleListener> bundleListeners = bundleContext.getBundleListeners();
        for(BundleListener listener : bundleListeners){
        	listener.bundleChanged(new BundleEvent(BundleEvent.LAZY_ACTIVATION, bundle));
        }
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
        
        for(BundleListener listener : bundleListeners){
        	listener.bundleChanged(new BundleEvent(BundleEvent.STOPPED, bundle));
        }

        assertEquals(0, signal.successCount);
        assertEquals(1, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    @Test
    public void applicationContextCreationFailureOfBundleThatIsPoweredBySpringDm() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
        
        bundle.start();
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        Exception failure = new Exception();
        properties.put("exception", failure);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(1, signal.failures.size());
        
        assertTrue(signal.failures.contains(failure));
    }
    
    @Test
    public void trackingOfSpringDmPoweredBundleThatAlreadyCreatedItsContainer() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));
                
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(1, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    @Test
    public void trackingOfSpringDmPoweredBundleThatHasAlreadyFailedToCreateItsApplicationContext() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        Exception failure = new Exception();
        properties.put("exception", failure);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
                
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(1, signal.failures.size());
        assertTrue(signal.failures.contains(failure));       
    }
    
    @Test
    public void signalIsOnlyDrivenOnceEvenWithMultipleEventsForStartOfBundleThatIsPoweredBySpringDm() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));
        
        assertEquals(1, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    @Test
    public void signalIsOnlyDrivenOnceEvenWithMultipleEventsForApplicationContextCreationFailureOfBundleThatIsPoweredBySpringDm() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        Exception failure = new Exception();
        properties.put("exception", failure);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(1, signal.failures.size());
        
        assertTrue(signal.failures.contains(failure));
    }
    
    @Test
    public void createdStateIsCleanedUpWhenBundleIsStopped() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.setBundleContext(bundleContext);
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        
        bundle.start();        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/CREATED", properties));        
        bundle.stop();
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    @Test
    public void failureStateIsCleanedUpWhenBundleIsStopped() throws BundleException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundle bundle = new StubBundle();
        bundle.setBundleContext(bundleContext);
        bundle.addHeader("Spring-Context", "foo");
        
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(bundleContext);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        properties.put("exception", new Exception());
        
        bundle.start();        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));        
        bundle.stop();
        
        UnitTestSignal signal = new UnitTestSignal();
        
        bundleStartTracker.trackStart(bundle, signal);
        
        assertEquals(0, signal.successCount);
        assertEquals(0, signal.abortCount);
        assertEquals(0, signal.failures.size());
    }
    
    private static final class UnitTestSignal implements AbortableSignal {
        
        private final List<Throwable> failures = new ArrayList<Throwable>();

        private int successCount = 0;
        private int abortCount = 0;
                
        /** 
         * {@inheritDoc}
         */
        public void signalFailure(Throwable cause) {            
            this.failures.add(cause);
        }

        /** 
         * {@inheritDoc}
         */
        public void signalSuccessfulCompletion() {
            successCount++;            
        }

        /** 
         * {@inheritDoc}
         */
		public void signalAborted() {
			abortCount++;
		}        
    }
}
