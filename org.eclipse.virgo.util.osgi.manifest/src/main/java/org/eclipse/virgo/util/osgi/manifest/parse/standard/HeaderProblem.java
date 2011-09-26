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

 * Encapsulates a single problem found during header parsing. The problem

 * includes a kind {@link HeaderProblemKind}, together with the start and end

 * offset for the problem in the data and some inserts that would be included in

 * the formatted form of this message. The toString() methods like to have the

 * context passed in so that they can produce nice results - the source context

 * encapsulates the source data that was being processed together with useful

 * information from that (eg. where the line breaks are - but that is less

 * useful in this situation where all problems are on the same line).

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class HeaderProblem {



	private HeaderProblemKind problemKind;

	private int startoffset;

	private int endoffset;

	private String[] inserts;



	public HeaderProblem(HeaderProblemKind problem, int startoffset, int endoffset, String... inserts) {

		this.problemKind = problem;

		this.startoffset = startoffset;

		this.endoffset = endoffset;

		this.inserts = inserts;

	}



	public String toString(SourceContext context) {

		return problemKind.format(context.getLine(startoffset), context.getColumn(startoffset), inserts);

	}



	public String toStringWithContext(SourceContext context) {

		int relevantLine = context.getLine(startoffset);

		String relevantLineData = context.getLineAsString(relevantLine);

		String theMessage = toString(context);

		StringBuilder beautifulMessage = new StringBuilder();

		beautifulMessage.append(relevantLineData).append('\n');

		int scol = context.getColumn(startoffset);

		int ecol = context.getColumn(endoffset);

		int where = 0;

		for (where = 0; where < scol; where++) {

			beautifulMessage.append(' ');

		}

		beautifulMessage.append('^');

		if (ecol != scol) {

			where++;

			for (; where < ecol; where++) {

				beautifulMessage.append(' ');

			}

			beautifulMessage.append('^');

		}

		beautifulMessage.append('\n');

		beautifulMessage.append(theMessage);

		return beautifulMessage.toString();

	}



	@Override

	public String toString() {

		return this.problemKind.format(0, 0, inserts);

	}



	public HeaderProblemKind getKind() {

		return this.problemKind;

	}



	public String[] getInserts() {

		String[] insertCopy = new String[inserts.length];

		System.arraycopy(inserts, 0, insertCopy, 0, inserts.length);

		return insertCopy;

	}



	public int getStartOffset() {

		return this.startoffset;

	}



	public int getEndOffset() {

		return this.endoffset;

	}



	public boolean isSeverity(Severity severity) {

		return this.problemKind.isSeverity(severity);

	}



}

