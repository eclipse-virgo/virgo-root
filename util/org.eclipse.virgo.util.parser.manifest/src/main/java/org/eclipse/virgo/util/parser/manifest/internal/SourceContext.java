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



import java.util.Arrays;

import java.util.List;

import org.eclipse.virgo.util.parser.manifest.ManifestProblem;





/**

 * SourceContext captures information about the data being processed by the

 * lexer and parser. This information can be used to provide more accurate

 * informational or error messages. Internally it has a copy of the original

 * source and where the linebreaks are within it.

 * <p>

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public class SourceContext {



	private int[] linebreaks;

	private String theSource;



	public SourceContext(String theSource) {

		this.theSource = theSource;

		this.linebreaks = new int[] { 0, theSource.length() };

	}



	public void setLinebreaks(List<Integer> linebreaks) {

		this.linebreaks = new int[linebreaks.size()];

		for (int i = 0; i < linebreaks.size(); i++) {

			this.linebreaks[i] = linebreaks.get(i);

		}

	}



	/*

	 * public SourceContext(char[] theSource, List<Integer> linebreaks) {

	 * this.theSource = theSource; this.linebreaks = new int[linebreaks.size()];

	 * for (int i = 0; i < linebreaks.size(); i++) { this.linebreaks[i] =

	 * linebreaks.get(i); } }

	 */



	/**

	 * @return the positions in the source data where the lines start.

	 */

	public int[] getLinebreaks() {

		int[] linebreaksCopy = new int[linebreaks.length];

		System.arraycopy(linebreaks, 0, linebreaksCopy, 0, linebreaks.length);

		return linebreaksCopy;

	}



	/**

	 * Compute the line that the token is on, using the linebreak information

	 * and the tokens offset
	 * @param token on the line
	 * @return the line number

	 */

	public int getLine(Token token) {

		return getRelevantStartLinebreakEntry(token.getStartOffset()) + 1;

	}



	public int getLine(int offset) {

		return getRelevantStartLinebreakEntry(offset) + 1;

	}



	public int getLine(ManifestProblem manifestParserProblemInstance) {

		return getRelevantStartLinebreakEntry(manifestParserProblemInstance.getStartOffset()) + 1;

	}



	public int getEndLine(Token token) {

		return getRelevantEndLinebreakEntry(token.getEndOffset()) + 1;

	}



	public int getStartColumn(Token token) {

		int lb = getRelevantStartLinebreakEntry(token.getStartOffset());

		return token.getStartOffset() - linebreaks[lb];

	}



	public int getStartColumn(ManifestProblem manifestParserProblemInstance) {

		int lb = getRelevantStartLinebreakEntry(manifestParserProblemInstance.getStartOffset());

		return manifestParserProblemInstance.getStartOffset() - linebreaks[lb];

	}



	public int getColumn(int offset) {

		int lb = getRelevantStartLinebreakEntry(offset);

		return offset - linebreaks[lb];

	}



	public int getEndColumn(Token token) {

		int lb = getRelevantEndLinebreakEntry(token.getEndOffset());

		return token.getEndOffset() - linebreaks[lb];

	}



	public int getEndColumn(ManifestProblem manifestParserProblemInstance) {

		int lb = getRelevantEndLinebreakEntry(manifestParserProblemInstance.getEndOffset());

		return manifestParserProblemInstance.getEndOffset() - linebreaks[lb];

	}



	// ---



	private int getRelevantStartLinebreakEntry(int startoffset) {

		int pos = Arrays.binarySearch(linebreaks, startoffset);

		if (pos < 0) {

			return -pos - 2;

		} else {

			return pos;

		}

	}



	private int getRelevantEndLinebreakEntry(int offset) {

		int pos = Arrays.binarySearch(linebreaks, offset);

		if (pos < 0) {

			return -pos - 2;

		} else {

			return pos;// - 1;

		}

	}



	public String getLineAsString(int relevantLine) {

		int startoffset = linebreaks[relevantLine - 1];

		// int endoffset = theSource.length;

		// if ((relevantLine + 1) < linebreaks.length) {

		int endoffset = linebreaks[relevantLine];

		// }

		return theSource.substring(startoffset, endoffset);

	}



}

