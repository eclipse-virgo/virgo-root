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



import java.util.ArrayList;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.parse.standard.HeaderProblem;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.HeaderProblemKind;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.HeaderToken;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.HeaderTokenKind;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.HeaderTokenStream;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.SourceContext;
import org.eclipse.virgo.util.osgi.manifest.parse.standard.StandardHeaderLexer;



import junit.framework.TestCase;



/**

 * Test the lexing of input data into appropriate tokens.

 * 


 */

public class StandardHeaderLexerTests extends TestCase {

	private StandardHeaderLexer lexer;
	private static char oUmlaut = '\u00f6';




	public void testLexNumber() {

		checkLexer("12345", new Number("12345"));

	}



	public void testLexAlphas() {

		checkLexer("abcdef", new Alphas("abcdef"));

	}



	public void testLexAlphanum1() {

		checkLexer("abc123", new Alphanumeric("abc123"));

	}



	public void testLexAlphanum2() {

		checkLexer("123abc", new Alphanumeric("123abc"));

	}



	public void testLexToken1() {

		checkLexer("abc_123", new Token("abc_123"));

		checkLexer("abc-123", new Token("abc-123"));

	}



	public void testLexToken2() {

		checkLexer("123_abc", new Token("123_abc"));

		checkLexer("123-abc", new Token("123-abc"));

	}



	public void testTokenKinds() {

		lexer = new StandardHeaderLexer();
		// \u00f6 is o-umlaut

		lexer.process("123;abc;abc123;_123;c\u00f6m;*;.*"); // number alpha alphanumeric token identifier star dotstar

		HeaderTokenStream stream = lexer.getTokenStream();

		HeaderToken t = stream.next();

		assertTrue("Should answer true for isNumber() :" + t, HeaderTokenKind.isNumber(t));

		assertEquals("Number", HeaderTokenKind.NUMBER.getString());

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isAlphas() :" + t, HeaderTokenKind.isAlphas(t));

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isAlphanumeric() :" + t, HeaderTokenKind.isAlphanumeric(t));

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isToken() :" + t, HeaderTokenKind.isToken(t));

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isIdentifier() :" + t, HeaderTokenKind.isIdentifier(t));

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isStar() :" + t, HeaderTokenKind.isStar(t));

		stream.skip(); // skip semicolon

		t = stream.next();

		assertTrue("Should answer true for isDotStar() :" + t, HeaderTokenKind.isDotStar(t));

	}



	public void testLexToken3() {

		checkLexer("_", new Token("_"));

		checkLexer("-", new Token("-"));

		checkLexer("_-_", new Token("_-_"));

	}

	

	public void testLexPath() {

		checkLexer(true,"a/b/c",new Alphas("a"),SLASH,new Alphas("b"),SLASH,new Alphas("c"));

	}



	public void testExtendedToken1() {

		checkLexer("com.this.that", new ExtendedToken("com.this.that"));

	}



	public void testExtendedToken2() {

		checkLexer("com. this.that", new ExtendedToken("com. this.that"));

		lexer.getTokenStream().reset();

		HeaderToken hToken = lexer.getTokenStream().next();

		assertTrue(hToken.isExtended());

		assertTrue(hToken.isSpaced());



		checkLexer("com.this .that", new ExtendedToken("com.this .that"));

		lexer.getTokenStream().reset();

		hToken = lexer.getTokenStream().next();

		assertTrue(hToken.isExtended());

		assertTrue(hToken.isSpaced());

		hToken = lexer.getTokenStream().next();

		assertFalse(hToken.isSpaced());

	}



	public void testLexPackageName1() {

		checkLexer("com.foo.bar", new Alphas("com"), DOT, new Alphas("foo"), DOT, new Alphas("bar"));

	}



	public void testLexPackageName2() {

		checkLexer("com.foo.bar.*", new Alphas("com"), DOT, new Alphas("foo"), DOT, new Alphas("bar"), new DotStar());

	}



	public void testLexInvalidPackageName() {

		checkLexer("\"WibbleWobble\"", new QuotedString("WibbleWobble"));

	}



