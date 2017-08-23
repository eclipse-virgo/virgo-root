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

package org.eclipse.virgo.util.osgi.manifest.internal;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;

abstract class BaseCompoundHeaderEntry extends BaseParameterised {
    
    BaseCompoundHeaderEntry(HeaderParser parser, String name) {
        super(parser);        
        this.name = name;         
    }
}
