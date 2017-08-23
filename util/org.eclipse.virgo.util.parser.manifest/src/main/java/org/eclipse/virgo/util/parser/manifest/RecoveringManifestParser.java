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



import java.io.IOException;

import java.io.Reader;

import java.util.List;

import org.eclipse.virgo.util.parser.manifest.internal.ManifestVisitor;
import org.eclipse.virgo.util.parser.manifest.internal.RecoveringManifestLexer;
import org.eclipse.virgo.util.parser.manifest.internal.StandardVisitor;
import org.eclipse.virgo.util.parser.manifest.internal.Token;
import org.eclipse.virgo.util.parser.manifest.internal.TokenKind;
import org.eclipse.virgo.util.parser.manifest.internal.TokenStream;





// Allow for the tokenizer also having trouble on bad data, so collect errors across the two

// things parser and tokenizer.



// manifest-file:    main-section newline *individual-section

// main-section:     version-info newline *main-attribute

// version-info:     Manifest-Version : version-number

// version-number:   digit+{.digit+}*

// main-attribute:   (any legitimate main attribute) newline

// individual-section: Name : value newline *perentry-attribute

// perentry-attribute: (any legitimate perentry attribute) newline

// newline : CR LF | LF | CR (not followed by LF)

// digit: {0-9}



