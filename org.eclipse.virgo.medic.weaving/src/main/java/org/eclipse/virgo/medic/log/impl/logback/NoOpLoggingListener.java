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

public class NoOpLoggingListener implements LoggingListener {

	public void onLogging(Logger logger, String fqcn, Marker marker,
		Level level, String message, Object param, Throwable throwable) {
	}

	public void onLogging(Logger logger, String fqcn, Marker marker,
		Level level, String message, Object param1, Object param2,
		Throwable throwable) {
	}

	public void onLogging(Logger logger, String fqcn, Marker marker,
		Level level, String message, Object[] params, Throwable throwable) {
	}
}
