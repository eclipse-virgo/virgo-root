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



import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.parse.BundleManifestParseException;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;









/**

 * Parses OSGI Standard Headers.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class StandardHeaderParser implements HeaderParser {



	private HeaderVisitor visitor; // a visitor that will be called to collect parsed information

	private HeaderTokenStream tokenStream;

	private StandardHeaderLexer lexer;

	private final ParserLogger logger;



	public StandardHeaderParser(ParserLogger logger) {

		this.visitor = new StandardHeaderVisitor();

		this.logger = logger;

	}



	public StandardHeaderParser(HeaderVisitor visitor, ParserLogger logger) {

		this(logger);

		// A multiplexing visitor will delegate to all visitors passed to it - first one is expected to be the one accumulating

		// headers

		this.visitor = new MultiplexingVisitor(this.visitor, visitor);

	}



	// HeaderParser interface implementation



	public HeaderDeclaration parseBundleSymbolicName(String header) {

		HeaderDeclaration declaration = internalParseBundleSymbolicName(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declaration;

	}



	public List<HeaderDeclaration> parsePackageHeader(String header, String headerType) {

		List<HeaderDeclaration> declarations = internalParsePackageHeader(header, headerType);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public HeaderDeclaration parseBundleActivationPolicy(String header) {

		HeaderDeclaration declaration = internalParseBundleActivationPolicy(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declaration;

	}



	public List<HeaderDeclaration> parseDynamicImportPackageHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseDynamicImportPackageHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public HeaderDeclaration parseFragmentHostHeader(String header) {

		HeaderDeclaration declaration = internalParseFragmentHostHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declaration;

	}



	public List<HeaderDeclaration> parseImportBundleHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseImportBundleHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public List<HeaderDeclaration> parseImportLibraryHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseImportLibraryHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public HeaderDeclaration parseLibrarySymbolicName(String header) {

		HeaderDeclaration declaration = internalParseLibrarySymbolicName(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declaration;

	}



	public List<HeaderDeclaration> parseRequireBundleHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseRequireBundleHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public List<HeaderDeclaration> parseWebFilterMappingsHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseWebFilterMappingsHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	public List<HeaderDeclaration> parseHeader(String header) {

		List<HeaderDeclaration> declarations = internalParseHeader(header);

		if (tokenStream.containsProblems(Severity.ERROR)) {

			throw new BundleManifestParseException(createErrorMessage());

		}

		return declarations;

	}



	// end of main HeaderParser interface implementation



	/**

	 * @return true if problems were found during parsing

	 */

	public boolean foundProblems() {

		return tokenStream.containsProblems();

	}



	/**

	 * @param severity of problems to look for
	 * @return true if problems were found during parsing

	 */

	public boolean foundProblems(Severity severity) {

		return tokenStream.containsProblems(severity);

	}



	/**

	 * @return the list of problems that occurred during parsing

	 */

	public List<HeaderProblem> getProblems() {

		return tokenStream.getProblems();

	}



	/**

	 * @return the source context for the parsed manifest

	 */

	public SourceContext getSourceContext() {

		return tokenStream.getSourceContext();

	}



	// internal parse methods - these are public for callers that want to parse without throwing an exception if an error occurs



	private HeaderDeclaration internalParseBundleSymbolicName(String header) {

		return parseOneBundleDescription(header);

	}



	private HeaderDeclaration internalParseLibrarySymbolicName(String header) {

		return parseOneBundleDescription(header);

	}



	private HeaderDeclaration internalParseFragmentHostHeader(String header) {

		return parseOneBundleDescription(header);

	}



	public List<HeaderDeclaration> internalParseRequireBundleHeader(String header) {

		return parseMultipleBundleDescriptions(header);

	}



	public List<HeaderDeclaration> internalParseImportLibraryHeader(String header) {

		return parseMultipleBundleDescriptions(header);

	}



	public List<HeaderDeclaration> internalParseWebFilterMappingsHeader(String header) {

		return parseMultipleBundleDescriptions(header);

	}



	public List<HeaderDeclaration> internalParseImportBundleHeader(String header) {

		return parseMultipleBundleDescriptions(header);

	}



	// Bundle-ActivationPolicy ::= policy ( ';' directive )*

	// policy ::= 'lazy'

	public HeaderDeclaration internalParseBundleActivationPolicy(String header) {

		initializeTokenStream(header);

		eatActivationPolicy();

		checkNextIsSemiColon();

		while (maybeEatSemicolon()) {

			eatParameter(false);

		}

		visitor.clauseEnded();

		confirmEnd();

		return visitor.getHeaderDeclarations().get(0);

	}



	/**
     * @param headerType  
     */
	List<HeaderDeclaration> internalParsePackageHeader(String header, String headerType) {

		initializeTokenStream(header);

		parsePackageHeader();

		return visitor.getHeaderDeclarations();

	}



	/**

	 * Attempt to consume multiple bundleDescriptions from the header.

	 * 

	 * @param header the data containing the bundleDescription. For example: "a.b.c.d;attr=5;dir:=hello,a.b.c.d;a.b.c.d.e;attr=3"

	 * @return the parsed HeaderDeclaration

	 */

	// Format: "bundledescription (',' bundledescription)*"

	private List<HeaderDeclaration> parseMultipleBundleDescriptions(String header) {

		initializeTokenStream(header);

		eatMultipleBundleDescriptions();

		return visitor.getHeaderDeclarations();

	}



	/**

	 * Attempt to consume just a single bundleDescription from the header. An error will be recorded if there is more data after the

	 * first bundleDescription.

	 * 

	 * @param header the data containing the bundleDescription. For example: "a.b.c.d;attr=5;directive:=foo"

	 * @return the parsed HeaderDeclaration

	 */

	private HeaderDeclaration parseOneBundleDescription(String header) {

		initializeTokenStream(header);

		eatSingleBundleDescription();

		return visitor.getFirstHeaderDeclaration();

	}



	private void initializeTokenStream(String header) {

		visitor.initialize();

		lexer = new StandardHeaderLexer();

		lexer.process(header);

		tokenStream = lexer.getTokenStream();

	}



	/**

	 * At the end of a name this will assert the next token in the tokenstream (if any) is a semicolon. If not then an error is

	 * recorded and recovery attempted to the next semicolon.

	 */

	private void checkNextIsSemiColon() {

		if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolon(tokenStream.peek())) {

			// ERROR: following a symbolic name should be a semi colon - that is the only option

			// RECOVERY: skip to next semi colon

			HeaderToken next = tokenStream.next();

			recordProblem(HeaderProblemKind.EXPECTED_SEMICOLON, next.getStartOffset(), next.getEndOffset(), next.stringValue());

			recoverToNextSemiColon();

		}

	}



	// /**

	// * At the end of a clause this will assert the next token in the tokenstream (if any) is a comma. If not then an error is

	// * recorded and recovery attempted to the next comma.

	// */

	// private void checkNextIsComma() {

	// if (tokenStream.hasMore() && !HeaderTokenKind.isComma(tokenStream.peek())) {

	// // ERROR: following a clause should be a comma - that is the only option

	// // RECOVERY: skip to next comma

	// HeaderToken next = tokenStream.next();

	// recordProblem(HeaderProblemKind.EXPECTED_COMMA, next.getStartOffset(), next.getEndOffset(), next.stringValue());

	// recoverToTheNextComma();

	// }

	// }



	private boolean eatActivationPolicy() {

		// TODO [later] could check in that code that the name is 'lazy'

		return eatUniqueName();

	}



	// bundleDescription::= symbolic-name (';' parameter)*

	private void eatSingleBundleDescription() {

		eatSymbolicName();

		checkNextIsSemiColon();

		while (maybeEatSemicolon()) {

			eatParameter(true);

		}

		visitor.clauseEnded();

		confirmEnd();

	}



	private void eatBundleDescription() {

		eatSymbolicName();

		while (maybeEatSemicolon()) {

			eatParameter(true);

		}

		visitor.clauseEnded();

	}



	// Require-Bundle ::= bundle-description ( ',' bundle-description )*

	// bundle-description ::= symbolic-name (';' parameter )*

	private void eatMultipleBundleDescriptions() {

		eatBundleDescription();

		// checkNextIsComma();

		while (maybeEatComma()) {

			eatBundleDescription();

		}

		visitor.endvisit();

		confirmEnd();

	}



	// PackageHeader::= pkgInfo ( ',' pkgInfo )*

	// pkgInfo ::= package-names ( ';' parameter )*

	// package-names ::= package-name ( ';' package-name )*

	private boolean parsePackageHeader() {

		boolean ok = eatPackageInfo();

		visitor.clauseEnded();

		while (maybeEatComma() && ok) {

			ok = eatPackageInfo();

			visitor.clauseEnded();

		}

		visitor.endvisit();

		confirmEnd();

		return ok;

	}



	// DynamicImport-Package ::= dynamic-description ( ',' dynamic-description )*

	private void parseDynamicImportPackage() {

		eatDynamicDescription();

		visitor.clauseEnded();

		while (maybeEatComma()) {

			eatDynamicDescription();

			visitor.clauseEnded();

		}

		visitor.endvisit();

		confirmEnd();

	}



	// header ::= clause ( ',' clause ) *

	private void parseHeader() {

		eatClause();

		visitor.clauseEnded();

		while (maybeEatComma()) {

			eatClause();

			visitor.clauseEnded();

		}

		visitor.endvisit();

		confirmEnd();

	}



	// DynamicImport-Package ::= dynamic-description ( ',' dynamic-description )*

	// dynamic-description::= wildcard-names ( ';' parameter )*

	// wildcard-names ::= wildcard-name ( ';' wildcard-name )*

	// wildcard-name ::= package-name | ( package-name '.*' ) | '*'

	private List<HeaderDeclaration> internalParseDynamicImportPackageHeader(String header) {

		initializeTokenStream(header);

		parseDynamicImportPackage();

		return visitor.getHeaderDeclarations();

	}

	