	public void testQuotedString1() {

		// checkLexer("\"WibbleWobble\"", new QuotedString("WibbleWobble"));

		// Next two are illegal due to non-terminating quoted strings

		checkLexer("\"Wibbl\"eWobble\"aa", new QuotedString("Wibbl"), new Alphas("eWobble"), new QuotedString("aa"));

		checkLexer("\"Wibbl\"eWobble\"", new QuotedString("Wibbl"), new Alphas("eWobble"), new QuotedString(""));

		// Escaped quotes in the string itself

		checkLexer("\"Wibbl\\\"eWobble\"", new QuotedString("Wibbl\\\"eWobble")); // slash quote in the middle

	}



	public void testQuotedString2() {

		checkLexer("\"com.goo.foo\"", new QuotedString("com.goo.foo"));

	}



	public void testLexExtendedTokens() {

		checkLexer("com.foo.goo", new Alphas("com"), new Dot(), new Alphas("foo"), new Dot(), new Alphas("goo"));

		HeaderTokenStream stream = lexer.getTokenStream();

		stream.setPosition(0);

		HeaderToken first = stream.next();

		stream.setPosition(stream.getCount() - 1);

		HeaderToken last = stream.next();

		assertTrue(first.isExtended());

		assertEquals(last.getEndOffset(), first.getExtendedEndOffset());

	}



	public void testLexIdentifiers() {

		checkLexer("com.123.goo", new Alphas("com"), new Dot(), new Number("123"), new Dot(), new Alphas("goo"));

		HeaderTokenStream stream = lexer.getTokenStream();

		stream.setPosition(0);

		HeaderToken tokenCOM = stream.next();

		stream.next();

		assertTrue(HeaderTokenKind.canBeTreatedAsIdentifier(tokenCOM));

		HeaderToken token123 = stream.next();

		assertTrue(HeaderTokenKind.canBeTreatedAsIdentifier(token123));

	}



	public void testLexAttribute() {

		checkLexer("a=5", new Alphas("a"), new Equals(), new Number("5"));

		HeaderTokenStream stream = lexer.getTokenStream();

		stream.setPosition(0);

		HeaderToken t = stream.next();

		assertTrue(t.isAttributeName());

		assertFalse(t.isDirectiveName());

		assertTrue(t.firstCharIsLetter());

		t = stream.next(); // equals

		assertTrue(HeaderTokenKind.isEquals(t));

		assertFalse(HeaderTokenKind.isColonEquals(t));

		t = stream.next(); // 5

		t = stream.next(); // check not crashing walking off the end!

	}



	public void testTokenStream() {

		checkLexer("a=5", new Alphas("a"), new Equals(), new Number("5"));

		HeaderTokenStream stream = lexer.getTokenStream();

		stream.setPosition(0);

		assertEquals(0, stream.getPosition());

		HeaderToken t = stream.next();

		assertEquals(1, stream.getPosition());

		t = stream.next();

		assertEquals(2, stream.getPosition());

		t = stream.next();

		assertEquals(t, stream.peekLast());

		stream.reset();

		assertEquals(0, stream.getPosition());

	}



	public void testLexDirective() {

		checkLexer("a:=5", new Alphas("a"), new ColonEquals(), new Number("5"));

		HeaderTokenStream stream = lexer.getTokenStream();

		stream.setPosition(0);

		HeaderToken t = stream.next();

		assertTrue(t.isDirectiveName());

		assertFalse(t.isAttributeName());

		assertTrue(t.firstCharIsLetter());

		t = stream.next(); // equals

		assertFalse(HeaderTokenKind.isEquals(t));

		assertTrue(HeaderTokenKind.isColonEquals(t));

	}



	public void testLexFunkyChars1() {
		// \u00f6 is o-umlaut

		checkLexer("c\u00f6m", new Identifier("c\u00f6m"));

		checkLexer("co" + Character.toString('\u0341') + "m", new Identifier("co" + Character.toString('\u0341') + "m"));

		checkLexer("c\u00f6m.abc.goo", new Identifier("c\u00f6m"), new Dot(), new Alphas("abc"), new Dot(), new Alphas("goo"));

		// HeaderTokenStream stream = lexer.getTokenStream();

		// stream.setPosition(0);

		// HeaderToken t = stream.next();

	}
	



