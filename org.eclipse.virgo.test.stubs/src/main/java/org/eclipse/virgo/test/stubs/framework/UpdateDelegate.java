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

package org.eclipse.virgo.test.stubs.framework;

import java.io.InputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * An interface to allow delegation of calls to {@link Bundle#update()} and {@link Bundle#update(InputStream)} to be
 * serviced in a test environment.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface UpdateDelegate {

    /**
     * @param bundle The {@link StubBundle} is being called upon
     * @throws BundleException
     * @see Bundle#update()
     */
    void update(StubBundle bundle) throws BundleException;

}
