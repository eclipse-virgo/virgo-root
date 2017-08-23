/*******************************************************************************
 * Copyright (c) 20012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console.stubs.objects;


public class MBean {

    public MBean(String mbeanName) {
    }
    
    public String get(String property) {
        return "value";
    }

}
