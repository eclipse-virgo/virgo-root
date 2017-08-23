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

package org.eclipse.virgo.util.osgi.manifest.parse.standard;



/**

 * HeaderTokens are lexed from some input data.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 */

public interface HeaderToken {



	public HeaderTokenKind getKind();



	public int getStartOffset();



	public int getEndOffset();



	public char[] value();



	public String stringValue();



	public boolean isExtended();



	public int getExtendedEndOffset();



	public char[] extendedValue();



	public boolean firstCharIsLetter();



	public boolean isAttributeName();



	public boolean isDirectiveName();



	boolean isSpaced();



	public boolean isAttributeOrDirectiveName();



	public boolean hasFollowingSpace();



	public char firstChar();



}