/**

 * Builds upon the ManifestLexer that breaks a bunch of text data into a series

 * of tokens

 * <p>

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public class RecoveringManifestParser implements ManifestParser {



	// Well known headers:

	// private static final char[] MANIFEST_VERSION_HEADER =

	// "Manifest-Version".toCharArray();

	private static final String NAME_HEADER = "Name";



	ManifestVisitor visitor;

	TokenStream tokenStream;



	public RecoveringManifestParser() {

		this.visitor = new StandardVisitor();

	}



	public RecoveringManifestParser(ManifestVisitor visitor) {

		this.visitor = visitor;

	}



	public ManifestContents parse(String manifestString) {

		tokenStream = RecoveringManifestLexer.tokenize(manifestString);

		return parseManifest();

	}



	public ManifestContents parse(Reader reader) throws IOException {

		tokenStream = RecoveringManifestLexer.tokenize(reader);

		return parseManifest();

	}



	ManifestContents parseManifest() {

		checkForRogueNewlinesProceedingInput();

		boolean cont = eatSection(true); // cont indicates whether the visitor

		// decided to stop processing early

		while (tokenStream.hasMore() && cont) {

			cont = eatSection(false);

		}

		if (cont) {

			confirmEnd();

		}

		return visitor.getManifestContents();

	}



	public ManifestContents getManifestContents() {

		return visitor.getManifestContents();

	}



	private void confirmEnd() {

	}



	private void checkForRogueNewlinesProceedingInput() {

		// boolean rogueNewlines = false;

		while (maybeEatNewline()) {

			// rogueNewlines = true;

		}

//		if (rogueNewlines) {

//			recordProblem(ManifestProblemKind.UNEXPECTED_BLANK_LINES_AT_START_OF_MANIFEST, 0, tokenStream.peek().getStartOffset());

//		}

	}



	public void eatNewline() {

		assert tokenStream.hasMore();

		Token t = tokenStream.next();

		assert t.getKind() == TokenKind.NEWLINE;

		// if (tokenStream.hasMore()) {

		// Token t = tokenStream.peek();

		// if (TokenKind.isNewline(t)) {

		// tokenStream.next();

		// return;

		// } else {

		// recordProblem(ManifestProblemKind.EXPECTED_NEWLINE,

		// t.getStartOffset(), t.getEndOffset(), new String(t.value()));

		// }

		// } else {

		// recordProblem(ManifestProblemKind.UNEXPECTED_EOM,

		// tokenStream.peekLast().getStartOffset(), tokenStream.peekLast()

		// .getEndOffset(), "newline");

		// }

	}



	/**

	 * Eat a section from the input data. The boolean indicates if it is

	 * expected to be the main section. The main section starts with the version

	 * and then some number of attributes, whilst the other sections should

	 * start with a name and then attributes.

	 * 

	 * @param isMain is this expected to be the main section?
	 * @return continue?

	 */

	public boolean eatSection(boolean isMain) {

		boolean cont = true;

		if (isMain) {

			// cont =

			visitor.visitSection(true, null);

			// Cannot terminate visit to main section

			// if (!cont) {

			// return false;

			// }

			//eatVersionInfo();

		} else {

			// Skip any extra newlines between sections

			while (maybeEatNewline()) {

			}

			if (tokenStream.hasMore()) {

				cont = eatNameAttribute();

			}

		}

		// Once a Newline is eaten we have finished the section

		while (!maybeEatNewline() && tokenStream.hasMore() && cont) {

			eatAttribute(isMain);

		}

		return cont;

	}



	public void eatVersionInfo() {

		if (maybeEatHeaderName("Manifest-Version")) {

			eatColon();

			eatVersionNumber();

			eatNewline();

		} else {

			// The problem will have been reported - jump out of here which will

			// treat this (whatever it is...) as a regular header

		}

	}



	/**

	 * Consume a specific Name token from the token stream, report an error if

	 * it is the wrong kind or the wrong name.

	 * 

	 * @param expectedName

	 * @return

	 */

	private boolean maybeEatHeaderName(String expectedName) {

		Token t = tokenStream.peek();

		if (TokenKind.isName(t)) {

			if (t.value().equals(expectedName)) {

				tokenStream.next();

				return true;

			} else {

				recordProblem(ManifestProblemKind.UNEXPECTED_NAME, t.getStartOffset(), t.getEndOffset(), expectedName, t.value());

				return false;

			}

		} else {

			recordProblem(ManifestProblemKind.UNEXPECTED_TOKEN_KIND, t.getStartOffset(), t.getEndOffset(), "Name", t.getKind()

					.getTokenString());

			return false;

		}

	}



	public void eatColon() {

		Token t = tokenStream.next();

		assert t.getKind() == TokenKind.COLON;

	}



	// ATTRIBUTE := NAME_TOKEN COLON_TOKEN VALUE_TOKEN NEWLINE

	public void eatAttribute(boolean isMain) {

		Token nameToken = tokenStream.next();

		// Token colonToken =

		tokenStream.next();

		Token valueToken = tokenStream.next();

		// Token newlineToken =

		tokenStream.next();

		// This gives the visitor a chance to decide what it wants to do about

		// the value

		visitor.visitHeader(nameToken.value(), valueToken.value());

	}



	public void eatVersionNumber() {

		Token tok = tokenStream.next();

		assert tok.getKind() == TokenKind.VALUE;

		visitor.visitManifestVersion(tok.value());

	}



	// ---



	/**

	 * @return true if able to consume a Newline token from the tokenstream

	 */

	private boolean maybeEatNewline() {

		if (tokenStream.hasMore()) {

			Token t = tokenStream.peek();

			if (TokenKind.isNewline(t)) {

				tokenStream.next();

				return true;

			} else {

				return false;

			}

		} else {

			return false;

		}

	}



	/**

	 * Expects to eat "Name" ":" "<value>"
	 * @return continue?

	 */

	public boolean eatNameAttribute() {

		boolean cont;

		if (maybeEatHeaderName("Name")) {

			eatColon();

			Token valueToken = tokenStream.next();

			tokenStream.next(); // newline

			cont = visitor.visitSection(false, valueToken.value());

			if (!cont) {

				return false;

			}

			visitor.visitHeader(NAME_HEADER, valueToken.value());

		} else {

			Token token = tokenStream.peek();

			// ERROR: they didn't supply a Name. Report an error and treat this

			// as a regular attribute

			cont = visitor.visitSection(false, null); // still the start of a

			// section, just has no

			// name

			recordProblem(ManifestProblemKind.MISSING_NAME_HEADER, token.getStartOffset(), token.getEndOffset(), token.value());

		}

		return cont;

	}



	/**

	 * @return true if problems were found during parsing

	 */

	public boolean foundProblems() {

		return tokenStream.containsProblems();

	}



	/**

	 * @return the list of problems that occurred during parsing

	 */

	public List<ManifestProblem> getProblems() {

		return tokenStream.getProblems();

	}



	/**

	 * Record a problem with parsing.

	 * 

	 * @param parseProblem the kind of problem that occurred

	 * @param startOffset the start offset of the problem

	 * @param endOffset the end offset of the problem

	 * @param inserts the inserts for the problem message text

	 */

	private void recordProblem(ManifestProblemKind parseProblem, int startOffset, int endOffset, String... inserts) {

		tokenStream

				.recordProblem(new ManifestProblem(parseProblem, tokenStream.getSourceContext(), startOffset, endOffset, inserts));

	}



	// public String getVersion() {

	// return visitor.getVersion();

	// }

	//

	// public Map<String, String> getMainAttributes() {

	// return visitor.getMainAttributes();

	// }

	//

	// public Map<String, String> getAttributesForSection(String sectionName) {

	// return visitor.getAttributesForSection(sectionName);

	// }

	//

	// public List<String> getSectionNames() {

	// return visitor.getSectionNames();

	// }



	public void setTerminateAfterMainSection(boolean shouldTerminate) {

		visitor.setTerminateAfterMainSection(shouldTerminate);

	}

}

