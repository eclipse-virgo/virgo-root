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

package org.eclipse.virgo.util.parser.manifest.internal;

/**
 * Represents part of a single header from a bundle manifest
 *
 */
public final class ManifestToken implements Token {

	private static final String COLON_VALUE = ":";

	private static final String NEWLINE_VALUE = "\\n";

	private String value;

	private TokenKind kind;

	private int startoffset;

	private int endoffset;

	public static ManifestToken makeName(char[] value, int start, int end) {
		return new ManifestToken(new String(value), TokenKind.NAME, start, end);
	}

	public static ManifestToken makeValue(char[] value, int start, int end) {
		return new ManifestToken(new String(value), TokenKind.VALUE, start, end);
	}

	public static ManifestToken makeColon(int start, int end) {
		return new ManifestToken(COLON_VALUE, TokenKind.COLON, start, end);
	}

	public static Token makeNewline(int start, int end) {
		return new ManifestToken(NEWLINE_VALUE, TokenKind.NEWLINE, start, end);
	}

	private ManifestToken(String value, TokenKind kind, int start, int end) {
		this.value = value;
		this.kind = kind;
		this.startoffset = start;
		this.endoffset = end;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("'").append(value).append("'");
		s.append("@").append(startoffset).append(":").append(endoffset);
		return s.toString();
	}

	public TokenKind getKind() {
		return kind;
	}

	public int getEndOffset() {
		return endoffset;
	}

	public int getStartOffset() {
		return startoffset;
	}

}