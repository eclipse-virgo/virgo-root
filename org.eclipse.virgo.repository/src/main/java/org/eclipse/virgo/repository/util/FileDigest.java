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

package org.eclipse.virgo.repository.util;

import static org.eclipse.virgo.util.common.Assert.isTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {@link FileDigest} is a utility for generating message digests of files.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public class FileDigest {

    public static final String SHA_DIGEST_ALGORITHM = "sha";
    
    public static final String MD5_DIGEST_ALGORITHM = "MD5";

    private static final int BUFFER_SIZE = 8192;

    public static String getFileShaDigest(File file) throws IOException {
        try {
            return getFileDigest(file, SHA_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            isTrue(false, "Use of %s algorithm threw %s", SHA_DIGEST_ALGORITHM, e);
            return null; // keep compiler happy
        }
    }

    public static String getFileDigest(File file, String digestAlgorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);

        byte[] hash = computeHash(file, digest);
        
        return hashToString(hash);
    }

    private static String hashToString(byte[] rawHash) {
        BigInteger bi = new BigInteger(1, rawHash);
        String hash = bi.toString(16);
        if (hash.length() % 2 != 0) {
            hash = "0" + hash;
        }
        return hash;
    }

    private static byte[] computeHash(File file, MessageDigest digest) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }

        return digest.digest();
    }

}
