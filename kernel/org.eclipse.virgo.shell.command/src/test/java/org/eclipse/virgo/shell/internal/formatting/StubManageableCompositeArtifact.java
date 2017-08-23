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

package org.eclipse.virgo.shell.internal.formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;

public class StubManageableCompositeArtifact implements ManageableCompositeArtifact {

    private volatile boolean atomicCalled = false;

    private volatile boolean scopedCalled = false;

    private volatile boolean namedCalled = false;

    private volatile boolean stateCalled = false;

    private volatile boolean typeCalled = false;

    private volatile boolean versionCalled = false;

    private volatile boolean regionCalled = false;

    private volatile boolean startCalled = false;

    private volatile boolean stopCalled = false;

    private volatile boolean uninstallCalled = false;

    private volatile boolean refreshCalled = false;

    private volatile boolean dependentsCalled = false;

    private volatile boolean propertiesCalled = false;
    
    private volatile boolean shouldRefreshSucceed = true;
	
    private volatile String state = "ACTIVE";
    
    public void setShouldRefreshSucceed(boolean shouldRefreshSucceed) {
		this.shouldRefreshSucceed = shouldRefreshSucceed;
	}

    public boolean isAtomic() {
        this.atomicCalled = true;
        return false;
    }

    public boolean isScoped() {
        this.scopedCalled = true;
        return false;
    }

    public ObjectName[] getDependents() {
        this.dependentsCalled = true;

        List<ObjectName> objectNames = new ArrayList<ObjectName>();
        try {
            objectNames.add(new ObjectName("test:artifact-type=test,name=com.springsource.test2,version=0.0.0"));
            objectNames.add(new ObjectName("test:artifact-type=test,name=com.springsource.test3,version=0.0.0"));
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }

        return objectNames.toArray(new ObjectName[objectNames.size()]);
    }

    public String getName() {
        this.namedCalled = true;
        return "com.springsource.testName";
    }

    public Map<String, String> getProperties() {
        this.propertiesCalled = true;

        Map<String, String> properties = new HashMap<String, String>(2);
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        return properties;
    }

    public String getState() {
        this.stateCalled = true;
        return this.state;
    }

    public String getRegion() {
        this.regionCalled = true;
        return "testRegion";
    }
    
    public StubManageableCompositeArtifact setState(String state) {
        this.state = state;
        return this;
    }

    public String getType() {
        this.typeCalled = true;
        return "testType";
    }

    public String getVersion() {
        this.versionCalled = true;
        return "0.0.0";
    }

    public void start() {
        this.startCalled = true;
    }

    public void stop() {
        this.stopCalled = true;
    }

    public void uninstall() {
        this.uninstallCalled = true;
    }

    public boolean refresh() {
        this.refreshCalled = true;
        return this.shouldRefreshSucceed;
    }

    public boolean getAtomicCalled() {
        return atomicCalled;
    }

    public boolean getScopedCalled() {
        return scopedCalled;
    }

    public boolean getNamedCalled() {
        return namedCalled;
    }

    public boolean getStateCalled() {
        return stateCalled;
    }

    public boolean getTypeCalled() {
        return typeCalled;
    }

    public boolean getRegionCalled() {
        return regionCalled;
    }

    public boolean getVersionCalled() {
        return versionCalled;
    }

    public boolean getStartCalled() {
        return startCalled;
    }

    public boolean getStopCalled() {
        return stopCalled;
    }

    public boolean getUninstallCalled() {
        return uninstallCalled;
    }

    public boolean getRefreshCalled() {
        return refreshCalled;
    }

    public boolean getDependentsCalled() {
        return dependentsCalled;
    }

    public boolean getPropertiesCalled() {
        return propertiesCalled;
    }

}
