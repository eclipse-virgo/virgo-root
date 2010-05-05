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

package org.eclipse.virgo.repository.internal.cacheing.cache.artifact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.Downloader;
import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class DownloaderTests {

    private PathReference destinationDirectory;

    @Before
    public void setUp() throws Exception {
        this.destinationDirectory = new PathReference("target/downloaderTests");
        this.destinationDirectory.delete(true);
        this.destinationDirectory.createDirectory();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSuccessfulDownload() throws IOException {
        File sourceFile = new File("src/test/resources/cacheing/b.jar");
        PathReference destinationPathReference = this.destinationDirectory.newChild("b.jar");

        ensureOldContentPresent(destinationPathReference);

        Downloader downloader = new Downloader(sourceFile.toURI(), destinationPathReference);
        downloader.downloadArtifact();

        checkDownloadedContent(sourceFile, destinationPathReference);
    }

    private void ensureOldContentPresent(PathReference destinationPathReference) throws IOException {
        destinationPathReference.createFile();
        File destinationFile = destinationPathReference.toFile();
        FileWriter fileWriter = new FileWriter(destinationFile);
        try {
            fileWriter.write("old content");
        } finally {
            fileWriter.close();
        }
    }

    private void checkDownloadedContent(File sourceFile, PathReference destinationPathReference) throws IOException {
        PathReference sourcePathReference = new PathReference(sourceFile);
        String sourceContents = sourcePathReference.fileContents();
        String destinationContents = destinationPathReference.fileContents();
        Assert.assertEquals(sourceContents, destinationContents);
    }

    @Test
    public void testNonExistenceSourceFile() throws IOException {
        File badSourceFile = new File("src/test/resources/cacheing/nosuchfile");
        PathReference destinationPathReference = this.destinationDirectory.newChild("b.jar");
        
        ensureOldContentPresent(destinationPathReference);
        
        Assert.assertTrue(destinationPathReference.exists());

        Downloader downloader = new Downloader(badSourceFile.toURI(), destinationPathReference);
        downloader.downloadArtifact();
        
        // Check the old destination file has been deleted.
        Assert.assertFalse(destinationPathReference.exists());
    }
    
    @Test
    public void testBadSourceUri() throws URISyntaxException, IOException {
        PathReference destinationPathReference = this.destinationDirectory.newChild("b.jar");
        
        ensureOldContentPresent(destinationPathReference);
        
        Assert.assertTrue(destinationPathReference.exists());

        Downloader downloader = new Downloader(new URI("x:y"), destinationPathReference);
        downloader.downloadArtifact();
        
        // Check the old destination file has been deleted.
        Assert.assertFalse(destinationPathReference.exists());
    }

}
