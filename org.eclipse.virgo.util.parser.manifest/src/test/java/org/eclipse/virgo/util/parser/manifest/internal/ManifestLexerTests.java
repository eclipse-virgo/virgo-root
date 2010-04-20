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



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.virgo.util.parser.manifest.ManifestProblem;
import org.eclipse.virgo.util.parser.manifest.ManifestProblemKind;
import org.eclipse.virgo.util.parser.manifest.internal.RecoveringManifestLexer;
import org.eclipse.virgo.util.parser.manifest.internal.SourceContext;
import org.eclipse.virgo.util.parser.manifest.internal.Token;
import org.eclipse.virgo.util.parser.manifest.internal.TokenKind;
import org.eclipse.virgo.util.parser.manifest.internal.TokenStream;

import junit.framework.TestCase;



/*

 * section:           *header +newline 

 * nonempty-section:  +header +newline

 * newline:           CR LF | LF | CR (not followed by LF)

 * header:            name : value

 * name:              alphanum *headerchar

 * value:             SPACE *otherchar newline *continuation

 * continuation:      SPACE *otherchar newline

 * alphanum:          {A-Z} | {a-z} | {0-9}

 * headerchar:        alphanum | - | _

 * otherchar:         any UTF-8 character except NUL, CR and LF

 */



/**

 * These tests verify the lowest level of processing - lexing the input into tokens with some simple error recovery.

 * 


 */

public class ManifestLexerTests extends TestCase {



	TokenStream tokenStream;

	Token t;



	public void testTokenKinds() {

		tokenStream = RecoveringManifestLexer.tokenize("Name: Andy");

		t = tokenStream.next();

		assertTrue(TokenKind.isName(t));

		assertEquals("Name", t.getKind().getTokenString());

		t = tokenStream.next();

		assertTrue(TokenKind.isColon(t));

		assertEquals(":", t.getKind().getTokenString());

		t = tokenStream.next();

		assertTrue(TokenKind.isValue(t));

		assertEquals("Value", t.getKind().getTokenString());

		t = tokenStream.next();

		assertTrue(TokenKind.isNewline(t));

		assertEquals("\\n", t.getKind().getTokenString());

		t = tokenStream.next();

		assertTrue(TokenKind.isNewline(t));

		assertEquals("\\n", t.getKind().getTokenString());



		// Check what happens off the end of the stream

		assertNull(tokenStream.next()); // null - no more tokens

		assertNull(tokenStream.peek(5)); // peek off the end

	}



	public void testIllegalNameChar() {

		// % not allowed in the name, it is ignored but logged as a problem

		checkTokenization(false, "Anima%l: Elephant", new Name("Animal"), new Colon(), new Value("Elephant"), Any, Any);

		assertTrue(tokenStream.containsProblems());

		ManifestProblem problem = tokenStream.getProblems().get(0);

		assertEquals(ManifestProblemKind.ILLEGAL_NAME_CHAR, problem.getKind());

		assertEquals(5, problem.getStartOffset());

		assertEquals(5, problem.getEndOffset());

		assertEquals(1, tokenStream.getSourceContext().getLine(problem.getStartOffset()));

	}



	public void testIllegalContinuation() {

		// First char where name or continuation expected is not a space or

		// valid name char

		checkTokenization(false, "Animal: Ele\n$phant", new Name("Animal"), new Colon(), new Value("Ele$phant"), Any, Any);

		assertTrue(tokenStream.containsProblems());

		ManifestProblem problem = tokenStream.getProblems().get(0);

		assertEquals(ManifestProblemKind.MISSING_SPACE_FOR_CONTINUATION, problem.getKind());

		assertEquals(12, problem.getStartOffset());

		assertEquals(13, problem.getEndOffset());

		assertEquals(2, problem.getStartLine());

		assertEquals(2, problem.getEndLine());

		assertEquals(0, problem.getStartColumn());

		assertEquals(1, problem.getEndColumn());

		assertEquals(2, tokenStream.getSourceContext().getLine(problem.getStartOffset()));

	}



