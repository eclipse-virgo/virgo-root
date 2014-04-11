/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.web.war.deployer;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class IOUtils {
	private static final char SEPARATOR1 = '/';
	private static final char SEPARATOR2 = '\\';

	public static boolean recursiveDelete(File root) {
		if (root == null) {
			return true;
		}

		if (root.isDirectory()) {
			File[] files = root.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.isDirectory()) {
						recursiveDelete(file);
					} else {
						file.delete();
					}
				}
			}
		}

		return root.delete();
	}// end of recursiveDelete(File root)

	public static void extractJar(File srcFile, File destDir) throws IOException {
		if (srcFile == null)
			throw new IllegalArgumentException("Source file is null.");

		if (destDir == null)
			throw new IllegalArgumentException("Destination directory is null.");

		if (!srcFile.isFile() || !srcFile.canRead())
			throw new IllegalArgumentException("Source file must be a readable file [" + srcFile + "].");

		if (destDir.exists())
			recursiveDelete(destDir);

		destDir.mkdirs();
		if (!destDir.exists())
			throw new IOException("Could not create destination directory [" + destDir + "].");

		JarFile zip = new JarFile(srcFile);
		try {
			Enumeration<JarEntry> enumZipEntries = zip.entries();
			String entryName = null;
			JarEntry theEntry = null;

			while (enumZipEntries.hasMoreElements()) {
				theEntry = (JarEntry) enumZipEntries.nextElement();
				entryName = theEntry.getName();
				if (entryName != null) {
					extractFile(zip, entryName, destDir);
				}
			}
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (Exception e) {
				}
			}
		}
	}// end of extractJar(File srcFile, File destDir)

	private static void extractFile(JarFile zipf, String entryName, File dir) throws IOException {
		if (zipf == null)
			throw new IllegalArgumentException("Cannot extract zip file, that is null.");

		if (entryName == null)
			throw new IllegalArgumentException("Cannot extract zip entry, that is null.");

		if (dir == null)
			throw new IllegalArgumentException("Cannot extract zip file to directory, that is null.");

		String fName = entryName;
		fName = fName.replace(SEPARATOR1, File.separatorChar);
		fName = fName.replace(SEPARATOR2, File.separatorChar);
		File f = new File(dir, fName);

		if (f.isDirectory()) {
			return;
		}

		File parent = f.getParentFile();
		parent.mkdirs();
		JarEntry entry = (JarEntry) zipf.getEntry(entryName);

		if (entry == null) {
			entry = (JarEntry) zipf.getEntry(entryName.replace(SEPARATOR2, SEPARATOR1));
		}

		if (entry == null) {
			entry = (JarEntry) zipf.getEntry(entryName.replace(SEPARATOR1, SEPARATOR2));
		}

		JarEntry tempent = new JarEntry(entry);

		if (tempent == null || tempent.isDirectory()) {
			return;
		}

		InputStream in = new BufferedInputStream(zipf.getInputStream(tempent));
		FileOutputStream fos = new FileOutputStream(f);
		int count = 1024;
		byte[] buff = new byte[count];
		try {
			while (count == 1024) {
				count = in.read(buff);

				if (count > 0) {
					fos.write(buff, 0, count);
				}
			}
		} catch (EOFException ex) {
			buff = new byte[(int) tempent.getSize()];
			in.read(buff);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
	}// end of extractFile(JarFile zipf, String entryName, File dir)

}// end of class
