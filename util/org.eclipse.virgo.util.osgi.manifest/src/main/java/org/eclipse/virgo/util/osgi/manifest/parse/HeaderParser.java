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

import java.util.List;

/**
 * Strategy for parsing OSGi manifest headers.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations need not be threadsafe.
 * 
 */
public interface HeaderParser {

    /**
     * Parses the supplied import/export package header text and returns the list of corresponding
     * {@link HeaderDeclaration}s.
     * 
     * @param header the header text to parse.
     * @param headerType 
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parsePackageHeader(String header, String headerType);

    /**
     * Parses the supplied dynamic import header text and returns the list of corresponding
     * {@link HeaderDeclaration HeaderDeclarations}. Dynamic imports allow wildcarded package names.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseDynamicImportPackageHeader(String header);

    /**
     * Parses the supplied require bundle header text and returns the list of corresponding
     * {@link HeaderDeclaration HeaderDeclarations}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseRequireBundleHeader(String header);

    /**
     * Parses the supplied fragment host header text and returns the corresponding
     * {@link HeaderDeclaration HeaderDeclaration}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}.
     * 
     */
    HeaderDeclaration parseFragmentHostHeader(String header);

    /**
     * Parses the supplied bundle symbolic name header text and returns the corresponding
     * {@link HeaderDeclaration HeaderDeclaration}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration} for the Bundle-SymbolicName.
     */
    HeaderDeclaration parseBundleSymbolicName(String header);

    /**
     * Parses the supplied import bundle header text and returns the list of corresponding
     * {@link HeaderDeclaration HeaderDeclarations}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseImportBundleHeader(String header);

    /**
     * Parses the supplied Import-Library header text and returns the list of corresponding
     * {@link HeaderDeclaration HeaderDeclarations}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseImportLibraryHeader(String header);

    /**
     * Parses the supplied Library-SymbolicName header text and returns the list of corresponding
     * {@link HeaderDeclaration HeaderDeclarations}.
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}.
     */
    HeaderDeclaration parseLibrarySymbolicName(String header);
    
    /**
     * Parses the supplied <code>Web-FilterMappings</code> header text and returns the list
     * of corresponding {@link HeaderDeclaration HeaderDeclarations}.
     * @param header 
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseWebFilterMappingsHeader(String header);
    
    /**
     * Parses the supplied <code>Bundle-ActivationPolicy</code> header text and returns the
     * corresponding {@link HeaderDeclaration}.
     * @param header 
     * @return the {@link HeaderDeclaration}.
     */
    HeaderDeclaration parseBundleActivationPolicy(String header);
    
    
    /**
     * Parses the supplied header text and returns the list of corresponding {@link HeaderDeclaration}.
     * Format for the header is expected to follow OSGi 3.2.4 "Common Header Syntax"
     * 
     * @param header the header text to parse.
     * @return the {@link HeaderDeclaration}s.
     */
    List<HeaderDeclaration> parseHeader(String header);
}
