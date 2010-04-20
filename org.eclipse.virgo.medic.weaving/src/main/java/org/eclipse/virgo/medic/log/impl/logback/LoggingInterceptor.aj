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

package org.eclipse.virgo.medic.log.impl.logback;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public aspect LoggingInterceptor {
	
	private volatile LoggingListener loggingListener = new NoOpLoggingListener();
	
	pointcut withinLoggingInterception() : cflowbelow(within(LoggingInterceptor) && adviceexecution());
	
	pointcut filteringAndAppendingWithZeroOr3PlusParams(Logger logger, String fqcn, Marker marker, Level level, String message, Object[] params, Throwable throwable)
		: execution(private void ch.qos.logback.classic.Logger.filterAndLog_0_Or3Plus(String, Marker, Level, String, Object[], Throwable))
		  && this(logger)
		  && args(fqcn, marker, level, message, params, throwable)
		  && !withinLoggingInterception();
	
	pointcut filteringAndAppendingWithOneParam(Logger logger, String fqcn, Marker marker, Level level, String message, Object param, Throwable throwable)
		: execution(private void ch.qos.logback.classic.Logger.filterAndLog_1(String, Marker, Level, String, Object, Throwable))
	      && this(logger)
	      && args(fqcn, marker, level, message, param, throwable)
	      && !withinLoggingInterception();
	
	pointcut filteringAndAppendingWithTwoParams(Logger logger, String fqcn, Marker marker, Level level, String message, Object param1, Object param2, Throwable throwable)
		: execution(private void ch.qos.logback.classic.Logger.filterAndLog_2(String, Marker, Level, String, Object, Object, Throwable))
		 && this(logger)
		 && args(fqcn, marker, level, message, param1, param2, throwable)
		 && !withinLoggingInterception();
	
	before(Logger logger, String fqcn, Marker marker, Level level, String message, Object[] params, Throwable throwable) :
		filteringAndAppendingWithZeroOr3PlusParams(logger, fqcn, marker, level, message, params, throwable) {
		
		this.loggingListener.onLogging(logger, fqcn, marker, level, message, params, throwable);				
	}
	
	before(Logger logger, String fqcn, Marker marker, Level level, String message, Object param, Throwable throwable) :
		filteringAndAppendingWithOneParam(logger, fqcn, marker, level, message, param, throwable) {
		
		this.loggingListener.onLogging(logger, fqcn, marker, level, message, param, throwable);	
	}	
	
	before(Logger logger, String fqcn, Marker marker, Level level, String message, Object param1, Object param2, Throwable throwable) :
		filteringAndAppendingWithTwoParams(logger, fqcn, marker, level, message, param1, param2, throwable) {
		
		this.loggingListener.onLogging(logger, fqcn, marker, level, message, param1, param2, throwable);		
	}		
	
	public void setLoggingListener(LoggingListener listener) {
		if (listener == null) {
			listener = new NoOpLoggingListener();
		}
		this.loggingListener = listener;
	}
}
