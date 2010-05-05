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

package org.eclipse.virgo.repository.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;


public class ShaHashGenerator implements HashGenerator {

    private static final String DIGEST_ALGORITHM = "sha";

    private static final int BUFFER_SIZE = 8192;

    public void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile) {
        if (artifactFile.isDirectory()) {
            return;
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            artifactDescriptorBuilder.addAttribute(new AttributeBuilder().setName(ArtifactBridge.ALGORITHM_KEY).setValue(DIGEST_ALGORITHM).build());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("Unable to get digest algorithm '%s'", DIGEST_ALGORITHM), e);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(artifactFile);

            int length;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((length = in.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read file '%s' for hashing", artifactFile.getAbsolutePath()), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing to do
                }
            }
        }

        BigInteger bi = new BigInteger(1, digest.digest());
        String hash = bi.toString(16);
        if (hash.length() % 2 != 0) {
            hash = "0" + hash;
        }

        AttributeBuilder attributeBuilder = new AttributeBuilder();
        attributeBuilder.setName(ArtifactBridge.HASH_KEY);
        attributeBuilder.setValue(hash);
        artifactDescriptorBuilder.addAttribute(attributeBuilder.build());
    }

}
