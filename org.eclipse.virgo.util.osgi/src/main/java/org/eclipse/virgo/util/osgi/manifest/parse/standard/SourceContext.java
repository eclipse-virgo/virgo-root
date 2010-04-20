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



import java.util.Arrays;

import java.util.List;



/**

 * SourceContext captures information about the data being processed by the

 * lexer and parser. This information can be used to provide more accurate

 * informational or error messages. Internally it has a copy of the original

 * source and where the linebreaks are within it.

 * <p>

 * The header parser actually doesn't benefit from this class knowing about

 * linebreaks because all data is on one line. But for the general Manifest

 * parser where data is split across multiple lines, it is very useful.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class SourceContext {



	private int[] linebreaks;

	private String theSource;



	public SourceContext(String theSource) {

		this.theSource = theSource;

		this.linebreaks = new int[] { 0, theSource.length() };

	}



	// Unused in this variant of Source

	public void setLinebreaks(List<Integer> linebreaks) {

		this.linebreaks = new int[linebreaks.size()];

		for (int i = 0; i < linebreaks.size(); i++) {

			this.linebreaks[i] = linebreaks.get(i);

		}

	}



	// public SourceContext(char[] theSource, List<Integer> linebreaks) {

	// this.theSource = theSource;

	// this.linebreaks = new int[linebreaks.size()];

	// for (int i = 0; i < linebreaks.size(); i++) {

	// this.linebreaks[i] = linebreaks.get(i);

	// }

	// }

	 



	/**

	 * @return the positions in the source data where the lines start.

	 */

	public int[] getLinebreaks() {

		int[] linebreaksCopy = new int[linebreaks.length];

		System.arraycopy(linebreaks, 0, linebreaksCopy, 0, linebreaks.length);

		return linebreaksCopy;

	}



	public int getLine(int offset) {

		return getRelevantStartLinebreakEntry(offset) + 1;

	}







	public int getColumn(int offset) {

		int lb = getRelevantStartLinebreakEntry(offset);

		return offset - linebreaks[lb];

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



	// private int getRelevantEndLinebreakEntry(int offset) {

	// int pos = Arrays.binarySearch(linebreaks, offset);

	// if (pos < 0) {

	// return -pos - 2;

	// } else {

	// return pos;// - 1;

	// }

	// }



	public String getLineAsString(int relevantLine) {

		int startoffset = linebreaks[relevantLine - 1];

		// int endoffset = theSource.length;

		// if ((relevantLine + 1) < linebreaks.length) {

		int endoffset = linebreaks[relevantLine];

		// }

		return theSource.substring(startoffset, endoffset - 1);

	}



}

