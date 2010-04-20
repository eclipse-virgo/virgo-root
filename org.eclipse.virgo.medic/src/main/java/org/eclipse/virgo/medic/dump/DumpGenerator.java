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

package org.eclipse.virgo.medic.dump;

import java.util.Map;

/**
 * A <code>DumpGenerator</code> can be used to request the generation of a dump. A
 * <code>DumpGenerator</code> instance can be obtained from the service registry.
 * <p />
 * 
 * When a dump is requested, the <code>DumpGenerator</code> will, by default, provide all {@link DumpContributor}s
 * that are known to it with an opportunity to contribute to the dump. <code>DumpContributor</code>
 * instances are made known to a {@link DumpGenerator} by publishing them in the service registry.
 * <p />
 * 
 * A {@link DumpGenerator} can be configured to exclude {@link DumpContributor}s from contributing to dumps
 * with a particular cause. <br/>
 * TODO Expand the documentation on contributor exclusion
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * <code>DumpGenerator</code>s must be thread-safe.
 */
public interface DumpGenerator {

    /**
     * Generates a dump
     * 
     * @param cause the cause of the dump
     * @param throwables {@link Throwable} instances to be associated with the dump
     */
    void generateDump(String cause, Throwable... throwables);

    /**
     * Generates a dump, with additional context.
     * 
     * @param cause the cause of the dump
     * @param context additional context that can be used by the {@link DumpContributor}s
     * @param throwables {@link Throwable} instances to be associated with the dump
     */
    void generateDump(String cause, Map<String, Object> context, Throwable... throwables);
}
