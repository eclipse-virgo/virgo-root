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

package org.eclipse.virgo.util.osgi.manifest;

import java.io.IOException;
import java.io.Reader;
import java.util.Dictionary;

import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;
import org.eclipse.virgo.util.osgi.manifest.parse.DummyParserLogger;
import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;
import org.eclipse.virgo.util.parser.manifest.ManifestContents;


/**
 * This interface provides factory methods for creating empty bundle manifests and empty instances of the more complex
 * bundle headers.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public class BundleManifestFactory {

    /**
     * Creates a new, empty {@link BundleManifest}.
     * 
     * @return the new <code>BundleManifest</code>.
     */
    public static BundleManifest createBundleManifest() {
        return createBundleManifest(new DummyParserLogger());
    }

    /**
     * Creates a new, empty {@link BundleManifest}.
     * 
     * @param parserLogger The parser logger to use when creating new headers in the manifest
     * @return the new <code>BundleManifest</code>.
     */
    public static BundleManifest createBundleManifest(ParserLogger parserLogger) {
        return new StandardBundleManifest(parserLogger);
    }

    /**
     * Creates a new {@link BundleManifest} derived from the supplied {@link Dictionary} of bundle manifest headers.
     * 
     * @param headers The <code>Dictionary</code> of headers
     * @return The <code>BundleManifest</code> derived from the <code>Dictionary</code>.
     */
    public static BundleManifest createBundleManifest(Dictionary<String, String> headers) {
        return createBundleManifest(headers, new DummyParserLogger());
    }

    /**
     * Creates a new {@link BundleManifest} derived from the supplied {@link Dictionary} of bundle manifest headers. The
     * supplied {@link ParserLogger} will be used to report problems encountered during parsing.
     * 
     * @param headers The <code>Dictionary</code> of headers
     * @param parserLogger The <code>ParserLogger</code> to be used to report parsing problems.
     * @return The <code>BundleManifest</code> derived from the <code>Dictionary</code>.
     */
    public static BundleManifest createBundleManifest(Dictionary<String, String> headers, ParserLogger parserLogger) {
        return new StandardBundleManifest(parserLogger, headers);
    }
    
    public static BundleManifest createBundleManifest(ManifestContents manifestContents, ParserLogger parserLogger) {
        return new StandardBundleManifest(parserLogger, manifestContents);
    }

    /**
     * Creates a new {@link BundleManifest}, reading its contents from the supplied {@link Reader} The supplied
     * {@link ParserLogger} will be used to report problems encountered during parsing.
     * 
     * @param reader The <code>Reader</code> of headers
     * @param parserLogger The <code>ParserLogger</code> to be used to report parsing problems.
     * @return The <code>BundleManifest</code> populated by reading the <code>Reader</code>.
     * @throws IOException if an error occurs reading the supplied <code>Reader</code>.
     */
    public static BundleManifest createBundleManifest(Reader reader, ParserLogger parserLogger) throws IOException {
        return new StandardBundleManifest(parserLogger, reader);
    }

    /**
     * Creates a new {@link BundleManifest}, reading its contents from the supplied {@link Reader}.
     * 
     * @param reader The <code>Reader</code> of headers
     * @return The <code>BundleManifest</code> populated by reading the <code>Reader</code>.
     * @throws IOException if an error occurs reading the supplied <code>Reader</code>.
     */
    public static BundleManifest createBundleManifest(Reader reader) throws IOException {
        return createBundleManifest(reader, new DummyParserLogger());
    }
}
