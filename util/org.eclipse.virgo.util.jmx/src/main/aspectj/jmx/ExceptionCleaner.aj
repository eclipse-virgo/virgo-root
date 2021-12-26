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

package org.eclipse.virgo.util.jmx;

import org.slf4j.LoggerFactory;
import javax.management.MXBean;

/**
 * An aspect that catches all {@link Exception}s thrown through @{@link MXBean} annotated interfaces and filters them to
 * make sure they can be safely thrown to a JMX client. Safe means that there will be no types thrown that cannot be
 * loaded by the JMX client.
 * <p/>
 * This is accomplished by throwing a <em>cleaned</em> exception instead, after logging the original exception at error
 * level with the logger for the throwing class.
 * <p/>
 * A <em>cleaned</em> exception, <code>cleaned(t)</code>, is a new {@link RuntimeException} with message that of
 * <code>t</code> (prefixed with the class name of <code>t</code>), with cause <code>cleaned(t.getCause())</code>, and
 * with stack trace set to <code>t.getStackTrace()</code>. <br/>
 * <em>Notice that this definition is recursive; <code>cleaned(<b>null</b>)</code> is defined to be
 * <code><b>null</b></code>.</em>
 * <p/>
 * The {@link StackTraceElement StackTrace} from the original exception is attached to the new {@link RuntimeException}
 * for information purposes.
 * <p/>
 * (Moved from <code>com.springsource.kernel.model.management.internal</code> package and enhanced to catch
 * all @{@link MXBean} annotated interfaces.)
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 */
public aspect ExceptionCleaner {

    /**
     * All implementations of @{@link MXBean} annotated interface methods:
     */
    pointcut exposedViaJmx(Object o) : this(o) && execution(* (@MXBean *).*(..));

    /**
     * Catch {@link Exception}s and clean them and the nested causes (if any). {@link Throwable}s are trapped, so we can
     * advise methods that do not declare a throws, but {@link Exception}s are filtered out.
     */
    after(Object o) throwing(Exception e) : exposedViaJmx(o) {
        LoggerFactory.getLogger(o.getClass()).error("Exception filtered from JMX invocation", e);
        throw cleanException(e);
    }

    private RuntimeException cleanException(Throwable throwable) {
        Throwable cause = throwable.getCause();
        RuntimeException cleanCause = null;
        if (cause != null) {
            cleanCause = cleanException(cause);
        }

        RuntimeException rte = new RuntimeException(throwable.getClass().getName() + ": " + throwable.getMessage(), cleanCause);
        rte.setStackTrace(throwable.getStackTrace());

        return rte;
    }
}
