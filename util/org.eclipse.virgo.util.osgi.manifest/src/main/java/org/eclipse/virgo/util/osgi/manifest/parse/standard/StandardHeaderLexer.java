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

/**
 * 
 * Lex an input string into a series of tokens. The possible tokens are defined
 * 
 * in {@link HeaderTokenKind}
 * 
 * 
 * 
 * Basic token descriptions:
 * 
 * <ul>
 * 
 * <li>digit ::= [0..9]
 * 
 * <li>alpha ::= [a..zA..Z]
 * 
 * <li>alphanum ::= alpha | digit
 * 
 * <li>token ::= ( alphanum | '_' | '-' | '.')+
 * 
 * <li>number ::= digit+
 * 
 * <li>jletter ::= <see [5] Lexical Structure Java Language for
 * 
 * JavaLetter>Character.isJavaIdentifierStart()
 * 
 * <li>jletterordigit::= <See [5] Lexical Structure Java Language for
 * 
 * JavaLetterOrDigit > Character.isJavaIdentifierPart()
 * 
 * <li>identifier ::= jletter jletterordigit *
 * 
 * <li>quoted-string ::= '"' ( [^"\#x0D#x0A#x00] | '\"'|'\\')* '"'
 * 
 * <li>DOT ::= .
 * 
 * <li>COLON_EQUALS ::= ':='
 * 
 * <li>EQUALS ::= '='
 * 
 * <li>DOTSTAR ::= .*
 * 
 * </ul>
 * 
 * 
 * 
 * header ::= clause ( ',' clause ) *
 * 
 * clause ::= path ( ';' path ) * ( ';' parameter ) *
 * 
 * 
 * 
 * path ::= path-unquoted | ('"' path-unquoted '"')
 * 
 * path-unquoted ::= path-sep | path-sep? path-element (path-sep path-element)*
 * 
 * path-element ::= [^/"\#x0D#x0A#x00]+
 * 
 * path-sep ::= '/'
 * 
 * 
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * 
 * 
 * Threadsafe.
 * 
 * 
 */

public class StandardHeaderLexer {

    private HeaderTokenStream tokenStream;

    private int tokenStart;

    private boolean tokenStartedWithLetter;

    char[] data; // visible directly to parser

    private int state;

    private int datapos; // where are we in the current data being lexed

    private int datalen; // how long is the data

    // Attribute/directive values in the header support an extended token - that is a token which also includes Dots.
    // There is no

    // notion of an 'extended' token, instead we produce the regular tokens: TOKEN,DOT,TOKEN,DOT,TOKEN but where it is

    // a valid extension we give the first token in the list a special end offset indicating the end of the final token
    // in the

    // extension.

    private BasicHeaderToken extensionStart = null;

    private BasicHeaderToken lastEmittedToken = null;

    private boolean foundSpace = false;

    private boolean allowsPathToken = false; // general headers following OSGI 3.2.4 Common Header Syntax allow paths

    public StandardHeaderLexer() {

    }

    public StandardHeaderLexer(boolean allowsPathToken) {

        this.allowsPathToken = allowsPathToken;

    }

    public HeaderTokenStream getTokenStream() {

        return tokenStream;

    }

    private void initializeLexer(String header) {

        // Create a NUL (0) terminated character array - by setting such a terminal we don't have to keep checking the
        // length as we

        // move along the data

        int stringlen = header.length();

        data = new char[stringlen + 1];

        header.getChars(0, stringlen, data, 0);

        data[stringlen] = 0;

        tokenStream = new HeaderTokenStream(header);

        datapos = 0;

        tokenStart = -1;

        datalen = data.length;

    }

    /**
     * 
     * Start in the UNKNOWN state where anything may come next, as characters are encountered and consumed the state
     * will transition
     * 
     * amongst other states. It is operating on a maximal munch approach where we eat as much data as we can before
     * being forced
     * 
     * into a new state and onto a new token.
     * 
     * @param header string
     */

