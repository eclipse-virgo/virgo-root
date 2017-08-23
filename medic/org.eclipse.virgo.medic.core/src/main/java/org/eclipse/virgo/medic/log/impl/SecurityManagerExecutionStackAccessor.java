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

public final class SecurityManagerExecutionStackAccessor extends SecurityManager implements ExecutionStackAccessor {

    public Class<?>[] getExecutionStack() {
        Class<?>[] classes = super.getClassContext();
        Class<?>[] executionStack = new Class<?>[classes.length - 1];

        System.arraycopy(classes, 1, executionStack, 0, executionStack.length);

        return executionStack;
    }
}