	public void testRogueExtraSpaceEndingNameLine() {

		checkTokenization(false, "Animal: \n Elephant", new Name("Animal"), new Colon(), new Value("Elephant"), Any, Any);

		assertStream("[Animal,:,Elephant,\\n,\\n]");

	}



	public void testMissingColonInNameValueHeader() {

		checkTokenization(false, "Animal Elephant", new Name("Animal"), new Colon(), new Value("Elephant"), Any, Any);

		assertTrue(tokenStream.containsProblems());

		assertEquals(ManifestProblemKind.NAME_ENDED_WITH_SPACE_RATHER_THAN_COLON, tokenStream.getProblems().get(0).getKind());

	}



	public void testTokenStreamMovement() {

		tokenStream = RecoveringManifestLexer.tokenize("Name: Andy");

		assertEquals(0, tokenStream.getPosition());

		t = tokenStream.next();

		assertEquals(1, tokenStream.getPosition());

		assertTrue(TokenKind.isName(t));

		tokenStream.setPosition(0);

		assertEquals(0, tokenStream.getPosition());

		t = tokenStream.next();

		assertTrue(TokenKind.isName(t));

	}



	public void testTokenInsertion() {

		// ':' should be inserted during lexing (it will be logged as a problem too)

		tokenStream = RecoveringManifestLexer.tokenize("Manifest-Version\n1.0");

		assertEquals("TokenStream:#9 tokens:[Manifest-Version,:,,\\n,10,:,,\\n,\\n]", tokenStream.toFormattedString(false));

		assertTrue(tokenStream.containsProblems());

	}



	public void testTokenPositioning() {

		// Andy value starts on line 4 and ends on 5

		tokenStream = RecoveringManifestLexer.tokenize("\n\n\nName: An\n dy");

		t = tokenStream.next(); // skip newline

		t = tokenStream.next(); // skip newline

		t = tokenStream.next(); // skip newline

		t = tokenStream.next(); // skip name

		t = tokenStream.next(); // skip colon

		t = tokenStream.next();

		assertEquals("Andy", t.value());

		assertEquals(4, tokenStream.getSourceContext().getLine(t));

		assertEquals(5, tokenStream.getSourceContext().getEndLine(t));

	}



	/**

	 * Check the line breaks are calculated as being in the right places - this is required for accurate messages if they need to

	 * include column and line numbers.

	 */

	public void testLinebreaks() {

		// Relies on the tokenizer basically working (this data is valid)

		tokenStream = RecoveringManifestLexer.tokenize("Foo: Bar\nGrobble: gribble\n\n\nWibble: wobble\nFlibble:\n Fl\nob\nle");

		SourceContext context = tokenStream.getSourceContext();

		int[] linebreaks = context.getLinebreaks();

		int[] expectedLinebreaks = new int[] { 0, 9, 26, 27, 28, 43, 52, 56, 59, 62, 63 };

		assertEquals(expectedLinebreaks.length, linebreaks.length);

		for (int i = 0; i < expectedLinebreaks.length; i++) {

			if (expectedLinebreaks[i] != linebreaks[i]) {

				fail("At position '" + i + "' in linebreak expected output, we expected " + expectedLinebreaks[i] + " but found "

						+ linebreaks[i]);

			}

		}

	}



	/**

	 * Check the line breaks are calculated as being in the right places - this is required for accurate messages if they need to

	 * include column and line numbers. This variant includes carriage returns.

	 */

