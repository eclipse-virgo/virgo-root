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

package org.eclipse.virgo.medic.log.logback;

import java.io.OutputStream;

import org.eclipse.virgo.medic.log.impl.LoggingPrintStreamWrapper;

import ch.qos.logback.core.ConsoleAppender;


/**
 * A special {@link ConsoleAppender} that is aware of re-routing of <code>System.out</code> and <code>System.err</code>
 * and will always use the original <code>System.out</code> and <code>System.err</code>, rather than the ones that are being rerouted.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 * @param <E> Event type being logged
 */
public class ReroutingAwareConsoleAppender<E> extends ConsoleAppender<E> {

    /**
     * This is responsible for ensuring the Appender is using the right output stream as it may have been wrapped.
     */
    @Override
    public void setOutputStream(OutputStream outputStream) {
        if (outputStream instanceof LoggingPrintStreamWrapper) {
            super.setOutputStream(((LoggingPrintStreamWrapper)outputStream).getOriginalPrintStream());
        } else {
            super.setOutputStream(outputStream);
        }
    }

}
