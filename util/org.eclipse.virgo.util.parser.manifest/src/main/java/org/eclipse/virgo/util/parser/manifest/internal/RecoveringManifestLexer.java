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



import java.io.IOException;

import java.io.Reader;

import java.util.ArrayList;

import java.util.List;

import org.eclipse.virgo.util.parser.manifest.ManifestProblem;
import org.eclipse.virgo.util.parser.manifest.ManifestProblemKind;





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

 * <p>

 * Process some input data according to the expected layout above. However, this

 * is not a dumb lexer and intelligently processes the data to allow for some

 * errors (which will be reported but are recovered from) and automatically

 * processes some constructs so that the real parser building on top of this

 * lexer does not have to (ie. it identifies ; = , := in the values)

 * 

 * <p>

 * The end of result of a 'lex' is that the data is broken into these tokens:

 * NAME, COLON, VALUE, NEWLINE. Importantly:

 * <ul>

 * <li>value continuations are dealt with during the lex stage so only complete

 * VALUE tokens are in the lex token stream

 * <li>tokens are inserted where they will help the next stage cope with the

 * data. For example if the input data is apparently missing a COLON to

 * terminate a value, this lexer will report the problem but also insert a COLON

 * <li>According to the specification for processing this data, two newlines are

 * appended to data for processing - this allows for a file that is missing a

 * final return and cleanly marks the end of a section. These 2 newline tokens

 * will appear in the token stream output from this lexer

 * <li>Due to the lexer automatically handling values commencing on the line

 * after the name and values being spread across several lines, the NEWLINEs

 * that may be in the input whilst lexing a "NAME: VALUE" sequence are not

 * contained in the output token stream. Only the NEWLINEs at the end of a value

 * or for blank lines are included.

 * </ul>

 * 

 * <p>

 * Hopefully building the above knowledge into the lexer isn't make it too

 * difficult to understand...

 * <p>

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 

 * 


 */

public class RecoveringManifestLexer {



	private final static char[] BLANK_TOKEN_VALUE = "".toCharArray();

	private final static char SPACE = ' ';

	private final static char COLON = ':';

	public final static int MAX_TOKEN_LENGTH = 65535;



	private SimpleTokenStream tokenStream;

	private SourceContext context;

	private char[] data; // the manifest as a char array

	private int dataLength; // data.length

	private int dataPos; // current position in data

	private List<Integer> linebreaks = new ArrayList<Integer>(); // position of a new line

	private ParsingState parsingState = null;

	private char[] token; // Used to hold accumulated token data

	private int tokenPos = 0; // index into token array

	private int startPosition;

	private int possibleTokenEnd;

	private ManifestToken lastEmittedNameToken = null;

	private List<Token> newlineTokens = new ArrayList<Token>();



	private RecoveringManifestLexer() {

	}



	/**

	 * Tokenize a string representing a manifest.
	 * @param manifestString the string
	 * @return the token stream

	 */

	public static TokenStream tokenize(String manifestString) {

		manifestString = manifestString + "\n\n";

		RecoveringManifestLexer lexer = new RecoveringManifestLexer();

		lexer.initialize();

		lexer.process(manifestString);

		return lexer.tokenStream;

	}



	/**

	 * Tokenize input from a Reader as a manifest.
	 * @param reader for input
	 * @return token stream
	 * @throws IOException for read errors

	 */

	public static TokenStream tokenize(Reader reader) throws IOException {

		StringBuilder manifestString = new StringBuilder();

		char[] data = new char[8192];

		int dataRead = reader.read(data);

		while (dataRead != -1) {

			manifestString.append(data, 0, dataRead);

			dataRead = reader.read(data);

		}

		manifestString.append("\n\n");

		RecoveringManifestLexer lexer = new RecoveringManifestLexer();

		lexer.initialize();

		lexer.process(manifestString.toString());

		return lexer.tokenStream;

	}



	private void initialize() {

		parsingState = ParsingState.Default;

		tokenStream = new SimpleTokenStream();

		dataPos = 0;

		linebreaks.clear();

		linebreaks.add(0);

	}



