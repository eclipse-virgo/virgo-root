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

package org.eclipse.virgo.kernel.osgi.framework;

import java.lang.instrument.ClassFileTransformer;

/**
 * Provides an interface onto <code>ClassLoaders</code> that support the use of {@link ClassFileTransformer
 * ClassFileTransformers} for load-time weaving.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be threadsafe.
 * 
 */
public interface InstrumentableClassLoader {

    /**
     * Adds the supplied {@link ClassFileTransformer} to the {@link ClassLoader}. All subsequent class loads will pass
     * through the newly added <code>ClassFileTransformer</code>.
     * 
     * @param transformer the <code>ClassFileTransformer</code>.
     */
    void addClassFileTransformer(ClassFileTransformer transformer);

    /**
     * Creates a throw away {@link ClassLoader} that can be used to explore and introspect on types that will be loaded
     * by this <code>ClassLoader</code>.
     * 
     * @return the throw away <code>ClassLoader</code>.
     */
    ClassLoader createThrowAway();

    /**
     * Queries whether this <code>ClassLoader</code> has been {@link #addClassFileTransformer(ClassFileTransformer)
     * instrumented}.
     * 
     * @return <code>true</code> if instrumented, otherwise <code>false</code>.
     */
    boolean isInstrumented();

    /**
     * Gets the number of {@link ClassFileTransformer ClassFileTransformers} that have been added to this
     * <code>ClassLoader</code>.
     * 
     * @return the number of <code>ClassFileTransformers</code>.
     */
    int getClassFileTransformerCount();
}
