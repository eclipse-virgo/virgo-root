/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact;

import java.io.IOException;
import java.util.Properties;

/**
 * {@link ConfigInstallArtifact} is a marker interface to identify configuration install artifacts.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations of this interface must be thread safe.
 */
public interface ConfigInstallArtifact extends GraphAssociableInstallArtifact {
    
    Properties getProperties() throws IOException;

}