    public void process(String header) {

        initializeLexer(header);

        state = UNKNOWN;

        while (datapos < datalen) {

            char ch = data[datapos];

            if (isDigit(ch)) {

                processDigit();

            } else if (isAlphabetic(ch)) {

                processAlphabetic();

            } else if (isUnderlineMinus(ch)) {

                processUnderlineMinus();

            } else if (ch == '"') {

                processQuote();

            } else if (ch == '/') {

                processSlash();

            } else if (ch == 0) {

                processNul();

            } else if (ch == ':') {

                processColon();

            } else if (ch == ';') {

                emitToken(datapos);

                emitToken2(HeaderTokenKind.SEMICOLON, datapos++);

            } else if (ch == '=') {

                emitToken(datapos);

                lastEmittedToken.tagAsAttributeName();

                emitToken2(HeaderTokenKind.EQUALS, datapos++);

            } else if (ch == ',') {

                emitToken(datapos);

                emitToken2(HeaderTokenKind.COMMA, datapos++);

            } else if (ch == '.') {

                processDot();

            } else if (ch == '*') {

                processStar();

            } else if (Character.isJavaIdentifierStart((int) ch)) {

                processJavaIdentifierStart();

            } else if (Character.isJavaIdentifierPart((int) ch)) {

                processJavaIdentifierPart();

            } else {

                processUnexpected();

            }

        }

        if (state != UNKNOWN) {

            emitToken(datapos);

        }

        tokenStream.lexComplete();

    }

    private void processSpace() {

        // ERROR: hit whitespace outside of a quoted string

        // RECOVER: emit what we have, skip the space, log a message

        if (state != UNKNOWN) {

            emitToken(datapos);

        }

        if (lastEmittedToken != null) {

            lastEmittedToken.tagAsFollowedBySpace();

        }

        char ch = data[datapos];

        recordProblem(HeaderProblemKind.UNEXPECTED_SPACE_WARNING, datapos, datapos, Character.toString(ch), Integer.toString(ch));

        foundSpace = true;

        datapos++;

    }

    private void processDot() {

        if (lastEmittedToken != null && lastEmittedToken.getKind() == HeaderTokenKind.SEMICOLON) {

            if (state == UNKNOWN) {

                processDotOrigin();

            } else if (state == DIGITS || state == ALPHABETIC || state == ALPHANUMERIC || state == TOKEN) {

                state = TOKEN;

                do {

                    // is ".*" ?

                    if (data[datapos + 1] == '*' && data[datapos] == '.') {

                        datapos++;

                    }

                    datapos++;

                } while (isToken(data[datapos]));

            } else {

                assert state == QUOTEDSTRING;

                emitToken(datapos);

                state = UNKNOWN;

            }
        } else {

            processDotOrigin();

        }

    }

    private void processDotOrigin() {

        emitToken(datapos); // emit what we have so far

        boolean isDotStar = (data[datapos + 1] == '*');

        if (isDotStar) {

            emitDotStar(datapos);

            datapos += 2;

        } else {

            emitToken(HeaderTokenKind.DOT, datapos);

            datapos++;

        }

    }

