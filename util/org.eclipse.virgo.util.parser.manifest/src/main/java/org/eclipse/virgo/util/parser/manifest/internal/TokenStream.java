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



import java.util.List;

import org.eclipse.virgo.util.parser.manifest.ManifestProblem;





/**

 * Provides a stream of tokens for parsing.

 * <p>

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public interface TokenStream {



	/**

	 * @return the next token and consume it

	 */

	public Token next();



	/**

	 * @return the next token but do not consume it

	 */

	public Token peek();



	/**

	 * @param offset the offset from the current TokenStream position, can be

	 *            negative.

	 * @return the token at that particular offset from the current position.

	 */

	public Token peek(int offset);



	/**

	 * @return position within the token stream

	 */

	public int getPosition();



	/**

	 * @param newPosition the new position to move to in the token stream

	 */

	public void setPosition(int newPosition);



	/**

	 * @return number of tokens in the stream

	 */

	public int getCount();



	/**

	 * @return true if problems were found whilst processing the input data

	 */

	public boolean containsProblems();



	/**

	 * @return all the problems that occurred whilst processing the input data

	 */

	public List<ManifestProblem> getProblems();



	/**

	 * @return the source context which can be used to create improved messages

	 */

	SourceContext getSourceContext();



	public String toFormattedString(boolean b);



	public String toFormattedString();



	/**

	 * @return true if there are still more tokens to process (ie. the position

	 *         is not yet at the end)

	 */

	public boolean hasMore();



	public Token peekLast();



	public void recordProblem(ManifestProblem manifestProblem);



}

