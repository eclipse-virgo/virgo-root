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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;
import org.eclipse.virgo.kernel.shell.model.helper.RamAccessorHelper;

/**
 */
final public class StubRamAccessorHelper implements RamAccessorHelper {

    private String lastCalled;

    public String getLastMethodCalled() {
        return this.lastCalled;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ArtifactAccessorPointer> getAllArtifactsOfType(String type) {
        this.lastCalled = "getAllArtifactsOfType";
        return new ArrayList<ArtifactAccessorPointer>();
    }

    /** 
     * {@inheritDoc}
     */
    public ArtifactAccessor getArtifact(String type, String name, String version, String region) {
        this.lastCalled = "getArtifact";
        return new StubArtifactAccessorAndPointer(type, name, version, region, "state");
    }

    /** 
     * {@inheritDoc}
     */
    public List<ArtifactAccessorPointer> getArtifactsOfType(String type) {
        this.lastCalled = "getArtifactsOfType";
        ArrayList<ArtifactAccessorPointer> artifactPointers = new ArrayList<ArtifactAccessorPointer>();
        artifactPointers.add(new StubArtifactAccessorAndPointer(type, "testName", "testVersion", "region", "state"));
        return artifactPointers;
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getTypes() {
        this.lastCalled = "getTypes";
        return new ArrayList<String>();
    }

    /** 
     * {@inheritDoc}
     */
    public String refresh(String type, String name, String version, String region) {
        this.lastCalled = "refresh";
        return "refresh";
    }

    /** 
     * {@inheritDoc}
     */
    public String start(String type, String name, String version, String region) {
        this.lastCalled = "start";
        return "start";
    }

    /** 
     * {@inheritDoc}
     */
    public String stop(String type, String name, String version, String region) {
        this.lastCalled = "stop";
        return "stop";
    }

    /** 
     * {@inheritDoc}
     */
    public String uninstall(String type, String name, String version, String region) {
        this.lastCalled = "uninstall";
        return "uninstall";
    }

}
