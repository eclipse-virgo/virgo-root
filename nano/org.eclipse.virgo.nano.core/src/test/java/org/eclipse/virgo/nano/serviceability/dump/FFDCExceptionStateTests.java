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

package org.eclipse.virgo.nano.serviceability.dump;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.eclipse.virgo.nano.serviceability.dump.FFDCExceptionState;
import org.junit.Test;


public class FFDCExceptionStateTests {
	
	@Test
	public void seen() {
		Throwable throwable = new Throwable();
		assertFalse(FFDCExceptionState.seen(throwable));
		FFDCExceptionState.record(throwable);
		assertTrue(FFDCExceptionState.seen(throwable));
	}
	
	@Test
	public void multithreadedSeen() {
		final Throwable throwable = new Throwable();
		FFDCExceptionState.record(throwable);
		assertTrue(FFDCExceptionState.seen(throwable));
		SeenChecker checker = new SeenChecker(throwable);
		checker.start();
		assertFalse(checker.seen());
	}
	
	@Test
	public void seenAsNestedCause() {
		Throwable nested = new Throwable();
		FFDCExceptionState.record(nested);
		Throwable root = new Throwable(nested);
		assertTrue(FFDCExceptionState.seen(root));
	}
	
	private class SeenChecker extends Thread {
		
		private final Throwable throwable;
		private volatile boolean seen;
		private final CountDownLatch latch;
		
		public SeenChecker(Throwable throwable) {
			this.throwable = throwable;
			this.latch = new CountDownLatch(1);
		}
		
		public boolean seen() {
			boolean awaiting = true;
			while (awaiting) {
				try {
				    this.latch.await();
				    awaiting = false;
				} catch (InterruptedException ie) {
					
				}
			}
			return this.seen;
		}
		
		public void run() {
			seen = FFDCExceptionState.seen(throwable);
			latch.countDown();
		}
	}
}
