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



import java.util.ArrayList;

import java.util.List;

import org.eclipse.virgo.util.parser.manifest.ManifestProblem;





/**

 * 

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public class SimpleTokenStream implements TokenStream {



	private int tokenStreamPosition = 0;

	private List<Token> tokens = new ArrayList<Token>();

	private int tokenStreamLen;

	private List<ManifestProblem> problems = new ArrayList<ManifestProblem>();

	private SourceContext sourceContext;



	SimpleTokenStream() {

		tokens.clear();

		tokenStreamPosition = 0;

	}



	@Override

	public String toString() {

		return toFormattedString(true);

	}



	public int getCount() {

		return tokens.size();

	}



	public int getPosition() {

		return tokenStreamPosition;

	}



	public Token next() {

		if (tokenStreamPosition >= tokenStreamLen) {

			return null;

		} else {

			return tokens.get(tokenStreamPosition++);

		}

	}



	public void setPosition(int newPosition) {

		tokenStreamPosition = newPosition;

	}



	public Token peek() {

		if (tokenStreamPosition >= tokens.size()) {

			return tokens.get(tokens.size() - 1);

		}

		return tokens.get(tokenStreamPosition);

	}



	public Token peekLast() {

		return tokens.get(tokens.size() - 1);

	}



	public Token peek(int offset) {

		// TODO [later] negative offset check >0

		if ((tokenStreamPosition + offset) >= tokenStreamLen) {

			return null;

		} else {

			return tokens.get(tokenStreamPosition + offset);

		}

	}



	public String toFormattedString() {

		return toFormattedString(true);

	}



	public String toFormattedString(boolean includePositionsInOutput) {

		StringBuilder sb = new StringBuilder();

		sb.append("TokenStream:#").append(tokens.size()).append(" tokens:");

		sb.append("[");

		int i = 0;

		for (Token token : tokens) {

			if (i > 0) {

				sb.append(",");

			}

			if (includePositionsInOutput) {

				sb.append(token.toString());

			} else {

				sb.append(token.value());



			}

			i++;

		}

		sb.append("]");

		return sb.toString();

	}



	void addToken(Token token) {

		tokens.add(token);

	}



	public boolean containsProblems() {

		return !problems.isEmpty();

	}



	public List<ManifestProblem> getProblems() {

		return problems;

	}



	public SourceContext getSourceContext() {

		return sourceContext;

	}



	public boolean hasMore() {

		return tokenStreamPosition < tokens.size();

	}



	public void recordProblem(ManifestProblem manifestProblem) {

		problems.add(manifestProblem);

	}



	public void setSourceContext(SourceContext sourceContext) {

		this.sourceContext = sourceContext;

	}



	/**

	 * Called by the lexer to indicate that population of the tokenStream is now

	 * complete.

	 */

	public void lexComplete() {

		tokenStreamLen = tokens.size();

	}



}
