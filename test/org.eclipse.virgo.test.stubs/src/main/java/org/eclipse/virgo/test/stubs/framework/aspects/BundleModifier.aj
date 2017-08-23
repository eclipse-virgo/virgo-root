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

import java.util.Date;

import org.eclipse.virgo.test.stubs.framework.StubBundle;

/**
 * Updates the <code>lastModified</code> time on a bundle when it's modified.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect BundleModifier {

    private pointcut modifyingMethod(StubBundle bundle) : this(bundle) && (
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.uninstall()) || 
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.reset()) ||
        execution(* org.eclipse.virgo.test.stubs.framework.StubBundle.update(..))
        );

    /**
     * Updates the modification timestamp on a {@link Bundle} when a modification is made to it
     * 
     * @param bundle the {@link Bundle} to modify
     */
    after(StubBundle bundle) : modifyingMethod(bundle) {
        bundle.setLastModified(new Date().getTime());
    }

}