    private void processSlash() {

        if (!allowsPathToken) {

            char ch = data[datapos];

            recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, Character.toString(ch), Integer.toString(ch));

            datapos++;

        } else {

            emitToken(datapos);

            emitToken(HeaderTokenKind.SLASH, datapos);

            datapos++;

        }

    }

    private void processStar() {

        emitToken(datapos); // emit what we have so far

        emitToken(HeaderTokenKind.STAR, datapos);

        datapos++;

    }

    private void processUnexpected() {

        // TODO [later] perhaps cope with some typical cases here

        // ERROR: encountered something unexpected

        // RECOVERY: could special case in here, for now skip whitespace and record an issue with everything else

        if (data[datapos] == ' ') {

            processSpace();

        } else {

            // If the lexer allows for paths then we have to cope with all the wierd chars and build a pathelement for
            // them

            if (allowsPathToken) {

                // path elements can be ANYTHING except: / " \ #x0D #x0A# x00

                state = PATHELEMENT;

                while (!pathEnd(data[datapos])) {

                    datapos++;

                }

                emitToken(datapos);

                char ch = data[datapos];

                if (pathEnd(ch) && ch != 0) {

                    recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, Character.toString(ch), Integer.toString(ch));

                    datapos++;

                }

            } else {

                emitToken(datapos);

                char ch = data[datapos];

                recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, Character.toString(ch), Integer.toString(ch));

                datapos++;

            }

        }

    }

    private boolean pathEnd(char ch) {

        return (ch == 0 || ch == '/' || ch == '\"' || ch == '\n' || ch == '\r' || ch == '\\' || ch == ' ');

    }

    private void processNul() {

        if (tokenStart != -1) {

            emitToken(datapos);

            // If the stream has ended with an ExtendedToken

            if (extensionStart != null && lastEmittedToken != extensionStart) {

                extensionStart.setExtendedOffset(lastEmittedToken.getEndOffset());

            }

        }

        datapos = datalen; // end of data reached

    }

    private void processDigit() {

        if (state == UNKNOWN) {

            // start new token, looks like it will be a number

            state = DIGITS;

            tokenStart = datapos++;

            while (isDigit(data[datapos])) {

                datapos++;

            }

        } else if (state == ALPHABETIC || state == ALPHANUMERIC) {

            // Digit encountered here where everything previously was alphabetic or alphanumeric, upgrade what we
            // currently

            // think it is to ALPHANUMERIC

            state = ALPHANUMERIC;

            do {

                datapos++;

            } while (isAlphanumeric(data[datapos]));

        } else {

            // assert - cannot be in TOKEN or IDENTIFIER state because those states would have continued to consume this
            // digit

            assert state == QUOTEDSTRING;

            emitToken(datapos);

            state = UNKNOWN;

        }

    }

    private void processAlphabetic() {

        if (state == UNKNOWN) {

            // start new token, assuming it will be an alphabetic string

            tokenStart = datapos;

            state = ALPHABETIC;

            tokenStartedWithLetter = true;

            while (isAlphabetic(data[++datapos])) {
            }

        } else if (state == DIGITS || state == ALPHANUMERIC) {

            state = ALPHANUMERIC;

            while (isAlphanumeric(data[++datapos])) {
            }

        } else {

            assert state == QUOTEDSTRING;

            emitToken(datapos);

            state = UNKNOWN;

        }

    }

    private void processUnderlineMinus() {

        if (state == UNKNOWN || state == DIGITS || state == ALPHABETIC || state == ALPHANUMERIC) {

            if (state == UNKNOWN) {

                tokenStart = datapos;

            }

            state = TOKEN;

            do {

                datapos++;

            } while (isToken(data[datapos]));

        } else {

            assert state == QUOTEDSTRING;

            emitToken(datapos);

            state = UNKNOWN;

        }

    }

    private void processQuote() {

        if (state != UNKNOWN) {

            // Finish whatever was before and process this quoted string

            emitToken(datapos);

            state = UNKNOWN;

        }

        tokenStart = datapos;

        state = QUOTEDSTRING;

        boolean run = true;

        boolean escape = false;// encountered a backslash

        while (run) {

            char ch = data[++datapos];

            if (ch == '\\') {

                escape = true;

            } else {

                if (!escape) {

                    if (ch == '"') {

                        run = false;

                    }

                    if (ch == '\r' || ch == '\n') {

                        recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, Character.toString(ch), Integer

                        .toString(ch));

                    }

                }

                if (ch == 0) {

                    // hit end of the data before string terminated

                    recordProblem(HeaderProblemKind.NON_TERMINATING_QUOTED_STRING, tokenStart, datapos);

                    run = false;

                }

                escape = false;

            }

        }

        datapos++;

    }

    private void processJavaIdentifierPart() {

        if (state == ALPHABETIC || (state == ALPHANUMERIC && tokenStartedWithLetter)

        || (state == TOKEN && (data[tokenStart] == '_' || isAlphabetic(data[tokenStart])))) {

            state = IDENTIFIER;

            char ch = 0;

            do {

                ch = data[++datapos];

            } while (ch != 0 && Character.isJavaIdentifierPart(ch));

        } else {

            // ERROR: hit a JavaIdentifierPart in an unexpected State

            // RECOVERY: emit anything we have been processing and skip over this character

            // Unexpected states will be: UNKNOWN, DIGITS, ALPHANUMERIC (where first tokenchar is a number), TOKEN
            // (where first

            // token char is not a letter or underscore), IDENTIFIER (can't happen...), QUOTEDSTRING

            if (state != UNKNOWN) {

                emitToken(datapos);

            }

            char ch = data[datapos];

            recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, Character.toString(ch), Integer.toString(ch));

            datapos++;

        }

    }

    private void processColon() {

        if (state != UNKNOWN) {

            emitToken(datapos);

        }

        boolean isColonEquals = (data[datapos + 1] == '=');

        if (isColonEquals) {

            lastEmittedToken.tagAsDirectiveName();

            emitColonEquals(datapos);

            datapos += 2;

        } else {

            // ERROR: cannot have colon by itself

            // RECOVERY: record a problem and skip over it

            recordProblem(HeaderProblemKind.UNEXPECTED_CHARACTER, datapos, datapos, ":", Integer.toString(':'));

            datapos++;

        }

    }

    private void processJavaIdentifierStart() {

        if (state == QUOTEDSTRING) {

            emitToken(datapos);

            state = UNKNOWN;

        }

        if (state == UNKNOWN) {

            tokenStart = datapos;

        }

        state = IDENTIFIER;

        char ch = 0;

        do {

            datapos++;

            ch = data[datapos];

        } while (ch != 0 && Character.isJavaIdentifierPart(ch));

    }

    private boolean isDigit(int ch) {

        if (ch > 255) {

            return false;

        }

        return (lookup[ch] & IS_DIGIT) != 0;

    }

    private boolean isAlphabetic(int ch) {

        if (ch > 255) {

            return false;

        }

        return (lookup[ch] & IS_ALPHA) != 0;

    }

    private boolean isAlphanumeric(int ch) {

        if (ch > 255) {

            return false;

        }

        return (lookup[ch] & IS_ALPHANUM) != 0;

    }

    private boolean isUnderlineMinus(int ch) {

        if (ch > 255) {

            return false;

        }

        return (lookup[ch] & IS_UNDERLINE_OR_MINUS) != 0;

    }

    private boolean isToken(int ch) {

        if (ch > 255) {

            return false;

        }

        if (state == TOKEN) {

            if ((ch == '.' || ch == '*') && lastEmittedToken != null && lastEmittedToken.getKind() == HeaderTokenKind.SEMICOLON) {

                if (extensionStart != null) {

                    return true;

                }

            }

        }

        return (lookup[ch] & IS_TOKEN) != 0;

    }

    private final static int UNKNOWN = 0;

    private final static int DIGITS = 1;

    private final static int ALPHABETIC = 2;

    private final static int ALPHANUMERIC = 3;

    private final static int TOKEN = 4;

    private final static int IDENTIFIER = 5;

    private final static int QUOTEDSTRING = 6;

    private final static int PATHELEMENT = 7;

    private static final HeaderTokenKind[] stateToTokenMap = new HeaderTokenKind[] { null, HeaderTokenKind.NUMBER,

    HeaderTokenKind.ALPHAS, HeaderTokenKind.ALPHANUMERIC, HeaderTokenKind.TOKEN, HeaderTokenKind.IDENTIFIER,

    HeaderTokenKind.QUOTEDSTRING, HeaderTokenKind.PATHELEMENT };

    private static final boolean[] tokenExtension = new boolean[] { false, true, true, true, true, false, false, true };

    private void emitToken(HeaderTokenKind kind, int pos) {

        pushExtensionToken(BasicHeaderToken.makeToken(data, kind, pos, pos + 1));

    }

    private void emitToken2(HeaderTokenKind kind, int pos) {

        pushToken(BasicHeaderToken.makeToken(data, kind, pos, pos + 1));

    }

    private void emitDotStar(int pos) {

        pushToken(BasicHeaderToken.makeToken(data, HeaderTokenKind.DOTSTAR, pos, pos + 2));

    }

    private void emitColonEquals(int pos) {

        pushToken(BasicHeaderToken.makeToken(data, HeaderTokenKind.COLONEQUALS, pos, pos + 2));

    }

    private void emitToken(int tokenEnd) {

        HeaderTokenKind kind = stateToTokenMap[state];

        if (kind == null) {

            return; // nothing to emit

        } else {

            if (kind == HeaderTokenKind.QUOTEDSTRING) {

                tokenStart += 1;

                tokenEnd -= 1;

            }

            BasicHeaderToken newToken = BasicHeaderToken.makeToken(data, kind, tokenStart, tokenEnd);

            if (tokenStartedWithLetter) {

                newToken.tagAsStartedWithLetter();

            }

            if (tokenExtension[state]) {

                pushExtensionToken(newToken);

            } else {

                pushToken(newToken);

            }

        }

        state = UNKNOWN;

        tokenStartedWithLetter = false;

        tokenStart = -1;

    }

    private void pushExtensionToken(BasicHeaderToken token) {

        if (extensionStart == null) {

            extensionStart = token; // set this as first element of an extended token

        } else if (foundSpace) {

            // this extension has a space in it... not good if someone wants to consume it as an extended token eg.

            // "com. foo.bar"

            extensionStart.tagAsSpaced();

        }

        foundSpace = false;

        lastEmittedToken = token;

        tokenStream.addToken(token);

    }

    private void pushToken(BasicHeaderToken token) {

        if (extensionStart != null) {

            if (lastEmittedToken != extensionStart) {

                extensionStart.setExtendedOffset(lastEmittedToken.getEndOffset());

            }

            extensionStart = null;

        }

        foundSpace = false;

        lastEmittedToken = token;

        tokenStream.addToken(token);

    }

    // Fast lookup table for determining kind of character

    private static final byte[] lookup;

    private static final byte IS_DIGIT = 0x0001;

    private static final byte IS_ALPHA = 0x0002;

    private static final byte IS_UNDERLINE_OR_MINUS = 0x0004;

    private static final byte IS_ALPHANUM = IS_DIGIT | IS_ALPHA;

    private static final byte IS_TOKEN = IS_ALPHANUM | IS_UNDERLINE_OR_MINUS;

    static {

        lookup = new byte[256];

        for (int ch = '0'; ch <= '9'; ch++) {

            lookup[ch] |= IS_DIGIT;

        }

        for (int ch = 'a'; ch <= 'z'; ch++) {

            lookup[ch] |= IS_ALPHA;

        }

        for (int ch = 'A'; ch <= 'Z'; ch++) {

            lookup[ch] |= IS_ALPHA;

        }

        lookup['_'] |= IS_UNDERLINE_OR_MINUS;

        lookup['-'] |= IS_UNDERLINE_OR_MINUS;

    }

    /**
     * 
     * Record a problem with lexing.
     * 
     * 
     * 
     * @param parseProblem the kind of problem that occurred
     * 
     * @param startOffset the start offset of the problem
     * 
     * @param endOffset the end offset of the problem
     * 
     * @param inserts the inserts for the problem message text
     */

    private void recordProblem(HeaderProblemKind parseProblem, int startOffset, int endOffset, String... inserts) {

        tokenStream.recordProblem(new HeaderProblem(parseProblem, startOffset, endOffset, inserts));

    }

}
