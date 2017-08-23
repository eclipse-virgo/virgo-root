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

package org.eclipse.virgo.repository.internal.remote;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.repository.internal.remote.DescriptorStore;
import org.eclipse.virgo.repository.internal.remote.DescriptorStoreFactory;
import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class DescriptorStoreFactoryTests {

    private static final String ETAG_1 = "etag1";

    private static final byte[] STORE_CONTENTS_1 = new byte[] { 0 };

    private static final String ETAG_2 = "etag2";

    private static final byte[] STORE_CONTENTS_2 = new byte[] { 0, 1 };

    private static final String REPOSITORY_NAME = "reponame";

    private static final File DESCRIPTOR_STORE_DIRECTORY = new File("build/descriptorStore");

    private DescriptorStoreFactory descriptorStoreFactory;

    @Before
    public void setUp() throws Exception {
        new PathReference(DESCRIPTOR_STORE_DIRECTORY).delete(true);
        DESCRIPTOR_STORE_DIRECTORY.mkdirs();
        this.descriptorStoreFactory = new DescriptorStoreFactory(REPOSITORY_NAME, DESCRIPTOR_STORE_DIRECTORY);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateDescriptorStore() throws FileNotFoundException, IOException {
        DescriptorStore descriptorStore = this.descriptorStoreFactory.createDescriptorStore(new ByteArrayInputStream(STORE_CONTENTS_1), ETAG_1);
        assertEquals(STORE_CONTENTS_1.length, descriptorStore.getLocation().length());
        assertEquals(ETAG_1, descriptorStore.getEtag());
    }

    @Test
    public void testRecoverDescriptorStore() throws FileNotFoundException, IOException {
        this.descriptorStoreFactory.createDescriptorStore(new ByteArrayInputStream(STORE_CONTENTS_1), ETAG_1);
        DescriptorStore descriptorStore = this.descriptorStoreFactory.recoverDescriptorStore();
        assertEquals(STORE_CONTENTS_1.length, descriptorStore.getLocation().length());
        assertEquals(ETAG_1, descriptorStore.getEtag());
    }

    @Test
    public void testRecoverDescriptorStoreFromMultipleFiles() throws FileNotFoundException, IOException, InterruptedException {
        this.descriptorStoreFactory.createDescriptorStore(new ByteArrayInputStream(STORE_CONTENTS_1), ETAG_1);
        
        // Ensure the persisted version of the next descriptor store has a distinct file last modified time to that of
        // the first descriptor store. The granularity appears to be 1000 mS on at least some platforms.
        Thread.sleep(1000);
        
        this.descriptorStoreFactory.createDescriptorStore(new ByteArrayInputStream(STORE_CONTENTS_2), ETAG_2);
        DescriptorStore descriptorStore = this.descriptorStoreFactory.recoverDescriptorStore();
        assertEquals(STORE_CONTENTS_2.length, descriptorStore.getLocation().length());
        assertEquals(ETAG_2, descriptorStore.getEtag());
    }

}