	public void testLinebreaksIncludingCarriageReturns() {

		// Relies on the tokenizer basically working (this data is valid)

		tokenStream = RecoveringManifestLexer

				.tokenize("Foo: Bar\r\nGrobble: gribble\r\n\r\n\r\nWibble: wobble\r\nFlibble:\r\n Fl\r\nob\r\nle");

		SourceContext context = tokenStream.getSourceContext();

		int[] linebreaks = context.getLinebreaks();

		int[] expectedLinebreaks = new int[] { 0, 10, 28, 30, 32, 48, 58, 63, 67, 70, 71 };

		assertEquals(expectedLinebreaks.length, linebreaks.length);

		for (int i = 0; i < expectedLinebreaks.length; i++) {

			if (expectedLinebreaks[i] != linebreaks[i]) {

				fail("At position '" + i + "' in linebreak expected output, we expected " + expectedLinebreaks[i] + " but found "

						+ linebreaks[i]);

			}

		}

	}



	public void testNewlineTokens() {

		tokenStream = RecoveringManifestLexer

				.tokenize("Foo: Bar\r\nGrobble: gribble\r\n\r\n\r\nWibble: wobble\r\nFlibble:\r\n Fl\r\n ob\r\n le");

		// Newlines will occur for blank lines and at the end of values:

		String exp = "[Foo,:,Bar,\\n,Grobble,:,gribble,\\n,\\n,\\n,Wibble,:,wobble,\\n,Flibble,:,Floble,\\n,\\n]";

		String actual = tokenStream.toFormattedString(false);

		assertEquals(19, tokenStream.getCount());

		assertEquals(exp, actual.substring(actual.indexOf("[")));

	}



	public void testNewlineTokensStreamProcessing1() throws IOException {

		Reader iStream = toStream("Foo: Bar\nGrobble: gribble\n\n\nWibble: wobble\nFlibble:\n Fl\n ob\n le");

		tokenStream = RecoveringManifestLexer.tokenize(iStream);

		// Newlines will occur for blank lines and at the end of values:

		String exp = "[Foo,:,Bar,\\n,Grobble,:,gribble,\\n,\\n,\\n,Wibble,:,wobble,\\n,Flibble,:,Floble,\\n,\\n]";

		String actual = tokenStream.toFormattedString(false);

		assertEquals(19, tokenStream.getCount());

		assertEquals(exp, actual.substring(actual.indexOf("[")));

	}



	public void testNewlineTokensStreamProcessing2() throws IOException {

		Reader iStream = toStream("Foo: Bar\r\nGrobble: gribble\r\n\r\n\r\nWibble: wobble\r\nFlibble:\r\n Fl\r\n ob\r\n le");

		tokenStream = RecoveringManifestLexer.tokenize(iStream);

		// Newlines will occur for blank lines and at the end of values:

		String exp = "[Foo,:,Bar,\\n,Grobble,:,gribble,\\n,\\n,\\n,Wibble,:,wobble,\\n,Flibble,:,Floble,\\n,\\n]";

		String actual = tokenStream.toFormattedString(false);

		assertEquals(19, tokenStream.getCount());

		assertEquals(exp, actual.substring(actual.indexOf("[")));

	}



	private Reader toStream(String data) {

		return new StringReader(data);

	}



	public void testUsingTokenizer() {

		// Should produce a stream of 3 tokens (name=Foo colon value=Bar)

		tokenStream = RecoveringManifestLexer.tokenize("Foo: Bar");

		assertEquals(5, tokenStream.getCount());

		checkNextToken(new Name("Foo", 0, 3, 1, 0, 3));

		checkNextToken(new Colon(3, 4, 1, 3, 4));

		checkNextToken(new Value("Bar", 5, 8, 1, 5, 8));

	}



	public void testUsingTokenizerMultiLine() {

		tokenStream = RecoveringManifestLexer.tokenize("Char: Drizzle\nClass: Warlock");

		assertEquals(9, tokenStream.getCount());

		checkNextToken(new Name("Char", 0, 4, 1, 0, 4));

		checkNextToken(new Colon(4, 5, 1, 4, 5));

		checkNextToken(new Value("Drizzle", 6, 13, 1, 6, 13));

		checkNextToken(new Newline(13, 14, 1, 13, 0)); // offset for end of a

		// newline is 0, first

		// char on the next row!

		checkNextToken(new Name("Class", 14, 19, 2, 0, 5));

		checkNextToken(new Colon(19, 20, 2, 5, 6));

		checkNextToken(new Value("Warlock", 21, 28, 2, 7, 14));

		checkNextToken(new Newline(28, 29, 2, 14, 0));

	}



