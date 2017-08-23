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

package org.eclipse.virgo.util.osgi.manifest.parse;

/**
 * <p>
 * This is a stand-in logging service just for parsing manifests. It can run before the normal serviceability services
 * have come up and allow errors to be correctly reported from the start of the server bootstrap process.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface should be Thread Safe
 * 
 */
public interface ParserLogger {

    /**
     * Deal with a exception at level 'Error' logging it with what ever services are available.
     * 
     * @param re
     * @param item
     */
    void outputErrorMsg(Exception re, String item);

    /**
     * Reports if this logging service has been used or not. If it has been used the parsing activities that have taken
     * place since this logger was created or reset have encountered problems of some kind.
     * 
     * @return String array of all log event messages since the parser was created or cleaned
     */
    String[] errorReports();

}