	private void process(String str) {

		this.data = str.toCharArray();

		this.context = new SourceContext(str);

		this.tokenStream.setSourceContext(context);

		this.dataLength = data.length;
		this.token = new char[this.dataLength];



		while (dataPos < dataLength) {

			char ch = data[dataPos];

			if (parsingState == ParsingState.Default) {

				// In state Default: expecting a name to start or a newline

				if (isAlphanumeric(ch)) {

					// it is a Name

					parsingState = ParsingState.ParsingName;

					startNewToken();

					dataPos++;

				} else if (isNewline(ch)) {

					// it is a blank line

					processNewlines(true);

				} else if (ch == SPACE) {

					// ERROR: Found SPACE where alphanumeric expected

					// RECOVERY: skip over it and continue in this state

					recordProblem(ManifestProblemKind.NAME_MUST_START_WITH_ALPHANUMERIC, dataPos, dataPos, " ");

					dataPos++;

				} else {

					// ERROR: Found something other than an alphanumeric, space

					// or newline

					// RECOVERY: skip over it and continue in this state

					recordProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR, dataPos, dataPos + 1, Character.toString(ch));

					dataPos++;

				}

			} else if (parsingState == ParsingState.ParsingName) {

				// In state ParsingName: process name characters until something

				// else is encountered

				while (isNameChar(ch) && tokenPos < MAX_TOKEN_LENGTH) {

					token[tokenPos++] = ch;

					ch = data[++dataPos];

				}

				if (tokenPos >= MAX_TOKEN_LENGTH) {

					recordProblem(ManifestProblemKind.NAME_TOO_LONG, this.startPosition, dataPos);

					// skip over the rest of this name...

					while (isNameChar(ch)) {

						ch = data[++dataPos];

					}

				}

				if (ch == COLON) {

					// correct end to a Name

					emitNameToken();

					emitColonToken(dataPos, dataPos + 1);

					parsingState = ParsingState.ParsingValueHeaderChar;

					dataPos++;

				} else if (ch == SPACE) {

					// ERROR: missed out a colon and headed straight into value

					// RECOVERY: assume they missed the ':' and dive straight

					// into processing the value

					emitNameToken();

					emitPhantomColonToken(dataPos);

					recordProblem(ManifestProblemKind.NAME_ENDED_WITH_SPACE_RATHER_THAN_COLON, dataPos, dataPos);

					// lastEmittedNameToken

					// .getStartOffset(), lastEmittedNameToken.getEndOffset());

					parsingState = ParsingState.ParsingValueHeaderChar;

					// dont move the dataPos on so the SPACE is handled OK as

					// the start of the value

				} else if (isNewline(ch)) {

					// ERROR: name ended prematurely with a newline

					// RECOVERY: assume they forgot the ':' and that the value

					// is on the next line

					emitNameToken();

					int endOfNamePosition = dataPos;

					emitPhantomColonToken(endOfNamePosition);

					recordProblem(ManifestProblemKind.NAME_ENDED_PREMATURELY_WITH_NEWLINE, lastEmittedNameToken.getStartOffset(),

							lastEmittedNameToken.getEndOffset());



					// Did they follow this line with a blank line?

					boolean blanklines = processNewlines(false);

					// If the prematurely ending name is followed by a blank

					// line then assume they forgot the

					// value and go back to looking for a name



					if (blanklines) {

						// ERROR: The Name (which was incorrectly followed by a

						// Newline rather than a Colon) now also has blanklines

						// following the Newline

						// RECOVERY: emit a phantom value token and move on back

						// to default parsing mode (looking for a name)

						// TODO [later] could do recovery here if next char is a

						// SPACE and just assume the accidentally put line

						// breaks in between Name and value

						recordProblem(ManifestProblemKind.MISSING_VALUE, lastEmittedNameToken.getStartOffset(),

								lastEmittedNameToken.getEndOffset(), substring(this.data, lastEmittedNameToken.getStartOffset(),

										lastEmittedNameToken.getEndOffset()));

						emitPhantomValueToken(endOfNamePosition);

						emitAccumulatedNewlineTokens();

						parsingState = ParsingState.Default;

					} else {

						// Couple of choices here...

						// 

						// (1) Determine whether to switch into name or value

						// parsing mode based on first char of next line. that

						// won't cope with this:

						// Name

						// value

						// where they forgot the : and also failed to start the

						// value with a space

						// (2) Just treat the next line as the value, that won't

						// handle the situation where they've forgotten the

						// value very cleanly as we could accidentally treat

						// something that is meant to be a name as a value



						// option (1)

						// clever? If the next char is not a space then the

						// value is missing and we should go back to looking for

						// a

						// name

						if (dataPos < dataLength && data[dataPos] == SPACE) {

							parsingState = ParsingState.ParsingValueHeaderChar;

							newlineTokens.clear();

						} else {

							recordProblem(ManifestProblemKind.MISSING_VALUE, lastEmittedNameToken.getStartOffset(),

									lastEmittedNameToken.getEndOffset(), lastEmittedNameToken.value());

							emitPhantomValueToken(endOfNamePosition);

							emitAccumulatedNewlineTokens();

							parsingState = ParsingState.Default;

						}

					}

				} else {

					// TODO [later] does this cause a problem? We might have

					// gotten into a state where a value is being treated as a

					// name

					recordProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR, dataPos, dataPos, Character.toString(ch));

					dataPos++;

				}

			} else if (parsingState == ParsingState.ParsingValueHeaderChar) {

				// In state ParsingValueHeaderChar: expecting a space or newline

				// then a space

				if (ch == SPACE) {

					// Found the expected space, move to value parsing state

					startPosition = dataPos + 1;

					tokenPos = 0;

					parsingState = ParsingState.ParsingValue;

					dataPos++;

				} else if (isNewline(ch)) {

					boolean blankline = processNewlines(false);

					// Cannot be a blankline - and the char following the

					// newline must be a space

					if (blankline) {

						// hmm, the colon was followed by at least one blank

						// line, that's no good

						// Record the problem, assume the value is missing and

						// go back to looking for a name

						recordProblem(ManifestProblemKind.VALUE_MUST_IMMEDIATELY_FOLLOW_NAME,

								lastEmittedNameToken.getStartOffset(), lastEmittedNameToken.getEndOffset());

						emitPhantomValueToken(newlineTokens.get(0).getStartOffset());

						emitAccumulatedNewlineTokens();

						parsingState = ParsingState.Default;

					} else {

						newlineTokens.clear();

						// we can drop through here - as there were no

						// blanklines we know the next

						// char must be 'something' and if it is not a SPACE

						// we'll barf on coming through this

						// parsingState section next time

					}

				} else {

					// ERROR: failed to start the value with a space

					// RECOVERY: assume they just missed out the space, and

					// treat what is there as the value

					recordProblem(ManifestProblemKind.VALUE_MUST_START_WITH_SPACE, dataPos, dataPos);

					startPosition = dataPos;

					tokenPos = 0;

					parsingState = ParsingState.ParsingValue;

					// currentPosition++; don't do this - and so we will reparse

					// this char as the start of the value

				}

			} else if (parsingState == ParsingState.ParsingValue) {

				// In state ParsingValue: process characters making up the value

				// until a newline is hit

				while (!isNewline(ch)) {

					token[tokenPos++] = ch;

					ch = data[++dataPos];

				}

				if (tokenPos >= MAX_TOKEN_LENGTH) {

					recordProblem(ManifestProblemKind.VALUE_TOO_LONG, this.startPosition, dataPos);

				}

				possibleTokenEnd = dataPos; // this may not be the end of the

				// token if there is a continuation

				boolean blanklines = processNewlines(false);

				if (blanklines) {

					// Check if there is accidentally just a rogue newline in

					// and the continuation is after that...

					if (dataPos < dataLength && data[dataPos] == SPACE) {

						recordProblem(ManifestProblemKind.UNEXPECTED_NEWLINE_DURING_VALUE_PARSING, possibleTokenEnd, dataPos);

						parsingState = ParsingState.ParsingNameOrContinuation;

					} else {

						// no continuation, emit the value token, the

						// accumulated newlines and move on

						emitValueToken(startPosition, possibleTokenEnd);

						emitAccumulatedNewlineTokens();

						parsingState = ParsingState.Default;

					}

				} else {

					// (*)

					parsingState = ParsingState.ParsingNameOrContinuation;

				}

			} else {

				assert parsingState == ParsingState.ParsingNameOrContinuation;

				// if (isNewline(ch)) {

				// emitValueToken();

				// emitAccumulatedNewlineTokens(); // from (*) above

				// processNewlines(true);

				// } else

				if (ch == SPACE) {

					// continuation

					newlineTokens.clear(); // will not need these

					parsingState = ParsingState.ParsingValue;

					dataPos++;

				} else if (isAlphanumeric(ch)) {

					// on the next name, that value was complete

					emitValueToken(startPosition, possibleTokenEnd);

					emitAccumulatedNewlineTokens(); // from (*) above

					// new name

					startPosition = dataPos;

					tokenPos = 0;

					token[tokenPos++] = ch;

					parsingState = ParsingState.ParsingName;

					dataPos++;

				} else {

					// ERROR: whilst expecting new Name or a continuation,

					// encountered another char

					// RECOVERY: assume it was meant to be a continuation and

					// the initial SPACE is missing

					assert !isNewline(ch); // Cannot be, will have been dealt

					// with before entering this state

					recordProblem(ManifestProblemKind.MISSING_SPACE_FOR_CONTINUATION, dataPos, dataPos + 1);

					newlineTokens.clear();

					token[tokenPos++] = ch;

					parsingState = ParsingState.ParsingValue;

					dataPos++;

					// throw new

					// InternalManifestProcessingException("No idea how to deal with '"

					// + ch + "' whilst in state "

					// + parsingState);

				}

			}

		}

		// End of the data - must be in default state

		assert parsingState == ParsingState.Default;

		tokenStream.getSourceContext().setLinebreaks(linebreaks);

		tokenStream.lexComplete();

	}



	// Note on token emission:

	// If the start and end position are the same, it is a phantom token -

	// inserted by the lexer because of malformed input data.

	// Phantom tokens enable the parser to be straightforward because it will

	// not see bad data.



	private int correctMaxPosition(int pos) {
	    return pos;

//		if (tokenPos < MAX_TOKEN_LENGTH) {

//			return tokenPos;

//		}

//		return MAX_TOKEN_LENGTH - 1;

	}



	private void emitNameToken() {

		lastEmittedNameToken = ManifestToken.makeName(subarray(token, 0, correctMaxPosition(tokenPos)), startPosition, dataPos);

		tokenStream.addToken(lastEmittedNameToken);

	}



	private void emitColonToken(int spos, int epos) {

		tokenStream.addToken(ManifestToken.makeColon(spos, epos));

	}



	private void emitPhantomColonToken(int pos) {

		tokenStream.addToken(ManifestToken.makeColon(pos, pos));

	}



	private void emitValueToken(int spos, int epos) {

		tokenStream.addToken(ManifestToken.makeValue(subarray(token, 0, correctMaxPosition(tokenPos)), spos, epos));

	}



	private void emitPhantomValueToken(int pos) {

		tokenStream.addToken(ManifestToken.makeValue(BLANK_TOKEN_VALUE, pos, pos));

	}



	private void emitAccumulatedNewlineTokens() {

		for (Token newlineToken : newlineTokens) {

			tokenStream.addToken(newlineToken);

		}

		newlineTokens.clear();

	}



	private void startNewToken() {

		startPosition = dataPos;

		tokenPos = 0;

		token[tokenPos++] = data[dataPos];

	}



	private void recordProblem(ManifestProblemKind problemKind, int start, int end, String... inserts) {

		tokenStream.recordProblem(new ManifestProblem(problemKind, context, start, end, inserts));

	}



	public static final char[] subarray(char[] array, int start, int end) {

		char[] result = new char[end - start];

		System.arraycopy(array, start, result, 0, end - start);

		return result;

	}



	public static final String substring(char[] array, int start, int end) {

		return new String(array, start, end - start);

	}



	// Private bit flags for fast detection of char types

	private static byte[] fastCharLookup = new byte[256];

	private final static byte letterChar = 0x01;

	private final static byte digitChar = 0x02;

	private final static byte underscoreHyphenChar = 0x04;

	private final static byte newlineChar = 0x10;

	private final static byte alphanumMask = letterChar | digitChar;

	private final static byte nameMask = letterChar | digitChar | underscoreHyphenChar;



	static {



		int i;

		for (i = 0; i < 256; i++) {

			fastCharLookup[i] = 0;

		}

		for (i = 'a'; i <= 'z'; i++) {

			fastCharLookup[i] |= letterChar;

		}

		for (i = 'A'; i <= 'Z'; i++) {

			fastCharLookup[i] |= letterChar;

		}

		for (i = '0'; i <= '9'; i++) {

			fastCharLookup[i] |= digitChar;

		}

		fastCharLookup['-'] |= underscoreHyphenChar;

		fastCharLookup['_'] |= underscoreHyphenChar;

		fastCharLookup['\r'] |= newlineChar;

		fastCharLookup['\n'] |= newlineChar;

	}



	// private static boolean isAlpha(char ch) {

	// if (ch > 256) {

	// return false;

	// }

	// return (fastCharLookup[ch] & letterChar) != 0;

	// }



	// private static boolean isDigit(char ch) {

	// if (ch > 256) {

	// return false;

	// }

	// return (fastCharLookup[ch] & digitChar) != 0;

	// }



	private static boolean isAlphanumeric(char ch) {

		if (ch > 256) {

			return false;

		}

		return (fastCharLookup[ch] & alphanumMask) != 0;

	}



	private static boolean isNameChar(char ch) {

		if (ch > 255) {

			return false;

		}

		return (fastCharLookup[ch] & nameMask) != 0;

	}



	private static boolean isNewline(char ch) {

		if (ch > 255) {

			return false;

		}

		return (fastCharLookup[ch] & newlineChar) != 0;

	}



	/**

	 * Process newlines from the currentposition until something other than a

	 * newline is encountered. Newlines are either "\n" or "\r" or "\n\r". The

	 * parameter emitToTokenStreamImmediately determines whether tokens for the

	 * newlines should be emitted to the tokenstream immediately or recorded for

	 * emitting later. The reason a caller may want to emit them later is that

	 * they are making a decision based on: (1) whether there was a blankline

	 * (2) what the next character is after the newlines

	 * 

	 * @param emitToTokenStreamImmediately whether to emit the Newline tokens

	 *            immediately or simply record for later emission

	 * @return true if a blank line was found (ie. at least two Newlines

	 *         together)

	 */

	private boolean processNewlines(boolean emitToTokenStreamImmediately) {

		assert newlineTokens.size() == 0;// If not zero then someone forgot to

		// deal with them

		char ch = data[dataPos];

		assert isNewline(ch);

		newlineTokens.clear();

		int newlines = 0;

		do {

			newlines++;

			int toskip = 1;

			if (ch == '\r' && ((dataPos + 1) < dataLength) && data[dataPos + 1] == '\n') {

				// need to skip over the following LF too

				toskip++;

			}

			Token newlineToken = ManifestToken.makeNewline(dataPos, dataPos + toskip);

			if (emitToTokenStreamImmediately) {

				tokenStream.addToken(newlineToken);

			} else {

				newlineTokens.add(newlineToken);

			}

			linebreaks.add(dataPos + toskip);

			dataPos += toskip;

		} while (dataPos < dataLength && isNewline(ch = data[dataPos]));

		return newlines > 1; // If >1 then a blank line has been encountered

	}



	// For lexer debugging, gives stream position

	// @Override

	// public String toString() {

	// StringBuilder sb = new StringBuilder();

	// sb.append("Manifest Lexer.  Data length = ").append(dataLength).append("\n");

	// for (int i = 0; i < dataLength; i++) {

	// if (data[i] == '\n' || data[i] == '\r') {

	// sb.append("~");

	// } else {

	// sb.append(data[i]);

	// }

	// }

	// sb.append("\n");

	// for (int i = 0; i < dataPos; i++) {

	// sb.append(" ");

	// }

	// sb.append("^");

	// sb.append("\n");

	// sb.append(tokenStream.toFormattedString(true));

	// sb.append("\n");

	// return sb.toString();

	// }



	private enum ParsingState {

		// In Default state we are expecting a name

		Default, ParsingName, ParsingValue, ParsingValueHeaderChar, ParsingNameOrContinuation

	}

}