//	 *

//	 * path ::= path-unquoted | ('"' path-unquoted '"')

//	 * path-unquoted ::= path-sep | path-sep? path-element (path-sep path-element)*

//	 * path-element ::= [^/"\#x0D#x0A#x00]+

//	 * path-sep ::= '/'

	// header ::= clause ( ',' clause ) *

	// clause ::= path ( ';' path ) * ( ';' parameter ) *

	private List<HeaderDeclaration> internalParseHeader(String header) {

		visitor.initialize();

		lexer = new StandardHeaderLexer(true);

		lexer.process(header);

		tokenStream = lexer.getTokenStream();

		parseHeader();

		return visitor.getHeaderDeclarations();

	}

		

	



	// pkgInfo ::= package-names ( ';' parameter )*

	// package-names ::= package-name ( ';' package-name )*

	private boolean eatPackageInfo() {

		boolean ok = eatPackageNames();

		while (maybeEatSemicolon()) {

			ok = eatParameter(true);

		}

		return ok;

	}



	// dynamic-description::= wildcard-names ( ';' parameter )*

	// wildcard-names ::= wildcard-name ( ';' wildcard-name )*

	// wildcard-name ::= package-name | ( package-name '.*' ) | '*'

	private void eatDynamicDescription() {

		eatWildcardNames();

		while (maybeEatSemicolon()) {

			eatParameter(true);

		}

	}



	// clause ::= path ( ';' path ) * ( ';' parameter ) *

	private void eatClause() {

		eatPaths();

		while (maybeEatSemicolon()) {

			eatParameter(true);

		}

	}

	

	private boolean peekSemiColon() {

		HeaderToken t = tokenStream.peek();

		return t != null && HeaderTokenKind.isSemicolon(t);

	}



	// private boolean peekComma() {

	// HeaderToken t = tokenStream.peek();

	// return t != null && HeaderTokenKind.isComma(t);

	// }



	private boolean peekNextIsAttributeOrDirective() {

		HeaderToken tok = tokenStream.peek(1);

		return tok != null && tok.isAttributeOrDirectiveName();

	}



	/**

	 * Attempt to recover to the next semicolon.

	 * 

	 * @return true if recovery succeeded or false if we ran out of tokens

	 */

	private boolean recoverToNextSemiColon() {

		while (!peekSemiColon() && tokenStream.hasMore()) {

			tokenStream.next();

		}

		return tokenStream.hasMore(); // if it has more then we hit a semi

	}



	// /**

	// * Attempt to recover to the next comma.

	// *

	// * @return true if recovery succeeded or false if we ran out of tokens

	// */

	// private boolean recoverToTheNextComma() {

	// while (!peekComma() && tokenStream.hasMore()) {

	// tokenStream.next();

	// }

	// return tokenStream.hasMore(); // if it has more then we hit a semi

	// }



	// wildcard-names ::= wildcard-name ( ';' wildcard-name )*

	private void eatWildcardNames() {

		boolean ok = eatWildcardName();



		if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolonOrComma(tokenStream.peek())) {

			// ERROR: next token must be a semicolon (end of this name) or a comma (end of this clause)

			// RECOVERY: record a problem and skip

			HeaderToken next = tokenStream.next();

			recordProblem(HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA, next.getStartOffset(), next.getEndOffset(), next

					.stringValue());

			recoverToNextSemiColonOrComma();

			if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolon(tokenStream.peek())) {

				return;

			}

		}



		do {

			if (peekSemiColon() && peekNextIsAttributeOrDirective()) {

				// Finished eating package names - now hit a directive or attribute

				return;

			}

			if (maybeEatSemicolon()) {

				ok = eatWildcardName();

			} else {

				break;

			}

		} while (ok);

	}

	

	// paths ::= path ( ';' path ) *

	// path ::= path-unquoted | ('"' path-unquoted '"')

	// path-unquoted ::= path-sep | path-sep? path-element (path-sep path-element)*

	// path-element ::= [^/"\#x0D#x0A#x00]+

	// path-sep ::= '/'

	private void eatPaths() {

		boolean ok = eatPath();



		if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolonOrComma(tokenStream.peek())) {

			// ERROR: next token must be a semicolon (end of this name) or a comma (end of this clause)

			// RECOVERY: record a problem and skip

			HeaderToken next = tokenStream.next();

			recordProblem(HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA, next.getStartOffset(), next.getEndOffset(), next

					.stringValue());

			recoverToNextSemiColonOrComma();

			if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolon(tokenStream.peek())) {

				return;

			}

		}



		do {

			if (peekSemiColon() && peekNextIsAttributeOrDirective()) {

				// Finished eating path - now hit a directive or attribute

				return;

			}

			if (maybeEatSemicolon()) {

				ok = eatPath();

			} else {

				break;

			}

		} while (ok);

	}

	 

	 private boolean eatPath() {

		HeaderToken startToken = tokenStream.peek();

		if (startToken == null) {

			// ERROR: run out of tokens

			HeaderToken previousToken = tokenStream.peek(-1);

			recordProblem(HeaderProblemKind.UNEXPECTEDLY_OOD, (previousToken==null?0:previousToken.getEndOffset()), 

					previousToken==null?0:previousToken.getEndOffset());

			return false;

		}

		HeaderToken endToken = startToken;

		if (HeaderTokenKind.isQuotedString(startToken)) {

			// done

			tokenStream.next();

		} else if (HeaderTokenKind.isSlash(startToken)) {

			// done

			tokenStream.next();

		} else {

			tokenStream.next();

			// skip on to the next non path element (should be a , / ;)

			while (HeaderTokenKind.canBeTreatedAsPathElement(tokenStream.peek())) {

				if (endToken.hasFollowingSpace()) { // ERROR: unquoted space

					recordIllegalSpaceProblem(startToken, endToken);

				}

				endToken = tokenStream.next();

			}

			HeaderToken slashToken = maybeEatSlash();

			while (slashToken != null) {

				if (endToken.hasFollowingSpace()) { // ERROR: unquoted space

					recordIllegalSpaceProblem(startToken, slashToken);

				}

				endToken = slashToken;

				while (HeaderTokenKind.canBeTreatedAsPathElement(tokenStream.peek())) {

					if (endToken.hasFollowingSpace()) { // ERROR: unquoted space

						recordIllegalSpaceProblem(startToken, endToken);

					}

					endToken = tokenStream.next();

				}

				if (slashToken.hasFollowingSpace()) { // ERROR: the dot has a space after it

					recordIllegalSpaceProblem(startToken, endToken);

				}

				slashToken = maybeEatSlash();

				if (slashToken!=null && HeaderTokenKind.isSlash(endToken)) { // ERROR: two slashes in a row

					recordDoubleSlashProblem(endToken,slashToken);

				}

			}

		}

		visitor.visitUniqueName(subarray(lexer.data, startToken.getStartOffset(), endToken.getEndOffset()));

		return true;

	}





	// package-names ::= package-name ( ';' package-name )*

	private boolean eatPackageNames() {

		boolean ok = eatPackageName();



		if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolonOrComma(tokenStream.peek())) {

			// ERROR: following a symbolic name should be a semi colon - that is the only option

			HeaderToken next = tokenStream.next();

			recordProblem(HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA, next.getStartOffset(), next.getEndOffset(), next

					.stringValue());

			recoverToNextSemiColonOrComma();

			if (tokenStream.hasMore() && !HeaderTokenKind.isSemicolon(tokenStream.peek())) {

				return ok;

			}

		}



		do {

			if (peekSemiColon() && peekNextIsAttributeOrDirective()) {

				// Finished eating package names - now hit a directive or attribute

				break;

			}

			if (maybeEatSemicolon()) {

				ok = eatPackageName();

			} else {

				break;

			}

		} while (ok);

		return ok;

	}



	// package-name ::= unique-name

	private boolean eatPackageName() {

		return eatUniqueName();

	}



	// wildcard-name ::= package-name | ( package-name '.*' ) | '*'

	private boolean eatWildcardName() {

		return eatWildcardedUniqueName();

	}



	private final static String STAR = "*";



	/**

	 * Like processing a unique-name, but it may either be followed by a DOTSTAR or replaced entirely by a STAR

	 */

	private boolean eatWildcardedUniqueName() {

		HeaderToken start = maybeEatStar();

		if (start != null) { // wildcard '*'

			visitor.visitWildcardName(STAR);

			return true;

		}

		start = eatIdentifier();

		if (start == null) {

			// ERROR: cannot even start processing, error already reported

			return false;

		}

		HeaderToken end = start;

		HeaderToken dot = maybeEatDot();

		while (dot != null) {

			if (end.hasFollowingSpace()) { // ERROR: between the first token and the dot was a space

				recordIllegalSpaceProblem(start, dot);

			}

			end = eatIdentifier();

			if (end == null) {

				return false;

			}

			if (dot.hasFollowingSpace()) { // ERROR: the dot has a space after it

				recordIllegalSpaceProblem(start, end);

			}

			dot = maybeEatDot();

		}

		HeaderToken dotStarEnding = maybeEatDotStar();

		if (dotStarEnding != null) {

			if (end.hasFollowingSpace()) {// ERROR: there was a space after the final token and the dotstar

				recordIllegalSpaceProblem(start, dotStarEnding);

			}

			visitor.visitWildcardName(subarray(lexer.data, start.getStartOffset(), dotStarEnding.getEndOffset()));

		} else {

			visitor.visitWildcardName(subarray(lexer.data, start.getStartOffset(), end.getEndOffset()));

		}

		return true;

	}



	// unique-name ::= identifier ( '.' identifier )*

	private boolean eatUniqueName() {

		HeaderToken startToken = eatIdentifier();

		if (startToken == null) { // already reported

			return false;

		}

		HeaderToken endToken = startToken;

		HeaderToken dotToken = maybeEatDot();

		while (dotToken != null) {

			if (endToken.hasFollowingSpace()) { // ERROR: space before the dot

				recordIllegalSpaceProblem(startToken, dotToken);

			}

			endToken = eatIdentifier();

			if (endToken == null) {

				return false;

			}

			if (dotToken.hasFollowingSpace()) { // ERROR: the dot has a space after it

				recordIllegalSpaceProblem(startToken, endToken);

			}

			dotToken = maybeEatDot();

		}

		visitor.visitUniqueName(subarray(lexer.data, startToken.getStartOffset(), endToken.getEndOffset()));

		return true;

	}



	/**

	 * Eat a symbolic-name - defined as "symbolic-name :: = token('.'token)*". If possible it will continue in the presence of

	 * errors.

	 * 

	 * (R)eviewed

	 */

	private void eatSymbolicName() {

		HeaderToken startToken = eatToken(); // eatToken() will report any problem if a token is not found

		if (startToken == null) {

			return;

		}

		HeaderToken endToken = startToken;

		HeaderToken dotToken = maybeEatDot();

		while (dotToken != null) {

			if (endToken.hasFollowingSpace()) { // ERROR: between the first token and the dot was a space

				recordIllegalSpaceProblem(startToken, dotToken);

			}

			endToken = eatToken();

			if (endToken == null) {

				recordProblem(HeaderProblemKind.TOKEN_CANNOT_END_WITH_DOT, dotToken.getStartOffset(), dotToken.getEndOffset());

				endToken = dotToken;

				break;

			}

			if (dotToken.hasFollowingSpace()) { // ERROR: the dot has a space after it

				recordIllegalSpaceProblem(startToken, endToken);

			}

			dotToken = maybeEatDot();

		}

		visitor.visitSymbolicName(subarray(lexer.data, startToken.getStartOffset(), endToken.getEndOffset()));

		return;

	}



	/**

	 * @return a new array chopped out of the supplied character array

	 */

	private static final String subarray(char[] array, int start, int end) {

		return new String(array,start,end-start);

//		char[] result = new char[end - start];

//		System.arraycopy(array, start, result, 0, end - start);

//		return new String(result);

	}



	/**

	 * @return a substring representing the characters from a chunk of the supplied character array

	 */

	private static final String substring(char[] data, int start, int end) {

		return new String(data, start, end - start);

	}



	private void confirmEnd() {

		if (tokenStream.hasMore()) {

			HeaderToken current = tokenStream.next();

			tokenStream.setPosition(tokenStream.getCount() - 1);

			HeaderToken last = tokenStream.peek();

			String insert = substring(lexer.data, current.getStartOffset(), last.getEndOffset());

			recordProblem(HeaderProblemKind.UNCONSUMED_DATA, current.getStartOffset(), last.getEndOffset(), insert);

		}

	}



	/**

	 * Record a problem with parsing.

	 * 

	 * @param parseProblem the kind of problem that occurred

	 * @param startOffset the start offset of the problem

	 * @param endOffset the end offset of the problem

	 * @param inserts the inserts for the problem message text

	 */

	private void recordProblem(HeaderProblemKind parseProblem, int startOffset, int endOffset, String... inserts) {

		tokenStream.recordProblem(new HeaderProblem(parseProblem, startOffset, endOffset, inserts));

	}



	/**

	 * Record a problem for a mismatch. The token of an expected kind was not found next in the stream - either the wrong token was

	 * found or the end of the stream was found. Depending on the situation this will record either an 'out of data' error or a

	 * 'expected token X' error.

	 * 

	 * @param token the token that was found

	 * @param kind the kind of error to report if the problem was not that the end of the stream was reached

	 */

	private void recordMismatch(HeaderToken token, HeaderProblemKind kind) {

		if (token == null) {

			if (tokenStream.getCount() == 0) {

				// ERROR: no data

				recordProblem(HeaderProblemKind.UNEXPECTEDLY_OOD, 0, 0);

			} else {

				HeaderToken previous = tokenStream.peek(-1);

				recordProblem(HeaderProblemKind.UNEXPECTEDLY_OOD, previous.getEndOffset(), previous.getEndOffset());

			}

		} else {

			recordProblem(kind, token.getStartOffset(), token.getEndOffset(), token.stringValue());

		}

	}



	private void recoverToNextSemiColonOrComma() {

		HeaderToken tok = tokenStream.peek();

		while (!(tok == null || HeaderTokenKind.isComma(tok) || HeaderTokenKind.isSemicolon(tok))) {

			tokenStream.next();

			tok = tokenStream.peek();

		}

	}



	// parameter ::= directive | attribute

	private boolean eatParameter(boolean attributesAllowed) {

		// Either of those must start with a token

		HeaderToken parameterName = eatToken();

		if (parameterName == null) {

			// error already reported

			return false;

		}

		if (!parameterName.isAttributeOrDirectiveName()) {

			// ERROR: this is not of the form "a:=" or "a=" - it is likely to either be "com.foo.goo;" - as in a rogue extra

			// bundle name, or a "a.b.c:=" - a rogue dotted name for an attribute/directive

			// RECOVERY: check which situation - record a problem, then skip to the recovery token

			int pStart = parameterName.getStartOffset();

			char[] insert = parameterName.value();

			int pEnd = parameterName.getEndOffset();

			if (parameterName.isExtended()) {

				pEnd = parameterName.getExtendedEndOffset();

				insert = parameterName.extendedValue();

			}

			recordProblem(HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE, pStart, pEnd, new String(insert));

			recoverToNextSemiColonOrComma();

			return false;

		} else {

			// Handle everything other than parameter 'version'

			HeaderToken selector = tokenStream.next();

			assert HeaderTokenKind.isColonEquals(selector) || HeaderTokenKind.isEquals(selector);

			HeaderToken argument = eatArgument();

			if (argument == null) {

				return false;

			}

			String argumentValue = null;

			if (argument.isExtended()) {

				argumentValue = new String(argument.extendedValue());

			} else {

				argumentValue = new String(argument.value());

			}

			if (parameterName.isAttributeName()) {

				if (!attributesAllowed) {

					recordProblem(HeaderProblemKind.ATTRIBUTES_NOT_ALLOWED_FOR_THIS_HEADER, parameterName.getStartOffset(),

							parameterName.getEndOffset(), substring(lexer.data, parameterName.getStartOffset(), parameterName

									.getEndOffset()));

					return false;

				}

				visitor.visitAttribute(new String(parameterName.value()), argumentValue);

			} else {

				visitor.visitDirective(new String(parameterName.value()), argumentValue);

			}

		}

		// Either should have run out of tokens or the next one is a semicolon or comma

		HeaderToken next = tokenStream.peek();

		if (next != null) {

			if (!(HeaderTokenKind.isSemicolon(next) || HeaderTokenKind.isComma(next))) {

				recordProblem(HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER, next.getStartOffset(), next.getEndOffset(), next

						.stringValue());

				recoverToNextSemiColonOrComma();

			}

		}

		return true;

	}



	// TODO [later] decide whether to include positions in the visit method so exception throwing visitors can include the right

	// context?



	// argument ::= extended | quoted-string

	private HeaderToken eatArgument() {

		HeaderToken argumentToken = tokenStream.peek();

		if (argumentToken == null) {

			// ERROR: run out of tokens

			HeaderToken lastToken = tokenStream.peek(-1);

			int lastCharacterOfThatToken = lastToken.getEndOffset()-1;

			recordProblem(HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE, lastCharacterOfThatToken, lastCharacterOfThatToken);

			return null;

		}

		if (HeaderTokenKind.isQuotedString(argumentToken)) {

			tokenStream.next();

			return argumentToken;

		} else if (HeaderTokenKind.canBeTreatedAsExtendedToken(argumentToken)) {

			// Skip to the end of any extended token

			if (argumentToken.isExtended()) {

				HeaderToken next = tokenStream.next();

				// TODO [later] optimize jump to end of token (maybe token count within the stream)

				while (next.getEndOffset() != argumentToken.getExtendedEndOffset()) {

					next = tokenStream.next();

				}

			} else {

				tokenStream.next();

			}

			return argumentToken;

		} else {

			// ERROR: the token found as the argument value is not a valid token or a quoted string (it probably should be the

			// latter)

			// RECOVERY: report the problem

			recordProblem(HeaderProblemKind.INVALID_ARGUMENT_VALUE, argumentToken.getStartOffset(), argumentToken.getEndOffset(),

					argumentToken.stringValue());

			recoverToNextSemiColonOrComma();

			return null;

		}

	}



	private HeaderToken eatToken() {

		HeaderToken tok = tokenStream.peek();

		if (tok != null && HeaderTokenKind.canBeTreatedAsToken(tok)) {

			tokenStream.skip();

			return tok;

		} else {

			recordMismatch(tok, HeaderProblemKind.EXPECTED_TOKEN);

			recoverToNextSemiColonOrComma();

			return null;

		}

	}



	private HeaderToken eatIdentifier() {

		HeaderToken hToken = tokenStream.peek();

		if (hToken != null && HeaderTokenKind.canBeTreatedAsIdentifier(hToken)) {

			tokenStream.skip();

			return hToken;

		} else {

			recordMismatch(hToken, HeaderProblemKind.EXPECTED_IDENTIFIER);

			recoverToNextSemiColonOrComma();

			return null;

		}

	}



	// The maybeEatXXX() methods attempt to eat XXX from the tokenStream. If there are no more tokens or the next token is not of

	// type XXX then they will not succeed in eating it. Some variants return the token (or null) depending on whether it can be

	// eaten, some return boolean indicating whether eating was successful - the boolean variants are for callers that do not want

	// the token value.



	// private HeaderToken maybeEatToken() {

	// return tokenStream.peekForToken();

	// }

	//

	// private HeaderToken maybeEatQuotedString() {

	// return tokenStream.peekFor(HeaderTokenKind.QUOTEDSTRING);

	// }

	//

	// private HeaderToken maybeEatNumber() {

	// return tokenStream.peekFor(HeaderTokenKind.NUMBER);

	// }



	private HeaderToken maybeEatDot() {

		return tokenStream.peekFor(HeaderTokenKind.DOT);

	}



	private HeaderToken maybeEatSlash() {

		return tokenStream.peekFor(HeaderTokenKind.SLASH);

	}



	private HeaderToken maybeEatStar() {

		return tokenStream.peekFor(HeaderTokenKind.STAR);

	}



	private HeaderToken maybeEatDotStar() {

		return tokenStream.peekFor(HeaderTokenKind.DOTSTAR);

	}



	private boolean maybeEatComma() {

		return tokenStream.peekFor(HeaderTokenKind.COMMA) != null;

	}



	private boolean maybeEatSemicolon() {

		return tokenStream.peekFor(HeaderTokenKind.SEMICOLON) != null;

	}



	private void recordIllegalSpaceProblem(HeaderToken start, HeaderToken end) {

		recordProblem(HeaderProblemKind.ILLEGAL_SPACE, start.getStartOffset(), end.getEndOffset(), substring(lexer.data, start

				.getStartOffset(), end.getEndOffset()));

	}

	

	private void recordDoubleSlashProblem(HeaderToken first, HeaderToken second) {

		recordProblem(HeaderProblemKind.ILLEGAL_DOUBLE_SLASH, first.getStartOffset(), second.getEndOffset(), substring(lexer.data, first

				.getStartOffset(), second.getEndOffset()));

	}



	/**

	 * Produce a summary string of all errors. The errors are also sent to the logger

	 * 

	 * @return a string error message containing all problems found during parsing.

	 */

	private String createErrorMessage() {

		StringBuilder sb = new StringBuilder();

		sb.append("Error parsing bundle manifest header ["); // TODO [later] decide whether to include header name in message

		char[] data = lexer.data;

		sb.append(subarray(data, 0, data.length - 1)).append("]\n");

		List<HeaderProblem> problems = tokenStream.getProblems();

		for (HeaderProblem headerProblem : problems) {

			logger.outputErrorMsg(null, headerProblem.toString(tokenStream.getSourceContext()));

			sb.append(headerProblem.toString(tokenStream.getSourceContext())).append("\n");

		}

		return sb.toString();

	}



	/**

	 * Provide useful string that shows the header and progress through the tokenStream consuming it.

	 */

	@Override

	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("StandardHeaderParser for [").append(lexer.data).append("]\n");

		sb.append(tokenStream.toFormattedString()).append("\n");

		return sb.toString();

	}

}
