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

import javax.management.MXBean;

/**
 * This is an interface that extends a @{@link MXBean} annotated interface for testing the {@link ExceptionCleaner} aspect.
 * <p />
 */
public interface JmxExceptionCleanerExtensionTestInterface extends JmxExceptionCleanerTestInterface {

    void unCaughtMethod() throws Exception;
}