	public void testLexAdvancedIdentifiers() {

		// acute accent (unicode 0341) is not a javaidStart but is a javaidPart (so cannot be used to start an identifier)

		char ch = '\u0341';
		// Alphabetic promoted to identifier when ch is hit

		checkLexer("aa" + ch + "c"+oUmlaut+"m", new Identifier("aa" + ch + "c"+oUmlaut+"m"));

		// Alphanumeric promoted to identifier when ch is hit

		checkLexer("a2" + ch + "c"+oUmlaut+"m", new Identifier("a2" + ch + "c"+oUmlaut+"m"));

		// Token promoted to identifier when ch is hit

		checkLexer("a_2" + ch + "c"+oUmlaut+"m", new Identifier("a_2" + ch + "c"+oUmlaut+"m"));

		// Token promoted to identifier when ch is hit

		checkLexer("_2" + ch + "c"+oUmlaut+"m", new Identifier("_2" + ch + "c"+oUmlaut+"m"));

	}



	public void testLexBrokenIdentifiers() {

		// acute accent (unicode 0341) is not a javaidStart but is a javaidPart (so cannot be used to start an identifier)

		char ch = '\u0341';



		// All these will also have errors in

		checkLexer(ch + "c"+oUmlaut+"m", new Identifier("c"+oUmlaut+"m")); // identifier cannot start with a non JavaIdentifierStart

		checkLexer("22" + ch + "c"+oUmlaut+"m", new Number("22"), new Identifier("c"+oUmlaut+"m")); // identifier cannot start with a digit

		checkLexer("-a" + ch + "c"+oUmlaut+"m", new Token("-a"), new Identifier("c"+oUmlaut+"m")); // identifier cannot start with a minus



		checkLexerError(ch + "c"+oUmlaut+"m", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

		checkLexerError("22" + ch + "c"+oUmlaut+"m", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

		checkLexerError("-a" + ch + "c"+oUmlaut+"m", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

	}



	public void testLexFunkyChars3() {

		checkLexer("e" + Character.toString('\u0341') + "c"+oUmlaut+"m", new Identifier("e\u0341c"+oUmlaut+"m"));

	}



	public void testLexFunkyChars4() {

		checkLexer(":");

		checkLexerError(":", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

		checkLexer(";", new SemiColon());

		checkLexer(".", new Dot());

		checkLexer("..", new Dot(), new Dot());

		checkLexer(",", new Comma());

		checkLexer(".;.;.:=;=;.,,");

		System.out.println(lexer.getTokenStream().toFormattedString());

		checkLexer("'$%(*&@!#%*(&))[]{}-=_+"); // 4 valid tokens: $ - = _

		System.out.println(lexer.getTokenStream().toFormattedString());

	}



	public void testWhitespace() {

		checkLexer("com.foo ", new Alphas("com"), new Dot(), new Alphas("foo"));

		checkLexer("    foo", new Alphas("foo"));

		checkLexerError("com.foo ", new ExpectedError(HeaderProblemKind.UNEXPECTED_SPACE_WARNING));

		checkLexerError(" foo", new ExpectedError(HeaderProblemKind.UNEXPECTED_SPACE_WARNING));

	}



	public void testQuotedStringProblems() {

		checkLexer("\"Wibbl\\\"eWobble\"", new QuotedString("Wibbl\\\"eWobble")); // slash quote in the middle

		checkLexerError("\"Wibbl\"eWobble\"a", new ExpectedError(HeaderProblemKind.NON_TERMINATING_QUOTED_STRING));

		checkLexerError("\"Wibbl\reWobble\"", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

		checkLexerError("\"Wibbl\neWobble\"", new ExpectedError(HeaderProblemKind.UNEXPECTED_CHARACTER));

		checkLexer("\"Wibbl\\\neWobble\"", new QuotedString("Wibbl\\\neWobble"));

	}



	public void testWildcards() {

		checkLexer("*", new Star());

		checkLexer(".*", new DotStar());

		checkLexer("com.foo.*", new Alphas("com"), new Dot(), new Alphas("foo"), new DotStar());

	}



	public void testSequencesThatExerciseUnexpectedStates() {

		// Just finished a QUOTEDSTRING and hitting a DIGIT

		checkLexer("\"a\"23", new QuotedString("a"), new Number("23"));

		checkLexer("\"a\"a", new QuotedString("a"), new Alphas("a"));

		checkLexer("\"a\"_", new QuotedString("a"), new Token("_"));

		checkLexer("a\"a\"", new Alphas("a"), new QuotedString("a"));

		checkLexer("1\"a\"", new Number("1"), new QuotedString("a"));

		checkLexer("_\"a\"", new Token("_"), new QuotedString("a"));

		checkLexer(""+oUmlaut+"\"a\"", new Identifier(""+oUmlaut+""), new QuotedString("a"));

		checkLexer("\"a\""+oUmlaut+"", new QuotedString("a"), new Identifier(""+oUmlaut+""));

	}



	// -- real header data



	// public void testLexRealHeaderValues1() {

	// checkLexer("com.springsource.server.osgi;version=1.2.3;uses:=org.springframework.core,com.springsource.server.osgi.framework;version=1.3.2");

	// System.out.println(lexer.getTokenStream().toFormattedString(false));

	// System.out.println(lexer.getTokenStream().toFormattedString(true));

	// }



	public void testLexRealHeaderValues2() {

		checkLexer("com.foo;version=\"1.2.3\";uses:=a.b,c.d;version=1.3.2");

		String fmt = lexer.getTokenStream().toFormattedString(false);

		assertTrue(">>" + fmt, fmt.startsWith("TokenStream:#25 tokens:[[[com]],.,foo,;,"));

		fmt = lexer.getTokenStream().toFormattedString(true);

		assertTrue(">>" + fmt, fmt.startsWith("TokenStream:#25 tokens:[[['com'@0:3]],'.'@3:4,'foo'@4:7"));

		assertNotNull(lexer.getTokenStream().toString());

	}

	

	public void testSourceContext() {

		char[] theSource = "hello world".toCharArray();

		SourceContext ctx = new SourceContext("hello world");

		// When not told about linebreaks, it assumes just one line: first break

		// at 0 to start it then final break at the end

		int[] breaks = ctx.getLinebreaks();

		assertNotNull(breaks);

		assertEquals(2, breaks.length);

		assertEquals(0, breaks[0]);

		assertEquals(theSource.length, breaks[1]);

		List<Integer> l = new ArrayList<Integer>();

		l.add(5);l.add(10);l.add(15);

		// ctx = new SourceContext("hello there world".toCharArray(), l);

		// breaks = ctx.getLinebreaks();

		// assertNotNull(breaks);

		// assertEquals(3, breaks.length);

		// assertEquals(5, breaks[0]);

		// assertEquals(10, breaks[1]);

		// assertEquals(15, breaks[2]);

		ctx = new SourceContext("hello World");

		ctx.setLinebreaks(l);

		breaks = ctx.getLinebreaks();

		assertNotNull(breaks);

		assertEquals(3, breaks.length);

		assertEquals(5, breaks[0]);

		assertEquals(10, breaks[1]);

		assertEquals(15, breaks[2]);

	}



	// ---



	/**

	 * Check that during lexing of the specified input data, the expected errors occur.

	 * 

	 * @param data input data for lexing

	 * @param errors expected sequence of errors that should occur during lexing

	 */

	private void checkLexerError(String data, ExpectedError... errors) {

		lexer = new StandardHeaderLexer();

		lexer.process(data);

		if (errors != null && errors.length != 0) {

			HeaderTokenStream stream = lexer.getTokenStream();

			assertTrue(stream.containsProblems());

			assertEquals(errors.length, stream.getProblems().size());

			List<HeaderProblem> problems = stream.getProblems();

			// for debugging:

			// for (HeaderProblem headerProblem : problems) {

			// System.out.println(headerProblem.toStringWithContext(stream.getSourceContext()));

			// }

			for (int i = 0; i < errors.length; i++) {

				assertEquals(errors[i].getKind(), problems.get(i).getKind());

			}

		}

	}



	static class ExpectedError {



		private HeaderProblemKind problemKind;



		public ExpectedError(HeaderProblemKind problemKind) {

			this.problemKind = problemKind;

		}



		public HeaderProblemKind getKind() {

			return problemKind;

		}



	}



	/**

	 * Check that the specified input data is lexed into the expected set of tokens

	 * 

	 * @param data input data for lexing

	 * @param expectedTokens expected sequence of tokens to be found after lexing

	 */

	private void checkLexer(boolean supportSlashes, String data, ExpectedToken... expectedTokens) {

		lexer = new StandardHeaderLexer(supportSlashes);

		lexer.process(data);

		if (expectedTokens != null && expectedTokens.length != 0) {

			HeaderTokenStream stream = lexer.getTokenStream();

			// System.out.println(stream.toFormattedString());

			if (expectedTokens.length == 1 && expectedTokens[0] instanceof ExtendedToken) {

				HeaderToken nextToken = stream.next();

				assertTrue(nextToken.isExtended());

				assertEquals(expectedTokens[0].expectedValue, new String(nextToken.extendedValue()));

			} else {

				assertEquals(expectedTokens.length, stream.getCount());

				for (int t = 0; t < expectedTokens.length; t++) {

					ExpectedToken exp = expectedTokens[t];

					HeaderToken nextToken = stream.next();

					// System.out.println(nextToken.stringValue());

					assertEquals(exp.getKind(), nextToken.getKind());

					String expValue = exp.getExpectedValue();

					if (expValue != null) {

						assertEquals(expValue, nextToken.stringValue());

					}

				}

			}

		}

	}

	

	private void checkLexer(String data, ExpectedToken... expectedTokens) {

		checkLexer(false,data,expectedTokens);

	}



	static class ExpectedToken {

		HeaderTokenKind expectedKind;

		String expectedValue;



		ExpectedToken(HeaderTokenKind expectedKind) {

			this.expectedKind = expectedKind;

		}



		ExpectedToken(HeaderTokenKind expectedKind, String expectedValue) {

			this.expectedKind = expectedKind;

			this.expectedValue = expectedValue;

		}



		HeaderTokenKind getKind() {

			return expectedKind;

		}



		String getExpectedValue() {

			return expectedValue;

		}

	}



	static class Alphanumeric extends ExpectedToken {

		Alphanumeric() {

			super(HeaderTokenKind.ALPHANUMERIC);

		}



		Alphanumeric(String expectedValue) {

			super(HeaderTokenKind.ALPHANUMERIC, expectedValue);

		}

	}



	static class Alphas extends ExpectedToken {

		Alphas(String expectedValue) {

			super(HeaderTokenKind.ALPHAS, expectedValue);

		}

	}



	static class Star extends ExpectedToken {

		Star() {

			super(HeaderTokenKind.STAR, "*");

		}

	}



	static class DotStar extends ExpectedToken {

		DotStar() {

			super(HeaderTokenKind.DOTSTAR, ".*");

		}

	}



	static class Number extends ExpectedToken {

		Number(String expectedValue) {

			super(HeaderTokenKind.NUMBER, expectedValue);

		}

	}



	static class Equals extends ExpectedToken {

		Equals() {

			super(HeaderTokenKind.EQUALS, "=");

		}

	}



	static class ColonEquals extends ExpectedToken {

		ColonEquals() {

			super(HeaderTokenKind.COLONEQUALS, ":=");

		}

	}



	static class Identifier extends ExpectedToken {

		Identifier(String expectedValue) {

			super(HeaderTokenKind.IDENTIFIER, expectedValue);

		}

	}



	private final static ExpectedToken DOT = new Dot();

	private final static ExpectedToken SLASH = new Slash();



	static class Dot extends ExpectedToken {

		Dot() {

			super(HeaderTokenKind.DOT, ".");

		}

	}

	

	static class Slash extends ExpectedToken {

		Slash() {

			super(HeaderTokenKind.SLASH, "/");

		}

	}



	static class SemiColon extends ExpectedToken {

		SemiColon() {

			super(HeaderTokenKind.SEMICOLON, ";");

		}

	}



	static class Comma extends ExpectedToken {

		Comma() {

			super(HeaderTokenKind.COMMA, ",");

		}

	}



	static class Token extends ExpectedToken {

		Token(String value) {

			super(HeaderTokenKind.TOKEN, value);

		}

	}



	static class ExtendedToken extends ExpectedToken {

		ExtendedToken(String value) {

			super(HeaderTokenKind.TOKEN, value);

		}

	}



	static class QuotedString extends ExpectedToken {

		QuotedString(String value) {

			super(HeaderTokenKind.QUOTEDSTRING, value);

		}

	}



	// private void printLexerProblems() {

	// printProblems(lexer.getTokenStream().getProblems(),

	// lexer.getTokenStream().getSourceContext());

	// }



	// private void printProblems(List<HeaderProblem> problems, SourceContext

	// context) {

	// System.out.println("Found " + problems.size() + " problems");

	// for (HeaderProblem headerProblem : problems) {

	// System.out.println(headerProblem.toStringWithContext(context));

	// }

	// }

}

