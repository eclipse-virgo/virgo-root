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

package org.eclipse.virgo.medic.log.impl;

public interface ExecutionStackAccessor {

    /**
     * Returns the current execution stack as an array of {@link Class Classes}. The first elements in the array is the
     * class of this method's caller, the second element is that method's caller, and so on.
     * 
     * @return the execution stack
     */
    Class<?>[] getExecutionStack();
}
