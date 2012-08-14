/*
 * Copyright (c) 2010 Olivier Girardot
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Olivier Girardot - initial contribution
 */

package org.eclipse.virgo.nano.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * This class is for testing the {@link Signal} implementation {@link BlockingSignal}.
 *
 */
public final class BlockingSignalTests {
	
	@Test
	public void signalCompletionAfterNoFailure() throws Throwable {
		final BlockingSignal signal = new BlockingSignal();
		ExceptionCatcherThread testThread = new ExceptionCatcherThread(new Runnable(){
			public void run() {
				try {
					// awaiting for signal completion flag
					boolean returnSignal = 
						signal.awaitCompletion(1000, TimeUnit.SECONDS);
					assertTrue(returnSignal);
				} catch (FailureSignalledException e) {
					// this code should not be reached
					fail();
				}
			}
		});
		testThread.start();
		signal.signalSuccessfulCompletion();
        testThread.join(10);
        assertFalse("Test thread still alive after delay.", testThread.isAlive());
		testThread.rethrowUncaughtExceptions();
	}
	
	@Test
	public void signalCompletionFailsAfterWaitingExceeded() throws Throwable {
		final BlockingSignal signal = new BlockingSignal();
		ExceptionCatcherThread testThread = new ExceptionCatcherThread(new Runnable(){
			public void run() {
				try {
					// awaiting for signal completion flag
					boolean returnSignal = 
						signal.awaitCompletion(1, TimeUnit.MILLISECONDS);
					assertFalse(returnSignal);
				} catch (FailureSignalledException e) {
					// this code should not be reached
					fail();
				}
			}
		});
		testThread.start();
		Thread.sleep(100);
		signal.signalSuccessfulCompletion();
		testThread.join(10);
		assertFalse("Test thread still alive after delay.", testThread.isAlive());
		testThread.rethrowUncaughtExceptions();
	}
	
	@Test
	public void signalCompletionFailsAfterFailureNotifiedToSignal() throws Throwable {
		final BlockingSignal signal = new BlockingSignal();
		final Throwable dummyEx = new Exception("Dummy cause");
		ExceptionCatcherThread testThread = new ExceptionCatcherThread(new Runnable(){
			public void run() {
				try {
					// awaiting for signal completion flag (not storing result, as the code should fail)
					signal.awaitCompletion(1, TimeUnit.SECONDS);
					
					// this code should not be reached
					// an exception being sent before
					fail();
				} catch (FailureSignalledException e) {
					// We'll check that we actually refer to the correct cause
					assertSame("Signal failure has incorrect cause.", dummyEx, e.getCause());
				}
			}
		});
		testThread.start();
		signal.signalFailure(dummyEx);
        testThread.join(10);
        assertFalse("Test thread still alive after delay.", testThread.isAlive());
		testThread.rethrowUncaughtExceptions();
	}

	/**
	 * Special thread designed to record uncaught exceptions
	 * and re-throw the first of them on demand.
	 */
	private class ExceptionCatcherThread extends Thread{
		private final Vector<Throwable> uncaughtExceptions = new Vector<Throwable>();
		
		public ExceptionCatcherThread(Runnable r) {
			super(r);
			this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					uncaughtExceptions.add(e);
				}
			});
		}
		
		public void rethrowUncaughtExceptions() throws Throwable {
			if (!uncaughtExceptions.isEmpty())
				throw uncaughtExceptions.firstElement();
		}
	}
}