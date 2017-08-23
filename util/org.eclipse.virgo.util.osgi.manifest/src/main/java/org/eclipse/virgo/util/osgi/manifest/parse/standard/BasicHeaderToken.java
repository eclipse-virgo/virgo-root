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

 * Instances of BasicHeaderToken represent tokens lexed from the input data.

 * <p>

 * Internals:

 * <p>

 * A BasicHeaderToken effectively encapsulates the start and end of the token

 * within the input data and some bitflag information that was learned during

 * lexing that can benefit parsing. To avoid unnecessary chopping up of the

 * input data, each token holds a reference to the complete headertext - and

 * should the token ever need to be extracted we can grab the piece of interest.

 * Sometimes the token values are never used and it would be a waste to chop up

 * the data and create an unnecessary array.

 * 

 * Due to the way the grammar is defined to support parameter values, there is a

 * notion in a HeaderToken of extension. The grammar defines a token to be

 * ([a-z][A-Z][0-9]_-)+ but in order to allow things like 1.2.3 and com.foo.bar

 * as parameter values without needing them to be quoted it defines an extended

 * token that is allowed for parameter values. The lexer only knows about

 * Tokens. However, it understands the notion of extension, which at its

 * simplest means a sequence of tokens with dots between them. So for any token

 * it can have the IS_EXTENDED bitflag set - and if that is set the token is the

 * start of an extended token, and it ends at the extendedEndOffset.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public final class BasicHeaderToken implements HeaderToken {



	private HeaderTokenKind kind;

	private char[] headerText; // The entire header of which this token is a chunk

	private int startoffset; // start index into the headerText

	private int endoffset; // end index into the headerText

	private int extendedEndOffset; // if this token IS_EXTENDED, this is the end of the extended token within the headerText

	private byte bits; // bitflags capturing information learned during lexing, see below



	//

	// The bit flags are set by the lexer for optimized parsing:

	// STARTED_WITH_LETTER - did this token begin with one of [a-zA-Z] - this can avoid later checks

	// (which can be expensive if Character.isJavaIdentifierStart() must be called)

	// ATTRIBUTE_NAME - does this token represent an attribute name? ie. is the token after this '='

	// DIRECTIVE_NAME - does this token represent an directive name? ie. is the token after this ':='

	// FOLLOWED_BY_SPACE - was this token followed by a ' '

	// SPACED - this token is part of an extended token and it has a space in it (may not be a problem if the consumer is not

	// consuming it as an extended token)

	// IS_EXTENDED - this token is part of an extended token (it is followed by

	// dots and further tokens). If IS_EXTENDED then the the extendedEndOffset

	// provides the end position in the header

	private static final byte STARTED_WITH_LETTER = 0x01;

	private static final byte ATTRIBUTE_NAME = 0x02;

	private static final byte DIRECTIVE_NAME = 0x04;

	private static final byte SPACED = 0x10;

	private static final byte IS_EXTENDED = 0x20;

	private static final byte FOLLOWED_BY_SPACE = 0x40;



	/**

	 * Private constructor, please use the factory method

	 */

	private BasicHeaderToken(char[] headerText, HeaderTokenKind kind, int start, int end) {

		this.headerText = headerText;

		this.kind = kind;

		this.startoffset = start;

		this.endoffset = end;

	}



	/**

	 * BasicHeaderToken factory method.
	 * @param data header text
	 * @param kind of header
	 * @param start offset
	 * @param end offset
	 * @return a new BasicHeaderToken

	 */

	public static BasicHeaderToken makeToken(char[] data, HeaderTokenKind kind, int start, int end) {

		return new BasicHeaderToken(data, kind, start, end);

	}



	/**

	 * This method creates a new array each time it is called - try not call it more than once.

	 * 

	 * @return the characters making up this token.

	 */

	public char[] value() {

		return subarray(startoffset, endoffset);

	}



	@Override

	public String toString() {

		StringBuilder s = new StringBuilder();

		s.append("'").append(stringValue()).append("'");

		s.append("@").append(startoffset).append(":").append(endoffset);

		return s.toString();

	}



	public HeaderTokenKind getKind() {

		return kind;

	}



	public int getEndOffset() {

		return endoffset;

	}



	public int getStartOffset() {

		return startoffset;

	}



	public String stringValue() {

		return new String(value());

	}



	/**

	 * This method creates a new array each time it is called - try not call it more than once.

	 * 

	 * @return the characters making up this token.

	 */

	public char[] extendedValue() {

		return subarray(startoffset, extendedEndOffset);

	}



	private final char[] subarray(int start, int end) {

		char[] result = new char[end - start];

		System.arraycopy(headerText, start, result, 0, end - start);

		return result;

	}



	public boolean isExtended() {

		return (bits & IS_EXTENDED) != 0;

	}



	public void setExtendedOffset(int endOffset) {

		bits |= IS_EXTENDED;

		extendedEndOffset = endOffset;

	}



	public int getExtendedEndOffset() {

		return extendedEndOffset;

	}



	public boolean isSpaced() {

		return (bits & SPACED) != 0;

	}



	public boolean firstCharIsLetter() {

		return (bits & STARTED_WITH_LETTER) != 0;

	}



	public boolean isAttributeOrDirectiveName() {

		return (bits & (DIRECTIVE_NAME | ATTRIBUTE_NAME)) != 0;

	}



	public boolean hasFollowingSpace() {

		return (bits & FOLLOWED_BY_SPACE) != 0;

	}



	/** @return the first character of this token */

	public char firstChar() {

		return headerText[startoffset];

	}



	public boolean isAttributeName() {

		return (bits & ATTRIBUTE_NAME) != 0;

	}



	public boolean isDirectiveName() {

		return (bits & DIRECTIVE_NAME) != 0;

	}



	public void tagAsDirectiveName() {

		bits |= DIRECTIVE_NAME;

	}



	public void tagAsAttributeName() {

		bits |= ATTRIBUTE_NAME;

	}



	public void tagAsStartedWithLetter() {

		bits |= STARTED_WITH_LETTER;

	}



	public void tagAsSpaced() {

		bits |= SPACED;

	}



	public void tagAsFollowedBySpace() {

		bits |= FOLLOWED_BY_SPACE;

	}



}

