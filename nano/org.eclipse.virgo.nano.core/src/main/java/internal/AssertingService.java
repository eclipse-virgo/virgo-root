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

package internal;

import org.eclipse.virgo.nano.serviceability.NonNull;

// FIXME Bug 463462 - Move back to test source folder when we know how to weave test classes
public class AssertingService {

    public AssertingService() {
    }
    
    public AssertingService( @NonNull String a) {
    }
    
    public AssertingService(String a, @NonNull Integer b) {
    }
    
    public AssertingService(String a, Integer b, @NonNull Double c) {
    }
    
    public void test(@NonNull String a) {
    }

    public void test(@NonNull String a, @NonNull Integer b) {
    }
    
    public void test(String a, Integer b, @NonNull Double d) {
    }
}