	public void testUsingTokenizerMultiLineAsStream() throws IOException {

		tokenStream = RecoveringManifestLexer.tokenize(toStream("Char: Drizzle\nClass: Warlock"));

		assertEquals(9, tokenStream.getCount());

		checkNextToken(new Name("Char", 0, 4, 1, 0, 4));

		checkNextToken(new Colon(4, 5, 1, 4, 5));

		checkNextToken(new Value("Drizzle", 6, 13, 1, 6, 13));

		checkNextToken(new Newline(13, 14, 1, 13, 0)); // offset for end of a

		// newline is 0, first

		// char on the next row!

		checkNextToken(new Name("Class", 14, 19, 2, 0, 5));

		checkNextToken(new Colon(19, 20, 2, 5, 6));

		checkNextToken(new Value("Warlock", 21, 28, 2, 7, 14));

		checkNextToken(new Newline(28, 29, 2, 14, 0));



	}



	public void testStreamProcessingUnicode() throws IOException {

		tokenStream = RecoveringManifestLexer.tokenize(toStream("Char: Drizzle\nClass: foo�bar"));

		assertEquals(9, tokenStream.getCount());

	}



	public void testProcessingUnicode() throws Exception {

		tokenStream = RecoveringManifestLexer.tokenize("Char: Drizzle\nClass: foo�bar");

		assertEquals(9, tokenStream.getCount());

	}



	public void testJustTheHeaderName() {

		// Missing a value for this name

		tokenStream = RecoveringManifestLexer.tokenize("JustTheName");

		assertTrue(tokenStream.containsProblems());

		assertStream("[JustTheName,:,,\\n,\\n]"); // phantom colon and value

		// inserted

		ManifestProblem problem = tokenStream.getProblems().get(0);

		assertEquals(ManifestProblemKind.NAME_ENDED_PREMATURELY_WITH_NEWLINE, problem.getKind());

		String s = problem.toString();

		assertTrue(">" + s, s.startsWith("MP002:[line 1, col 0]: Header name ended prematurely"));

		s = problem.toStringWithContext();

		assertTrue(">" + s, s.startsWith("JustTheName"));



	}



	public void testNameWithoutValue() {

		checkTokenizationProblemOccurs("JustTheName:", new ExpectedProblem(ManifestProblemKind.VALUE_MUST_IMMEDIATELY_FOLLOW_NAME,

				1, 0, 11));

	}



	public void testStartingWithNewlines() {

		checkTokenization("\n\nName: value", new Newline(), new Newline(), new Name("Name"), new Colon(), new Value("value"), Any,

				Any);

	}



	public void testFailingToStartValueWithSpace() {

		checkTokenizationProblemOccurs("\n\nName:value", new ExpectedProblem(ManifestProblemKind.VALUE_MUST_START_WITH_SPACE, 3, 5,

				5));

		String exp = "[\\n,\\n,Name,:,value,\\n,\\n]";

		String act = tokenStream.toFormattedString(false);

		assertEquals(exp, act.substring(act.indexOf("[")));

	}



	public void testSplitNameValue() {

		tokenStream = RecoveringManifestLexer.tokenize("Class:\n Warlock");

		assertEquals(5, tokenStream.getCount());

		t = tokenStream.peek(2);

		assertEquals("Warlock", t.value());



		tokenStream = RecoveringManifestLexer.tokenize("Class:\n  Warlock");

		assertEquals(5, tokenStream.getCount());

		t = tokenStream.peek(2);

		assertEquals(" Warlock", t.value());

		t = tokenStream.peekLast();

		assertEquals("\\n", t.value());

	}



