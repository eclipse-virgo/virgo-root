/*******************************************************************************
 * Copyright (c) 2014 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.web.enterprise.security;

import java.lang.reflect.Field;
import java.security.Principal;

import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertFalse;

/**
 * The class StandardSecurityService has a method enterWebApp,
 * which sets a wrapper object. It is used in isCallerInRole and then removed in exitWebApp. The problem is that
 * if a second thread calls enterWebApp before the first thread has called exitWebApp, it overwrites the
 * wrapper object with its own. An if the second thread calls exitWebApp and after that the first calls isCallerInRole,
 * a NullPointerException is thrown because the second thread's exitWebApp has removed the wrapper. This is fixed by
 * using a thread local to store the wrapper.
 * 
 *  The test simulates the above situation. The test starts two threads. The first calls enterWebApp and checks that 
 *  its wrapper is correctly set. Then the second thread calls the enterWebApp, checks that its wrapper is correctly set
 *  and then the first thread checks again that its wrapper is not overwritten and is correct. Then the first calls exitWebApp
 *  and checks that its wrapper is removed. Then the second thread checks that its wrapper is present and correct, then calls 
 *  exitWebApp and checks that its wrapper is removed.
 *
 */
public class StandardSecurityServiceTest {
	
	private static final long TIMEOUT = 10000;
	
	@Test
	public void testWrapper() throws Exception {
		StandardSecurityService securityService = new StandardSecurityService();
		
		Wrapper wrapper1 = createMock(Wrapper.class);
		Principal principal1 = createMock(Principal.class);
		Realm realm1 = createMock(Realm.class);
		EasyMock.expect(wrapper1.getRealm()).andReturn(realm1);
		EasyMock.expect(wrapper1.getName()).andReturn("wrapper1").anyTimes();
		
		Wrapper wrapper2 = createMock(Wrapper.class);
		Principal principal2 = createMock(Principal.class);
		Realm realm2 = createMock(Realm.class);
		EasyMock.expect(wrapper2.getRealm()).andReturn(realm2);
		EasyMock.expect(wrapper2.getName()).andReturn("wrapper2").anyTimes();
		
		EasyMock.replay(wrapper1, wrapper2);
		
		ApplicationThread applicationThread1 = new ApplicationThread(securityService, wrapper1, "user1");
		ApplicationThread applicationThread2 = new ApplicationThread(securityService, wrapper2, "user2");
		applicationThread1.setCoworkerThread(applicationThread2);
		applicationThread1.setName("Thread-1");
		applicationThread2.setCoworkerThread(applicationThread1);
		applicationThread2.setName("Thread-2");
		
		applicationThread1.setReady();
		applicationThread1.start();		
		applicationThread2.start();
		
		applicationThread1.join();
		applicationThread2.join();
		
		String errorMessage1 = applicationThread1.getErrorMessage() == null ? "no error" : applicationThread1.getErrorMessage();
		String errorMessage2 = applicationThread2.getErrorMessage() == null ? "no error" : applicationThread2.getErrorMessage();
		assertFalse("ApplicationThread1 error message: " + errorMessage1 + "; ApplicationThread2 error message: " + errorMessage2, applicationThread1.isError() || applicationThread2.isError());
	}
	
	private static class ApplicationThread extends Thread {
		private StandardSecurityService service;
		private ApplicationThread coworkerThread;
		private Wrapper wrapper;
		private String runAs;
		private boolean isReady = false;
		private boolean isError = false;
		private String errorMessage = null;
		
		ApplicationThread(StandardSecurityService serv, Wrapper wrap, String runAs) {
			service = serv;
			wrapper = wrap;
			this.runAs = runAs;
		}
		
		public void run() {
			if (!waitForOtherThread()){
				return;
			}
			Object webAppState = service.enterWebApp(wrapper, null, runAs);
			boolean isExpectedWrapper = checkWrapper(wrapper);
			isReady = false;
			coworkerThread.setReady();
			if (!isExpectedWrapper) {
				return; 
			}
			
			if (!waitForOtherThread()){
				return;
			}
			isExpectedWrapper = checkWrapper(wrapper);
			if (!isExpectedWrapper) {
				isReady = false;
				coworkerThread.setReady();
				return; 
			}
			
			service.exitWebApp(webAppState);
			checkWrapper(null);
			isReady = false;
			coworkerThread.setReady();
		}
		
		private boolean waitForOtherThread() {
			long start = System.currentTimeMillis();
			while (!isReady && System.currentTimeMillis() - start < TIMEOUT) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!isReady) {
				isError = true;
				errorMessage = "Timed out waiting for notification from the other thread";
				return false;
			}
			return true;
		}
		
		private boolean checkWrapper(Wrapper expectedWrapper) {
			Wrapper currentWrapper;
			boolean result;
			try {
				currentWrapper = getWrapper();
				result = (expectedWrapper == currentWrapper);
				if (!result) {
					isError = true;
					errorMessage = "epxected [" + (expectedWrapper.getName()) + "] but found [" + (currentWrapper.getName()) + "] instead";
				}
				
				return result;
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
				isError = true;
				errorMessage = e.getMessage();
			}
			return false;
		}
		
		void setCoworkerThread(ApplicationThread thread) {
			coworkerThread = thread;
		}
		
		void setReady() {
			isReady = true;
		}
		
		boolean isError() {
			return isError;
		}
		
		String getErrorMessage() {
			return errorMessage;
		}
		
		private Wrapper getWrapper() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
			Field wrapperField = StandardSecurityService.class.getDeclaredField("wrapper"); 
			wrapperField.setAccessible(true);
			Object wrapper = wrapperField.get(service);
			
			if (wrapper == null) {
				return (Wrapper) wrapper;
			} else if (wrapper instanceof Wrapper) {
				return (Wrapper) wrapper;
			} else {
				ThreadLocal<Wrapper> wrapperLocal = (ThreadLocal<Wrapper>) wrapper; 
				return wrapperLocal.get();
			}
		}
	}
}


