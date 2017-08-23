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



import java.util.ArrayList;

import java.util.List;



/**

 * Represents a stream of {@link HeaderToken} instances as lexed from some input

 * data by the lexer.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 */



public class HeaderTokenStream {



	private List<HeaderToken> tokens = new ArrayList<HeaderToken>(20);

	private int tokenStreamLen = -1;

	private int tokenStreamPosition = 0;

	private List<HeaderProblem> problems = new ArrayList<HeaderProblem>();

	SourceContext sourceContext;



	HeaderTokenStream(String header) {

		tokens.clear();

		tokenStreamPosition = 0;

		sourceContext = new SourceContext(header);

	}



	@Override

	public String toString() {

		return toFormattedString(true);

	}



	public int getCount() {

		return tokenStreamLen;

	}



	public int getPosition() {

		return tokenStreamPosition;

	}



	public HeaderToken next() {

		if (tokenStreamPosition >= tokenStreamLen) {

			return null;

		} else {

			return tokens.get(tokenStreamPosition++);

		}

	}



	public void setPosition(int newPosition) {

		tokenStreamPosition = newPosition;

	}



	public HeaderToken peek() {

		if (tokenStreamPosition >= tokenStreamLen) {

			return null;

		}

		return tokens.get(tokenStreamPosition);

	}



	public HeaderToken peekLast() {

		return tokens.get(tokenStreamLen - 1);

	}



	public HeaderToken peek(int offset) {

		int pos = tokenStreamPosition + offset;

		if (pos >= tokenStreamLen) {

			return null;

		} else if (pos < 0) {

			return null;

		}

		return tokens.get(pos);

	}



	public String toFormattedString() {

		return toFormattedString(false);

	}



	public String toFormattedString(boolean includePositionsInOutput) {

		StringBuilder sb = new StringBuilder();

		sb.append("TokenStream:#").append(tokens.size()).append(" tokens:");

		sb.append("[");

		int i = 0;

		for (HeaderToken token : tokens) {

			if (i > 0) {

				sb.append(",");

			}

			if (i == tokenStreamPosition) {

				sb.append("[[");

			}

			if (includePositionsInOutput) {

				sb.append(token.toString());

			} else {

				sb.append(token.value());



			}

			if (i == tokenStreamPosition) {

				sb.append("]]");

			}

			i++;

		}

		sb.append("]");

		return sb.toString();

	}



	void addToken(HeaderToken token) {

		tokens.add(token);

	}



	public SourceContext getSourceContext() {

		return sourceContext;

	}



	public boolean hasMore() {

		return tokenStreamPosition < tokenStreamLen;

	}



	public void skip() {

		tokenStreamPosition++;

	}



	public void recordProblem(HeaderProblem problem) {

		problems.add(problem);

	}



	public boolean containsProblems() {

		return !problems.isEmpty();

	}



	public boolean containsProblems(Severity severity) {

		if (problems.size() != 0) {

			for (HeaderProblem problem : problems) {

				if (problem.isSeverity(severity)) {

					return true;

				}

			}

		}

		return false;

	}



	public List<HeaderProblem> getProblems() {

		return problems;

	}



	public void reset() {

		setPosition(0);

	}



	/**

	 * Check if the next token is of a particular kind and return it if it is (consuming it in the process).

	 * 

	 * @param kind the kind of token wanted

	 * @return the token (if it was the right kind) otherwise null

	 */

	public HeaderToken peekFor(HeaderTokenKind kind) {

		if (tokenStreamPosition < tokenStreamLen) {

			HeaderToken t = tokens.get(tokenStreamPosition);

			if (t.getKind() == kind) {

				tokenStreamPosition++;

				return t;

			}

		}

		return null;

	}



	// public HeaderToken peekForToken() {

	// if (tokenStreamPosition < tokenStreamLen) {

	// HeaderToken t = tokens.get(tokenStreamPosition);

	// if (HeaderTokenKind.canBeTreatedAsToken(t)) {

	// tokenStreamPosition++;

	// return t;

	// }

	// }

	// return null;

	// }



	/**

	 * Called by the lexer to indicate that population of the tokenStream is now complete.

	 */

	public void lexComplete() {

		tokenStreamLen = tokens.size();

	}



}
