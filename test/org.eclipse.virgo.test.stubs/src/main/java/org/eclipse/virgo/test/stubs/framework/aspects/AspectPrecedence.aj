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

package org.eclipse.virgo.test.stubs.framework.aspects;

/**
 * An aspect that defines the precendence of the aspects in this project
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect AspectPrecedence {

    declare precedence: org.eclipse.virgo.test.stubs.framework.aspects.Valid*,  *, org.eclipse.virgo.test.stubs.framework.aspects.*Events;

}
