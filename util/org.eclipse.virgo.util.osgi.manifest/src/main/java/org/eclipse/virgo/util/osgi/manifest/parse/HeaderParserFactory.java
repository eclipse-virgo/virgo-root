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

package org.eclipse.virgo.util.osgi.manifest.parse;

import org.eclipse.virgo.util.osgi.manifest.parse.standard.StandardHeaderParser;

/**
 * Factory for creating {@link HeaderParser} instances
 * <p/>
 * .
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class HeaderParserFactory {

	/**
	 * Creates a new {@link HeaderParser} that logs to the supplied {@link ParserLogger}.
	 * 
	 * @param logger the <code>ParserLogger</code> to log to.
	 * @return the new <code>HeaderParser</code>.
	 */
	public static HeaderParser newHeaderParser(ParserLogger logger) {
		return new StandardHeaderParser(logger);
	}
}
