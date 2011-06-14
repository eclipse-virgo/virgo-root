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
 * Enumeration of token kinds created by the lexer. Very confusing that the grammar defines a token called TOKEN.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum HeaderTokenKind {
    NUMBER("Number", true), //

    ALPHAS("Alphas", true), //

    ALPHANUMERIC("Alphanumeric", true), //

    DOT("."), //

    DOTSTAR(".*"), //

    TOKEN("Token", true), //

    QUOTEDSTRING("QuotedString"), //

    IDENTIFIER("Identifier"), //

    SEMICOLON(";"), //

    COLONEQUALS(":="), //

    COMMA(","), //

    EQUALS("="), //

    STAR("*"), //

    SLASH("/"), //

    PATHELEMENT("PathElement");

    private String tokenString;

    private boolean canBeTreatedAsToken; // Number/Alphas/Alphanumeric can all be treated as TOKEN


    private HeaderTokenKind(String messageString, boolean canBeTreatedAsToken) {
        this.tokenString = messageString;
        this.canBeTreatedAsToken = canBeTreatedAsToken;
    }

    private HeaderTokenKind(String messageString) {
        this(messageString, false);
    }

    public String getString() {
        return tokenString;
    }

    public static boolean isNumber(HeaderToken token) {
        return token.getKind() == NUMBER;
    }

    public static boolean isAlphas(HeaderToken token) {
        return token.getKind() == ALPHAS;
    }

    public static boolean isAlphanumeric(HeaderToken token) {
        return token.getKind() == ALPHANUMERIC;
    }

    public static boolean isToken(HeaderToken token) {
        return token.getKind() == TOKEN;
    }

    public static boolean isEquals(HeaderToken token) {
        return token.getKind() == EQUALS;
    }

    public static boolean isColonEquals(HeaderToken token) {
        return token.getKind() == COLONEQUALS;
    }

    public static boolean isQuotedString(HeaderToken token) {
        return token.getKind() == QUOTEDSTRING;
    }

    public static boolean isDot(HeaderToken token) {
        return token.getKind() == DOT;
    }

    public static boolean isSemicolon(HeaderToken token) {
        return token.getKind() == SEMICOLON;
    }

    public static boolean isSlash(HeaderToken token) {
        return token.getKind() == SLASH;
    }

    public static boolean isComma(HeaderToken token) {
        return token.getKind() == COMMA;
    }

    public static boolean isIdentifier(HeaderToken token) {
        return token.getKind() == IDENTIFIER;
    }

    public static boolean canBeTreatedAsIdentifier(HeaderToken t) {
        return t.getKind() == IDENTIFIER || t.getKind().canBeTreatedAsToken;
        
        //remove check for number-starting packages to align with Equinox's header parser behavior
        // this can be enabled at a later point if required
        //    && (t.firstCharIsLetter() || Character.isJavaIdentifierStart(t.firstChar()));
    }

    public static boolean canBeTreatedAsToken(HeaderToken token) {
        return token.getKind().canBeTreatedAsToken;
    }

    public static boolean canBeTreatedAsExtendedToken(HeaderToken argumentToken) {
        return canBeTreatedAsToken(argumentToken) || isDot(argumentToken);
    }

    public static boolean isSemicolonOrComma(HeaderToken t) {
        HeaderTokenKind k = t.getKind();
        return k == SEMICOLON || k == COMMA;
    }

    public static boolean isStar(HeaderToken token) {
        return token.getKind() == STAR;
    }

    public static boolean isDotStar(HeaderToken token2) {
        return token2.getKind() == DOTSTAR;
    }

    public static boolean canBeTreatedAsPathElement(HeaderToken t) {
        if (t == null) {
            return false;
        }
        HeaderTokenKind k = t.getKind();
        boolean pathElement = (k == DOT || k == STAR || k == DOTSTAR || k == COLONEQUALS || k == PATHELEMENT || canBeTreatedAsIdentifier(t));
        if (pathElement) {
            return true;
        } else {
            // one last special check because of hyphens
            return (k == TOKEN && t.firstChar() == '-');
        }
    }
}
