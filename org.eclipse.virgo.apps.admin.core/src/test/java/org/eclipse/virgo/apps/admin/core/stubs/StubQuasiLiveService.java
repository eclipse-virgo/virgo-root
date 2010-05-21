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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;

/**
 */
final public class StubQuasiLiveService implements QuasiLiveService {

    private long serviceId;
    
    private QuasiLiveBundle provider;

    public StubQuasiLiveService(long serviceId, QuasiLiveBundle provider) {
        this.serviceId = serviceId;
        this.provider = provider;
    }
    
    public List<QuasiLiveBundle> getConsumers() {
        return new ArrayList<QuasiLiveBundle>();
    }

    public Map<String, String> getPropertyMap() {
        return new HashMap<String, String>();
    }

    public QuasiLiveBundle getProvider() {
        return provider;
    }

    public long getServiceId() {
        return this.serviceId;
    }

    public int compareTo(QuasiLiveService o) {
        return 0;
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, Object> getProperties() {
        return new HashMap<String, Object>();
    }
    
}
