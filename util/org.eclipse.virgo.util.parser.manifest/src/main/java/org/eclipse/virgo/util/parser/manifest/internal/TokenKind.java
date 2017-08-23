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

 * Token kinds created by the Manifest lexer.

 * <p>

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public enum TokenKind {

	NAME("Name"), VALUE("Value"), COLON(":"), NEWLINE("\\n");



	private String tokenString;



	private TokenKind(String messageString) {

		this.tokenString = messageString;

	}



	public static boolean isNewline(Token t) {

		return t.getKind() == NEWLINE;

	}



	public static boolean isName(Token t) {

		return t.getKind() == NAME;

	}



	public static boolean isValue(Token t) {

		return t.getKind() == VALUE;

	}



	public static boolean isColon(Token t) {

		return t.getKind() == COLON;

	}



	public String getTokenString() {

		return tokenString;

	}

}
