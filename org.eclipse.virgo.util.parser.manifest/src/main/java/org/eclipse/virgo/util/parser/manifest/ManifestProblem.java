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



import org.eclipse.virgo.util.parser.manifest.internal.SourceContext;



/**

 * <p>

 * Encapsulates a problem discovered during manifest parsing.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * This class is thread safe.

 * 


 */

public class ManifestProblem {



	private ManifestProblemKind problemKind;

	private SourceContext context;

	private int startoffset;

	private int endoffset;

	private String[] inserts;



	public ManifestProblem(ManifestProblemKind problem, SourceContext context, int startoffset, int endoffset, String... inserts) {

		this.problemKind = problem;

		this.context = context;

		this.startoffset = startoffset;

		this.endoffset = endoffset;

		this.inserts = inserts;

	}



	@Override

	public String toString() {

		return problemKind.format(context.getLine(startoffset), context.getColumn(startoffset), inserts);

	}



	/**

	 * Returns a multi line message that includes the manifest extract that gave

	 * rise to the problem.

	 * 

	 * @return a multi line string

	 */

	public String toStringWithContext() {

		int relevantLine = context.getLine(startoffset);

		String relevantLineData = context.getLineAsString(relevantLine);

		String theMessage = problemKind.format(context.getLine(startoffset), context.getColumn(startoffset), inserts);

		StringBuilder beautifulMessage = new StringBuilder();

		beautifulMessage.append(relevantLineData);

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



	public ManifestProblemKind getKind() {

		return problemKind;

	}



	public int getInsertCount() {

		return inserts.length;

	}



	public String getInsert(int insertIndex) {

		return inserts[insertIndex];

	}



	public int getStartLine() {

		return context.getLine(startoffset);

	}



	public int getEndLine() {

		return context.getLine(endoffset);

	}



	public int getStartColumn() {

		return context.getColumn(startoffset);

	}



	public int getEndColumn() {

		return context.getColumn(endoffset);

	}



	public int getStartOffset() {

		return startoffset;

	}



	public int getEndOffset() {

		return endoffset;

	}



}

