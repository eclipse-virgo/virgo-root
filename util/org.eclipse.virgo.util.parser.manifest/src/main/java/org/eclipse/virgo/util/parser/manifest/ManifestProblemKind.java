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

package org.eclipse.virgo.util.parser.manifest;



import java.text.MessageFormat;



/** 

 * <p>

 * Enumeration of all the kinds of problem that can occur whilst parsing a manifest.

 * </p>

 *

 * <strong>Concurrent Semantics</strong><br />

 *

 * This class is thread safe.

 *


 */

public enum ManifestProblemKind {

	NAME_MUST_START_WITH_ALPHANUMERIC("MP001",

			"Header names in the manifest must begin at the start of the line with an alphanumeric character.  Found a ''{0}''"), //

	NAME_ENDED_PREMATURELY_WITH_NEWLINE("MP002",

			"Header name ended prematurely when a newline was encountered.  Expected form is '[Name]: [Value]'"), //

	VALUE_MUST_IMMEDIATELY_FOLLOW_NAME("MP003",

			"The value must immediately follow the '[Name]:' either on the same line or the next line."), //

	MISSING_VALUE("MP004", "The value appears to be missing for the header name ''{0}''"), //

	ILLEGAL_NAME_CHAR("MP005", "Header names cannot contain the character ''{0}''"), //

	VALUE_MUST_START_WITH_SPACE("MP006", "Values must start with a space, either following the ':' or on the next line"), //

//	UNEXPECTED_BLANK_LINES_AT_START_OF_MANIFEST("MP007", "Manifest cannot start with blank lines"), //

	UNEXPECTED_NAME("MP008", "Expected the name ''{0}'' but found the name ''{1}''"), //

	UNEXPECTED_TOKEN_KIND("MP009", "Expected a ''{0}'' but found a ''{1}''"), //

	EXPECTED_NEWLINE("MP010", "Expected a new line but found a ''{0}''"), //

	UNEXPECTED_NEWLINE_DURING_VALUE_PARSING("MP011", "Unexpectedly found a newline in the middle of a value"), //

	MISSING_SPACE_FOR_CONTINUATION("MP012",

			"Illegal character found where expecting new Name or value continuation - assuming missing space"), //

	EXPECTED_COLON("MP013", "Expected a ':' but found a ''{0}'' ''{1}''"), //

	UNEXPECTED_EOM("MP014", "Unexpectedly reached end of the manifest when expecting {0}"), //

	MISSING_NAME_HEADER("MP015", "Expected the header 'Name:' at the start of the section but found ''{0}''"), // 

	NAME_ENDED_WITH_SPACE_RATHER_THAN_COLON("MP016", "Name ended with a space instead of a colon"), //

	NAME_TOO_LONG("MP017", "Name is too long, max 65535 chars allowed"), //

	VALUE_TOO_LONG("MP018", "Value is too long, max 65535 chars allowed");



	private String code;

	private String message;



	private ManifestProblemKind(String i, String string) {

		this.code = i;

		this.message = string;

	}



	public String format(int line, int scol, String... inserts) {

		StringBuilder str = new StringBuilder();

		str.append(code).append(":");

		if (line != 0) {

			str.append(MessageFormat.format("[line {0}, col {1}]: ", line, scol));

		}

		str.append(MessageFormat.format(message, (Object[])inserts));

		return str.toString();

	}



}