	public void testMultiLineValue() {

		tokenStream = RecoveringManifestLexer.tokenize("Class:\n War\n lo\n ck");

		assertEquals(5, tokenStream.getCount());

		t = tokenStream.peek(2);

		assertEquals("Warlock", t.value());

	}



	public void testIncorrectStartCharacterForName() {

		// Manifest cannot start with a 'space', recovery will attempt to treat

		// it as a name and move on

		checkTokenizationProblemOccurs(" Foo: Bar", new ExpectedProblem(ManifestProblemKind.NAME_MUST_START_WITH_ALPHANUMERIC, 1,

				0, 0));

		assertStream("[Foo,:,Bar,\\n,\\n]");

	}



	public void testMultipleErrorsAllDetected() {

		// Manifest cannot start with a 'space', recovery will attempt to treat

		// it as a name and move on - but there is a further problem in that it

		// is only a name by itself, no colon or value

		checkTokenizationProblemOccurs(" Foo", new ExpectedProblem(ManifestProblemKind.NAME_MUST_START_WITH_ALPHANUMERIC, 1, 0, 0));



		checkTokenizationProblemOccurs(" Foo",

				new ExpectedProblem(ManifestProblemKind.NAME_ENDED_PREMATURELY_WITH_NEWLINE, 1, 1, 4));



		checkTokenizationProblemOccurs(" Foo", new ExpectedProblem(ManifestProblemKind.MISSING_VALUE, 1, 1, 4));

	}



	/**

	 * Names are:

	 * <ul>

	 * <li>NAME = ALPHANUM *HEADERCHAR

	 * <li>ALPHANUM = {A-Z}|{a-z}|{0-9}

	 * <li>HEADERCHAR = ALPHANUM | - | _

	 * </ul>

	 */

