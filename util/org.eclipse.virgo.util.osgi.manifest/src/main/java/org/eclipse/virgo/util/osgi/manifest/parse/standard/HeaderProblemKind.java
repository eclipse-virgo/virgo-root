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



import java.text.MessageFormat;



/**

 * Enumerated type that captures all the errors that can occur during parsing.

 * By using enumerated types it is easier to test for errors without creating a

 * dependency on the actual text in the message. Messages can be formatted by

 * specifying the necessary missing pieces: a position and optionally some

 * inserts. Messages have a severity so that the programmer may choose to ignore

 * info/warning problems if they wish. The funky '//' chars are there to stop

 * the formatter messing the whole file up on save !

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public enum HeaderProblemKind {



	EXPECTED_TOKEN("HP001", "Expected a sequence of ([A-Z][a-z][_][-]*) but found ''{0}''", Severity.ERROR), //

	EXPECTED_IDENTIFIER("HP002", "Expected an identifier but found ''{0}''", Severity.ERROR), //

	UNEXPECTED_CHARACTER("HP003", "Unexpected character ''{0}'' ({1})", Severity.ERROR), //

	NON_TERMINATING_QUOTED_STRING("HP004", "Quoted string did not terminate before end of data", Severity.ERROR), // 

	UNEXPECTED_SPACE_WARNING("HP005", "Unexpected space found", Severity.WARNING), //

	ILLEGAL_SPACE("HP006", "Space not allowed here", Severity.ERROR), //

	EXPECTED_ATTRIBUTE_OR_DIRECTIVE("HP007", "Expected a simple attribute or directive name, but found ''{0}''", Severity.ERROR), //

	UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE("HP008", "Unexpectedly ran out of data whilst processing an argument value", Severity.ERROR), //

	UNEXPECTEDLY_OOD("HP009", "Unexpectedly ran out of data", Severity.ERROR), // 

	INVALID_ARGUMENT_VALUE("HP010", "Invalid argument value ''{0}'' - must either be a quoted string or a simple sequence",

			Severity.ERROR), //

	UNCONSUMED_DATA("HP011", "Unconsumed data found at end of header ''{0}''", Severity.ERROR), //

	EXTRANEOUS_DATA_AFTER_PARAMETER("HP012",

			"Extraneous data found at end of the attribute/directive ''{0}'' - non simple values must be quoted", Severity.ERROR), //

	EXPECTED_SEMICOLON("HP013", "Expected a semicolon but found ''{0}''", Severity.ERROR), //

	EXPECTED_SEMICOLON_OR_COMMA("HP014", "Expected a semicolon or comma but found ''{0}''", Severity.ERROR), //

	TOKEN_CANNOT_END_WITH_DOT("HP015", "Sequence cannot end with a dot", Severity.ERROR), //

	ATTRIBUTES_NOT_ALLOWED_FOR_THIS_HEADER("HP016", "This header cannot specify attributes.  Attribute found was ''{0}''",

			Severity.ERROR), //

	EXPECTED_COMMA("HP017", "Expected a comma but found ''{0}''", Severity.ERROR), //

	ILLEGAL_DOUBLE_SLASH("HP018", "A path cannot contain two slashes together", Severity.ERROR);



	private Severity severity;

	private String code;

	private String message;



	private HeaderProblemKind(String i, String string, Severity severity) {

		this.code = i;

		this.message = string;

		this.severity = severity;

	}



	public boolean isSeverity(Severity severity) {

		return this.severity == severity;

	}



	public String getCode() {

		return code;

	}



	/**
     * @param line unused 
	 * @param scol column offset
	 * @param inserts into format
	 * @return formatted problem string 
     */
	public String format(int line, int scol, String... inserts) {

		StringBuilder str = new StringBuilder();

		str.append(code).append(severity.name().charAt(0)).append(":");

		if (scol != 0) {

			str.append(MessageFormat.format("[col {0}]: ", scol));

		}

		str.append(MessageFormat.format(message, (Object[]) inserts));

		return str.toString();

	}



}
