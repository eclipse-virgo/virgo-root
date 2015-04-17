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

import org.eclipse.virgo.util.jmx.ExceptionCleaner;

/**
 * This is an annotated @{@link MXBean} interface for testing the {@link ExceptionCleaner} aspect.
 * <p />
 *
 */
@MXBean
//FIXME Bug 463462 - Move back to test source folder when we know how to weave test classes
public interface JmxExceptionCleanerTestInterface {
    
    public void caughtMethod() throws Exception;
    
    public void anotherCaughtMethod();

}