	public void testParsingValidNames() {

		checkTokenization("abc: x", new Name("abc"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("abc42: x", new Name("abc42"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("37abc: x", new Name("37abc"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("abc-37: x", new Name("abc-37"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("abc_7: x", new Name("abc_7"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("3-1: x", new Name("3-1"), new Colon(), new Value("x"), new Newline(), new Newline());

		checkTokenization("3_Z: x", new Name("3_Z"), new Colon(), new Value("x"), new Newline(), new Newline());

	}



	public void testTokenizingInvalidNames() {

		checkTokenizationProblemOccurs("_", new ExpectedProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR, "_"));

		checkTokenizationProblemOccurs("-", new ExpectedProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR, "-"));

		checkTokenizationProblemOccurs(":", new ExpectedProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR, ":"));

	}



	public void testTokenizingValidValues() {

		checkTokenization("a: somevalue", Any, Any, new Value("somevalue"), Any, Any);

		checkTokenization("x: !@#$%^&*()", Any, Any, new Value("!@#$%^&*()"), Any, Any);

	}



	public void testTokenizingValidValuesSpecialChars() {

		checkTokenization("a: \"string\"", Any, Any, new Value("\"string\""), Any, Any);

		checkTokenization("a: a=3", new Name("a"), new Colon(), new Value("a=3"), Any, Any);

		checkTokenization("a: \"a=3\"", new Name("a"), new Colon(), new Value("\"a=3\""), Any, Any);

		checkTokenization("a: b:=3", new Name("a"), new Colon(), new Value("b:=3"), Any, Any);

		checkTokenization("a: \"b:=3\"", new Name("a"), new Colon(), new Value("\"b:=3\""), Any, Any);

		checkTokenization("a: b,3", new Name("a"), new Colon(), new Value("b,3"), Any, Any);

		checkTokenization("a: \"b,3\"", new Name("a"), new Colon(), new Value("\"b,3\""), Any, Any);

	}



	/**

	 * Investigating special character behaviour.
	 * @throws Exception for IO errors, for example 

	 */

	public void testSpecialCharacters() throws Exception {

		String twoByteUtf8 = "Export-Package: sch\u00f6n\n\n"; // the umlauted 'o' is

		// \u00f6

		byte[] twoByteUtf8data = twoByteUtf8.getBytes("UTF-8");

		// The byte data is one longer than the string length as 2 bytes are

		// needed to encode the special character

		assertEquals(twoByteUtf8.length() + 1, twoByteUtf8data.length);



		// String threeByteUtf8 = "Export-Package: Euro\u20acSymbol\n\n"; // the euro

		// symbol is \u20ac

		// byte[] threeByteUtf8data = threeByteUtf8.getBytes("UTF-8");

		// // The byte data is two longer than the string length as 3 bytes are

		// needed to encode the special character

		// assertEquals(threeByteUtf8.length() + 2, threeByteUtf8data.length);

		// String exports = new Manifest(new

		// ByteArrayInputStream(threeByteUtf8data)).getMainAttributes().getValue("Export-Package");

		// assertEquals("Euro\u20acSymbol", exports);

		// System.out.println(exports);



		String s = "Export-Package: c\u00f6m\n\n";

		ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));

		Attributes attrs = new Manifest(is).getMainAttributes();

		String exports = attrs.getValue("Export-Package");

		assertEquals("c\u00f6m", exports);

		// How is it treated by the lexer?

		// String defaultManifestParser = new Manifest(new ByteArrayInputStream(twoByteUtf8data)).getMainAttributes().getValue(

		// "Export-Package");

		// System.out.println(defaultManifestParser);



	}



	public void testTokenizingMultipleValidValuesSpecialChars() {

		checkTokenization("a: a=3,b:=2,d=\"foo\"", Any, Any, new Value("a=3,b:=2,d=\"foo\""), Any, Any);

	}



	public void testReal01() {

		checkTokenization(false,

				"Bundle-Localization: plugin\n\rExport-Package: org.eclipse.ajdt.core,\n\r org.eclipse.ajdt.core.builder,", Any,

				new Colon(), Any, new Newline(), Any, new Name("Export-Package"), new Colon(), new Value(

						"org.eclipse.ajdt.core,org.eclipse.ajdt.core.builder,"), new Newline(), Any);

		assertTrue(tokenStream.containsProblems());

		assertEquals(ManifestProblemKind.UNEXPECTED_NEWLINE_DURING_VALUE_PARSING, tokenStream.getProblems().get(0).getKind());

	}



	public void testReal02() {

		checkTokenization(true,

				"Bundle-Localization: plugin\r\nExport-Package: org.eclipse.ajdt.core,\r\n org.eclipse.ajdt.core.builder,", Any,

				new Colon(), Any, new Newline(), new Name("Export-Package"), new Colon(), new Value(

						"org.eclipse.ajdt.core,org.eclipse.ajdt.core.builder,"), new Newline(), Any);

	}



	public void testMultiLineValues() {

		checkTokenization("a: foo\n goo", Any, Any, new Value("foogoo"), Any, Any);

		checkTokenization("a: foo\n goo\n hoo", Any, Any, new Value("foogoohoo"), Any, Any);

	}



	public void testNameValueSequences() {

		checkTokenization(false, "a: foo\n goo\nhoo", Any, Any, new Value("foogoo"), Any, new Name("hoo"), Any, Any, Any, Any);

	}



	public void testValidNameValues() {

		checkTokenization("abc: foo", new Name("abc"), new ExpectedToken(":", TokenKind.COLON), new Value("foo"), Any, Any);

	}



	// Before parsing, two newlines are added to the file (spec)

	public void testNewlines() {

		tokenStream = RecoveringManifestLexer.tokenize("");

		assertEquals(2, tokenStream.getCount());

		assertEquals(TokenKind.NEWLINE, tokenStream.peek(0).getKind());

		assertEquals(TokenKind.NEWLINE, tokenStream.peek(1).getKind());

	}



	public void testFinalValueHasNoPreceedingSpace() {

		tokenStream = RecoveringManifestLexer.tokenize("Foo: Bar\nGrobble: gribble\n\n\nWibble: wobble\nFlibble:\nFl\n ob\n le");

		assertTrue(tokenStream.containsProblems());

		assertEquals(ManifestProblemKind.VALUE_MUST_START_WITH_SPACE, tokenStream.getProblems().get(0).getKind());

		assertEquals(7, tokenStream.getSourceContext().getLine(tokenStream.getProblems().get(0).getStartOffset()));

		t = tokenStream.next();

		while (!t.value().equals("Flibble")) {

			t = tokenStream.next();

		}

		tokenStream.next(); // colon

		t = tokenStream.next();

		assertEquals("Floble", t.value());

		assertTrue(TokenKind.isValue(t));

	}



	// ---



	private static final ExpectedToken Any = new Anything();



	private void checkNextToken(ExpectedToken etoken) {

		Token nexttoken = tokenStream.next();

		assertEquals(etoken.kind, nexttoken.getKind());

		assertEquals(etoken.value, nexttoken.value());

		if (etoken.hasExpectedOffsets()) {

			assertEquals(etoken.soff, nexttoken.getStartOffset());

			assertEquals(etoken.eoff, nexttoken.getEndOffset());

		}

		if (etoken.hasExpectedLineAndColumn()) {

			assertEquals(etoken.line, tokenStream.getSourceContext().getLine(nexttoken));

			assertEquals(etoken.scol, tokenStream.getSourceContext().getStartColumn(nexttoken));

			assertEquals(etoken.ecol, tokenStream.getSourceContext().getEndColumn(nexttoken));

		}

	}



	static class ExpectedProblem {

		ManifestProblemKind kind;

		String[] inserts;

		int line = -1;

		int scol = -1;

		int ecol = -1;



		ExpectedProblem(ManifestProblemKind problemKind, int line, int scol, int ecol, String... inserts) {

			this.kind = problemKind;

			this.line = line;

			this.scol = scol;

			this.ecol = ecol;

			this.inserts = inserts;

		}



		ExpectedProblem(ManifestProblemKind problemKind, String... inserts) {

			this.kind = problemKind;

			this.inserts = inserts;

		}



		public boolean hasExpectedPosition() {

			return line != -1 && scol != -1 && ecol != -1;

		}

	}



	static class Anything extends ExpectedToken {

		Anything() {

			super(null, null);

		}

	}



	static class Name extends ExpectedToken {

		Name(String txt) {

			super(txt, TokenKind.NAME);

		}



		Name(String txt, int soff, int eoff, int line, int scol, int ecol) {

			super(txt, TokenKind.NAME, soff, eoff, line, scol, ecol);

		}

	}



	static class Colon extends ExpectedToken {

		Colon() {

			super(":", TokenKind.COLON);

		}



		Colon(int soff, int eoff, int line, int scol, int ecol) {

			super(":", TokenKind.COLON, soff, eoff, line, scol, ecol);

		}

	}



	static class Newline extends ExpectedToken {

		Newline() {

			super("\\n", TokenKind.NEWLINE);

		}



		Newline(int soff, int eoff, int line, int scol, int ecol) {

			super("\\n", TokenKind.NEWLINE, soff, eoff, line, scol, ecol);

		}

	}



	static class Value extends ExpectedToken {

		Value(String txt) {

			super(txt, TokenKind.VALUE);

		}



		Value(String txt, int soff, int eoff, int line, int scol, int ecol) {

			super(txt, TokenKind.VALUE, soff, eoff, line, scol, ecol);

		}

	}



	private void checkTokenizationProblemOccurs(String data, ExpectedProblem eProblem) {

		tokenStream = RecoveringManifestLexer.tokenize(data);

		assertTrue(tokenStream.containsProblems());

		List<ManifestProblem> problems = tokenStream.getProblems();

		boolean checked = false;

		for (ManifestProblem manifestParserProblemInstance : problems) {

			if (manifestParserProblemInstance.getKind() == eProblem.kind) {

				if (eProblem.inserts != null) {

					for (int i = 0; i < eProblem.inserts.length; i++) {

						assertTrue(manifestParserProblemInstance.getInsertCount() > 0);

						if (!eProblem.inserts[i].equals(manifestParserProblemInstance.getInsert(i))) {

							fail("Insert " + i + " was expected to be '" + eProblem.inserts[i] + "' but was '"

									+ manifestParserProblemInstance.getInsert(i) + "'");

						}

					}

				}

				if (eProblem.hasExpectedPosition()) {

					assertEquals(eProblem.line, tokenStream.getSourceContext().getLine(manifestParserProblemInstance));

					assertEquals(eProblem.scol, tokenStream.getSourceContext().getStartColumn(manifestParserProblemInstance));

					assertEquals(eProblem.ecol, tokenStream.getSourceContext().getEndColumn(manifestParserProblemInstance));

				}

				checked = true;

			}

		}

		if (!checked) {

			fail("Did not find a problem of the expected kind '" + eProblem.kind + "'\n" + stringifyProblems(problems));

		}

	}



	private void checkTokenization(String data, ExpectedToken... expectedTokens) {

		checkTokenization(true, data, expectedTokens);

	}



	private void checkTokenization(boolean careAboutProblems, String data, ExpectedToken... expectedTokens) {

		tokenStream = RecoveringManifestLexer.tokenize(data);

		if (careAboutProblems && tokenStream.containsProblems()) {

			printProblems(tokenStream.getProblems());

			fail("Unexpected problems during processing of '" + data + "'\n" + stringifyProblems(tokenStream.getProblems()));

		}

		assertEquals("Tokenstream: " + tokenStream.toFormattedString(), (expectedTokens == null ? 0 : expectedTokens.length),

				tokenStream.getCount());

		if (expectedTokens != null) {

			for (int i = 0; i < expectedTokens.length; i++) {

				ExpectedToken etoken = expectedTokens[i];

				if (etoken == Any) {

					continue;

				}

				Token t = tokenStream.peek(i);

				assertEquals(etoken.kind, t.getKind());

				assertEquals(etoken.value, t.value());

				if (etoken.hasExpectedOffsets()) {

					assertEquals(etoken.line, tokenStream.getSourceContext().getLine(t));

				}

			}

		}

	}



	static class ExpectedToken {

		String value;

		TokenKind kind;

		int soff = -1;

		int eoff = -1;

		int line = -1;

		int scol = -1;

		int ecol = -1;



		ExpectedToken(String value) {

			this.value = value;

		}



		ExpectedToken(String value, TokenKind kind) {

			this.value = value;

			this.kind = kind;

		}



		ExpectedToken(String value, TokenKind kind, int soffset, int eoffset, int line, int scol, int ecol) {

			this.value = value;

			this.kind = kind;

			this.soff = soffset;

			this.eoff = eoffset;

			this.line = line;

			this.scol = scol;

			this.ecol = ecol;

		}



		boolean hasExpectedOffsets() {

			return soff != -1 && eoff != -1;

		}



		public boolean hasExpectedLineAndColumn() {

			return line != -1 && scol != -1 && ecol != -1;

		}

	}



	private void printProblems(List<ManifestProblem> lexerProblems) {

		for (Iterator<ManifestProblem> iterator = lexerProblems.iterator(); iterator.hasNext();) {

			ManifestProblem manifestProblem = iterator.next();

			System.out.println(manifestProblem.toStringWithContext());

		}

	}



	private String stringifyProblems(List<ManifestProblem> problems) {

		StringBuilder sb = new StringBuilder();

		for (ManifestProblem manifestProblem : problems) {

			sb.append(manifestProblem.toString()).append("\n");

		}

		return sb.toString();

	}



	private void assertStream(String expectedFormattedStream) {

		String actualFormattedStream = tokenStream.toFormattedString(false);

		actualFormattedStream = actualFormattedStream.substring(actualFormattedStream.indexOf("["));

		assertEquals(expectedFormattedStream, actualFormattedStream);

	}

}

