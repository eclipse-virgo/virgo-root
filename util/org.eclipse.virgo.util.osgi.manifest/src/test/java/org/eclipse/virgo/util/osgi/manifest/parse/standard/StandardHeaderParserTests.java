/*
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 */

package org.eclipse.virgo.util.osgi.manifest.parse.standard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.virgo.util.osgi.manifest.parse.BundleManifestParseException;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test header parsing - normal and error scenarios.
 * 
 */
public class StandardHeaderParserTests {

    private static ParserLogger tlogger = new TestLogger();

    private static final char oUmlaut = '\u00f6';

    private static final char aeDipthong = '\u00c6';

    @Test
    public void testPackageAttributeNameNastySplit() throws Exception {
        String test = "a.split.pkg;nasty.split=\"split\"";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        assertEquals(1, packageDeclarations.size());

        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("a.split.pkg", decl.getNames().get(0));
        assertEquals("split", decl.getAttributes().get("nasty.split"));
	}

    @Test
    public void testPackageAttributeNameWithDotsAndUnderscores() throws Exception {
        String test = "a.long.package.name;test.split_mixed_with.underscore=split";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        assertEquals(1, packageDeclarations.size());

        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("a.long.package.name", decl.getNames().get(0));
        assertEquals("split", decl.getAttributes().get("test.split_mixed_with.underscore"));
	}

    @Test
    public void testPackageAttributeNameWithUnderscore() throws Exception {
        String test = "package;test_split=split";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        assertEquals(1, packageDeclarations.size());

        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("package", decl.getNames().get(0));
        assertEquals("split", decl.getAttributes().get("test_split"));
	}

    @Test
    public void testPackageAttributeNameWithDot() throws Exception {
        String test = "package;test.split=split";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        assertEquals(1, packageDeclarations.size());

        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("package", decl.getNames().get(0));
        assertEquals("split", decl.getAttributes().get("test.split"));
	}

    @Test
    public void testParseGeneralHeaderError() {
        checkGeneralHeaderFailure("a\"wibble\"", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkGeneralHeaderFailure("a\"wibble\"/", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkGeneralHeaderFailure("a\"wibble\",", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkGeneralHeaderFailure("a\"wibble\";foo", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkGeneralHeaderFailure("a//", HeaderProblemKind.ILLEGAL_DOUBLE_SLASH);
        checkGeneralHeaderFailure("a.b\n", HeaderProblemKind.UNEXPECTED_CHARACTER);
        checkGeneralHeaderFailure("a.b .*", HeaderProblemKind.ILLEGAL_SPACE);
        checkGeneralHeaderFailure("a.b.*a.b;a=5.*", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkGeneralHeaderFailure("", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkGeneralHeaderFailure("a;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkGeneralHeaderFailure("a/ b", HeaderProblemKind.ILLEGAL_SPACE);
        checkGeneralHeaderFailure("a /b", HeaderProblemKind.ILLEGAL_SPACE);
        checkGeneralHeaderFailure("abc .goo", HeaderProblemKind.ILLEGAL_SPACE);
        checkGeneralHeaderFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.UNEXPECTED_SPACE_WARNING);
        checkGeneralHeaderFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkGeneralHeaderFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
    }

    @Test
    public void testBasicVisitors() {
        StandardHeaderVisitor visitor = new StandardHeaderVisitor();
        visitor.initialize();
        assertNull(visitor.getFirstHeaderDeclaration()); // should not NPE

        MultiplexingVisitor mv = new MultiplexingVisitor((HeaderVisitor) null);
        assertNotNull(mv); // stop findBugs whining

        mv = new MultiplexingVisitor();
        assertNotNull(mv); // stop findBugs whining

        mv = new MultiplexingVisitor(visitor);
        assertNull(mv.getFirstHeaderDeclaration());
    }

    @Test
    public void testPossibleGeneralHeaders() {
        checkGeneralHeader("a", "names=[a]");
        checkGeneralHeader("a.b", "names=[a.b]");
        checkGeneralHeader("\"a/b/c.xml\"", "names=[a/b/c.xml]");
        checkGeneralHeader("a/b.xml", "names=[a/b.xml]");
        checkGeneralHeader("config/account-data-context.xml, config/account-security-context.xml", "names=[config/account-data-context.xml]",
            "names=[config/account-security-context.xml]");
        checkGeneralHeader("a/b.xml;a=b", "names=[a/b.xml] attrs=[a=b]");
        checkGeneralHeader("a/b.xml;a:=b", "names=[a/b.xml] directives=[a:=b]");
        checkGeneralHeader("a/b.xml;a=b,c.foo;a=b", "names=[a/b.xml] attrs=[a=b]", "names=[c.foo] attrs=[a=b]");
        checkGeneralHeader("a/b.xml   ;    a=b,c.foo ; a=b", "names=[a/b.xml] attrs=[a=b]", "names=[c.foo] attrs=[a=b]");
        checkGeneralHeader("*", "names=[*]");
        checkGeneralHeader("*/*", "names=[*/*]");
        checkGeneralHeader("/", "names=[/]");
        checkGeneralHeader("*;create-asynchronously:=false", "names=[*] directives=[create-asynchronously:=false]");
        checkGeneralHeader("config/account-data-context.xml;create-asynchronously:=false",
            "names=[config/account-data-context.xml] directives=[create-asynchronously:=false]");
        checkGeneralHeader("config/osgi-*.xml;wait-for-dependencies:=false", "names=[config/osgi-*.xml] directives=[wait-for-dependencies:=false]");
        checkGeneralHeader("*;timeout:=60", "names=[*] directives=[timeout:=60]");
        checkGeneralHeader("*;publish-context:=false", "names=[*] directives=[publish-context:=false]");
        checkGeneralHeader("org.springframework.osgi.extender", "names=[org.springframework.osgi.extender]");
        checkGeneralHeader(".*", "names=[.*]");
        checkGeneralHeader("a.b.*a.b", "names=[a.b.*a.b]");
        checkGeneralHeader("a.c+b.*a.b", "names=[a.c+b.*a.b]");
        checkGeneralHeader("abc.", "names=[abc.]");
        checkGeneralHeader("a:=,foo", "names=[a:=]", "names=[foo]");
        checkGeneralHeader("a/", "names=[a/]");
        checkGeneralHeader("a/b", "names=[a/b]");
    }

    @Test
    public void testInfraDebugVisitor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugVisitor dv = new DebugVisitor(); // check default ctor

        assertNotNull(dv); // stop findBugs whining

        dv = new DebugVisitor(new PrintStream(baos));
        new StandardHeaderParser(dv, tlogger).parseImportBundleHeader("a.b;x=5;y:=\"a.b.c\"");
        checkVisited(baos, "visitSymbolicName(a.b) visitAttribute(x=5) visitDirective(y:=a.b.c) clauseEnded() endVisit()");
        assertNull(dv.getFirstHeaderDeclaration());
        assertEquals(0, dv.getHeaderDeclarations().size());
    }

    @Test
    public void testInfraDebugVisitor2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugVisitor dv = new DebugVisitor(new PrintStream(baos));
        new StandardHeaderParser(dv, tlogger).parseDynamicImportPackageHeader("a.b.*;*");
        checkVisited(baos, "visitWildcardName(a.b.*) visitWildcardName(*) clauseEnded() endVisit()");
    }

    @Test
    public void testInfraDebugVisitor3() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugVisitor dv = new DebugVisitor(new PrintStream(baos));
        List<HeaderDeclaration> decls = new StandardHeaderParser(dv, tlogger).parsePackageHeader("com.foo.goo", "MyHeaderName");
        checkVisited(baos, "visitUniqueName(com.foo.goo) clauseEnded() endVisit()");
        assertNotNull(decls.get(0).toString());
    }

    @Test
    public void testInfraVisitorReset() {
        StandardHeaderParser shp = new StandardHeaderParser(tlogger);
        List<HeaderDeclaration> lhd = shp.parsePackageHeader("com.foo.goo;a=b", "MyHeaderName");
        assertEquals(1, lhd.size());
        lhd = shp.parsePackageHeader("com.foo.hoo;a:=c", "MyHeaderName");
        assertEquals(1, lhd.size());
        lhd = shp.parsePackageHeader("com.foo.boo;b:=f", "MyHeaderName");
        assertEquals(1, lhd.size());
    }

    // 3.5.2 BundleSymbolicName

    // Bundle-SymbolicName ::= symbolic-name ( ';' parameter )*

    @Test
    public void testBundleSymbolicName() {
        checkDecl(getParser().parseBundleSymbolicName("com.b.goo.foo"), "names=[com.b.goo.foo]");
        checkDecl(getParser().parseBundleSymbolicName("c.b.goo.foo;a:=1"), "names=[c.b.goo.foo] directives=[a:=1]");
        checkDecl(getParser().parseBundleSymbolicName("c.b.goo.foo;a=b"), "names=[c.b.goo.foo] attrs=[a=b]");
        checkDecl(getParser().parseBundleSymbolicName("c.b.goo.foo;a=b;c:=d"), "names=[c.b.goo.foo] attrs=[a=b] directives=[c:=d]");
        checkDecl(getParser().parseBundleSymbolicName("a.b.c;a=1;b=2;c:=d;e:=2"), "names=[a.b.c] attrs=[a=1 b=2] directives=[c:=d e:=2]");
        checkDecl(getParser().parseBundleSymbolicName("a;a=\"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseBundleSymbolicName("a ; a =    \"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseBundleSymbolicName("a  "), "names=[a]");
    }

    @Test
    public void testBundleSymbolicNameErrors() {
        checkBundleSymbolicNameFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkBundleSymbolicNameFailure("1;uses:=c" + oUmlaut + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkBundleSymbolicNameFailure("1;uses:=wibble.c" + oUmlaut + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkBundleSymbolicNameFailure("1;uses:=wibble.c" + oUmlaut + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
    }

    @Test
    public void testLibrarySymbolicName() {
        checkDecl(getParser().parseLibrarySymbolicName("com.b.goo.foo"), "names=[com.b.goo.foo]");
        checkDecl(getParser().parseLibrarySymbolicName("c.b.goo.foo;a:=1"), "names=[c.b.goo.foo] directives=[a:=1]");
        checkDecl(getParser().parseLibrarySymbolicName("c.b.goo.foo;a=b"), "names=[c.b.goo.foo] attrs=[a=b]");
        checkDecl(getParser().parseLibrarySymbolicName("c.b.goo.foo;a=b;c:=d"), "names=[c.b.goo.foo] attrs=[a=b] directives=[c:=d]");
        checkDecl(getParser().parseLibrarySymbolicName("a.b.c;a=1;b=2;c:=d;e:=2"), "names=[a.b.c] attrs=[a=1 b=2] directives=[c:=d e:=2]");
        checkDecl(getParser().parseLibrarySymbolicName("a;a=\"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseLibrarySymbolicName("a ; a =    \"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseLibrarySymbolicName("a  "), "names=[a]");

        checkLibrarySymbolicNameFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkLibrarySymbolicNameFailure("1;uses:=c" + oUmlaut + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkLibrarySymbolicNameFailure("1;uses:=wibble.c" + oUmlaut + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkLibrarySymbolicNameFailure("1;uses:=wibble.c" + oUmlaut + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
    }

    @Test
    public void testBundleSymbolicNameNastyErrors() {
        checkBundleSymbolicNameFailure(";", HeaderProblemKind.EXPECTED_TOKEN);
        checkBundleSymbolicNameFailure(".", HeaderProblemKind.EXPECTED_TOKEN);
        checkBundleSymbolicNameFailure("com.", HeaderProblemKind.TOKEN_CANNOT_END_WITH_DOT);
        checkBundleSymbolicNameFailure(".;.;", HeaderProblemKind.EXPECTED_TOKEN);
        // checkBundleSymbolicNameFailure(".;.;", HeaderProblemKind.UNCONSUMED_DATA);

        checkBundleSymbolicNameFailure("..;", HeaderProblemKind.EXPECTED_TOKEN);
        checkBundleSymbolicNameFailure(",a=b;", HeaderProblemKind.EXPECTED_TOKEN);
        checkBundleSymbolicNameFailure("a:=b;d", HeaderProblemKind.EXPECTED_SEMICOLON);
        checkBundleSymbolicNameFailure("a:=b;d", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
    }

    @Test
    public void testFragmentHostHeader() {
        checkDecl(getParser().parseFragmentHostHeader("com.b.goo.foo"), "names=[com.b.goo.foo]");
        checkDecl(getParser().parseFragmentHostHeader("c.b.goo.foo;a:=1"), "names=[c.b.goo.foo] directives=[a:=1]");
        checkDecl(getParser().parseFragmentHostHeader("c.b.goo.foo;a=b"), "names=[c.b.goo.foo] attrs=[a=b]");
        checkDecl(getParser().parseFragmentHostHeader("c.b.goo.foo;a=b;c:=d"), "names=[c.b.goo.foo] attrs=[a=b] directives=[c:=d]");
        checkDecl(getParser().parseFragmentHostHeader("a.b.c;a=1;b=2;c:=d;e:=2"), "names=[a.b.c] attrs=[a=1 b=2] directives=[c:=d e:=2]");
        checkDecl(getParser().parseFragmentHostHeader("a;a=\"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseFragmentHostHeader("a ; a =    \"foo\""), "names=[a] attrs=[a=foo]");
        checkDecl(getParser().parseFragmentHostHeader("a  "), "names=[a]");
    }

    // TODO Bug 463462 - As a developer I'd like to be able to build the Virgo artifacts with Gradle
    @Ignore("Deactivated with Bug 463462 - As a developer I'd like to be able to build the Virgo artifacts with Gradle")
    @Test
    public void testDirectivesAttributes() {
        checkFragmentHostHeaderFailure("a;x:=a/b", HeaderProblemKind.UNEXPECTED_CHARACTER);
        checkDecl(getParser().parseFragmentHostHeader("a;x:=y"), "names=[a] directives=[x:=y]");
    }

    @Test
    public void testFragmentHostHeaderErrors() {
        checkFragmentHostHeaderFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkFragmentHostHeaderFailure("1;uses:=c" + oUmlaut + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkFragmentHostHeaderFailure("1;uses:=wibble.c" + oUmlaut + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkFragmentHostHeaderFailure("1;uses:=wibble.c" + oUmlaut + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
    }

    @Test
    public void testHeaderProblemContext() {
        StandardHeaderParser parser = checkFragmentHostHeaderFailure("1;uses:=c" + oUmlaut + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        HeaderProblem hProblem = parser.getProblems().get(0);
        String s = hProblem.toStringWithContext(parser.getSourceContext());
        assertEquals(0, s.indexOf("1;uses:=c" + oUmlaut + "m.wibbl"));
        assertNotSame(-1, s.indexOf("        ^  ^"));
    }

    @Test
    public void testHeaderProblemContext2() {
        StandardHeaderParser parser = checkFragmentHostHeaderFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        HeaderProblem hProblem = parser.getProblems().get(0);
        String s = hProblem.toStringWithContext(parser.getSourceContext());
        System.out.println(s);
        assertEquals(0, s.indexOf("com .fo"));
        assertNotSame(-1, s.indexOf("   ^"));
    }

    @Test
    public void testVersioning() {
        checkDecl(getParser().parseBundleSymbolicName("a;version=1.2.3.qualifier"), "names=[a] attrs=[version=1.2.3.qualifier]");
        checkDecl(getParser().parseBundleSymbolicName("a;version=1.2.3"), "names=[a] attrs=[version=1.2.3]");
        checkDecl(getParser().parseBundleSymbolicName("a;version=1.2"), "names=[a] attrs=[version=1.2]");
        checkDecl(getParser().parseBundleSymbolicName("a;version=1"), "names=[a] attrs=[version=1]");
    }

    @Test
    public void testRequireBundle() {
        checkDecls(checkRequireBundle("a.b.c;f=5"), "names=[a.b.c] attrs=[f=5]");
        checkDecls(checkRequireBundle("a.b.c,f"), "names=[a.b.c]", "names=[f]");
        checkDecls(checkRequireBundle("a.b.c;uses:=a.b"), "names=[a.b.c] directives=[uses:=a.b]");
        checkDecls(checkRequireBundle("a.b.c;f=5,a.d,a.e"), "names=[a.b.c] attrs=[f=5]", "names=[a.d]", "names=[a.e]");
        checkDecls(checkRequireBundle("a.b.c;f=5,a.d,a.e"), "names=[a.b.c] attrs=[f=5]", "names=[a.d]", "names=[a.e]");
        checkDecls(checkRequireBundle("com.123"), "names=[com.123]");
        checkDecls(checkRequireBundle("com"), "names=[com]");
        checkDecls(checkRequireBundle("1"), "names=[1]");
        checkDecls(checkRequireBundle("a.b.c;f:=\"c�m\",a.d,a.e"), "names=[a.b.c] directives=[f:=c�m]", "names=[a.d]", "names=[a.e]");
    }

    @Test
    public void testRequireBundleFailures() {
        checkRequireBundleFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkRequireBundleFailure("1;uses:=c" + oUmlaut + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkRequireBundleFailure("1;uses:=wibble.c" + oUmlaut + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkRequireBundleFailure("1;uses:=wibble.c" + oUmlaut + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkRequireBundleFailure("a.b.c;f=5,a.d,a.e,", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkRequireBundleFailure("a.b.c;f=5,a.d,a.e;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkRequireBundleFailure("a.b.c.d;e.f.g.h;version=1.2.3;uses:=a.b.c", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkRequireBundleFailure("a.b.c;a.b.d;foo=bar", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkRequireBundleFailure("a.b.c;a.b.d", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkRequireBundleFailure("a.b.c;a=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkRequireBundleFailure("a.b.c;a:=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkRequireBundleFailure("a.b.c;a:= ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkRequireBundleFailure("  a.b.c ;  a := ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkRequireBundleFailure("c" + oUmlaut + "m.wibble", HeaderProblemKind.EXPECTED_TOKEN);
    }

    // These headers use the same parse method, parsePackageHeader()

    // 3.5.5 Export-Package

    // Export-Package ::= export ( ',' export )*

    // export ::= package-names ( ';' parameter )*

    // package-names ::= package-name ( ';' package-name )*

    // 3.5.4 Import-Package

    // Import-Package ::= import ( ',' import )*

    // import ::= package-names ( ';' parameter )*

    // package-names ::= package-name ( ';' package-name )*


    // All these variants are legal

    @Test
    public void testPackageHeader() {
        checkDecls(getParser().parsePackageHeader("com.foo", "Export-Package"), "names=[com.foo]");
        checkDecls(getParser().parsePackageHeader("com.foo.goo.boo,com.bib.bob.bub", "Export-Package"), "names=[com.foo.goo.boo]",
            "names=[com.bib.bob.bub]");
        checkDecls(getParser().parsePackageHeader("com.a;com.b", "Export-Package"), "names=[com.a com.b]");
        checkDecls(getParser().parsePackageHeader("com.a;com.bbbb,com.dd;com.eee", "Export-Package"), "names=[com.a com.bbbb]",
            "names=[com.dd com.eee]");
        checkDecls(getParser().parsePackageHeader("com.springsource.server.osgi;version=1.2.3", "Export-Package"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3]");
        checkDecls(getParser().parsePackageHeader("com.springsource.server.osgi;uses:=org.springframework.core", "Export-Package"),
            "names=[com.springsource.server.osgi] directives=[uses:=org.springframework.core]");
        checkDecls(getParser().parsePackageHeader("com.springsource.server.osgi;version=1.2.3;uses:=org.springframework.core", "Export-Package"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
        checkDecls(getParser().parsePackageHeader(
            "com.springsource.server.osgi;com.springsource.server.kernel;version=1.2.3;uses:=org.springframework.core", "Export-Package"),
            "names=[com.springsource.server.osgi com.springsource.server.kernel] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
    }

    /**
     * Try a few things with an extended character set - some valid, some not
     */
    @Test
    public void testPackageHeaderVarietyOfIdentifierChars() {
        checkPackageHeaderFailure("a:=,foo", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkDecls(getParser().parsePackageHeader("c" + aeDipthong + "m.foo", "Export-Package"), "names=[c" + aeDipthong + "m.foo]");
        checkPackageHeaderFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkPackageHeaderFailure("a;uses:=c" + aeDipthong + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkPackageHeaderFailure("x;uses:=wibble.c" + aeDipthong + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkPackageHeaderFailure("y;uses:=wibble.c" + aeDipthong + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkPackageHeaderFailure("a.b.c;f=5,a.d,a.e,", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkPackageHeaderFailure("a.b.c;f=5,a.d,a.e;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkDecls(getParser().parsePackageHeader("a.b.c.d;e.f.g.h;version=1.2.3;uses:=a.b.c", "test"),
            "names=[a.b.c.d e.f.g.h] attrs=[version=1.2.3] directives=[uses:=a.b.c]");
        checkDecls(getParser().parsePackageHeader("a.b.c;a.b.d;foo=bar", "test"), "names=[a.b.c a.b.d] attrs=[foo=bar]");
        checkDecls(getParser().parsePackageHeader("a.b.c;a.b.d", "test"), "names=[a.b.c a.b.d]");
        checkPackageHeaderFailure("a.b.c;a=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkPackageHeaderFailure("a.b.c;a:=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkPackageHeaderFailure("a.b.c;a:= ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkPackageHeaderFailure("  a.b.c ;  a := ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        //verify number-starting packages don't generate an ERROR - Equinox style.
        checkPackageHeader("co23.45o");
        checkPackageHeader("1");
        checkPackageHeaderFailure("a. b", HeaderProblemKind.ILLEGAL_SPACE);
        checkPackageHeaderFailure("a.   b", HeaderProblemKind.ILLEGAL_SPACE);
    }

    @Test
    public void testPackageHeaderNastyErrors() {
        checkPackageHeaderFailure(";", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure(".", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure(".;.;", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure(".;.;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkPackageHeaderFailure(".;.;", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure("..;", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure(",a=b;", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkPackageHeaderFailure("a:=b;d", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
    }

    @Test
    public void testPackageHeaderErrorMessagePositions() {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        parser.internalParsePackageHeader("a. b", "test");
        HeaderProblem hp = parser.getProblems().get(0);
        if (hp.getKind() == HeaderProblemKind.UNEXPECTED_SPACE_WARNING) {
            hp = parser.getProblems().get(1);
        }
        assertEquals(HeaderProblemKind.ILLEGAL_SPACE, hp.getKind());
        assertEquals(0, hp.getStartOffset());
        assertEquals(4, hp.getEndOffset());
        assertEquals("a. b", hp.getInserts()[0]);
        assertEquals(false, hp.isSeverity(Severity.WARNING));
        assertEquals(true, hp.isSeverity(Severity.ERROR));
        assertEquals("HP006E:Space not allowed here", hp.toString());
        assertNotNull(parser.getSourceContext());
        assertNotNull(parser.toString());
    }

    // TODO [later] who checks activation policy is only 'lazy' if set to anything at all?

    // Section 4.3.6.1

    // TODO Bug 463462 - As a developer I'd like to be able to build the Virgo artifacts with Gradle
    @Ignore("Deactivated with Bug 463462 - As a developer I'd like to be able to build the Virgo artifacts with Gradle")
    @Test
    public void testBundleActivationPolicy() {
        checkDecl(getParser().parseBundleActivationPolicy("lazy"), "names=[lazy]");
        checkDecl(getParser().parseBundleActivationPolicy("foobar"), "names=[foobar]");
        checkDecl(getParser().parseBundleActivationPolicy("lazy;a:=b"), "names=[lazy] directives=[a:=b]");
        checkDecl(getParser().parseBundleActivationPolicy("lazy;a:=b;b:=37"), "names=[lazy] directives=[a:=b b:=37]");

        checkBundleActivationPolicyFailure("lazy;include:=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkBundleActivationPolicyFailure("lazy;include:=a/b", HeaderProblemKind.UNEXPECTED_CHARACTER);
        checkBundleActivationPolicyFailure("lazy;a=b", HeaderProblemKind.ATTRIBUTES_NOT_ALLOWED_FOR_THIS_HEADER);
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        parser.internalParseBundleActivationPolicy("lazy;wiring=magic");

        // Let us do a bit more analysis on this

        HeaderProblem problem = parser.getProblems().get(0);
        assertEquals("wiring", problem.getInserts()[0]);
        assertEquals(5, problem.getStartOffset());
        assertEquals(11, problem.getEndOffset());
    }

    @Test
    public void testDynamicImportPackageWithWildcards() {
        checkDecls(getParser().parseDynamicImportPackageHeader("a.b.*"), "names=[a.b.*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("*"), "names=[*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("   *   "), "names=[*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("a.b.*;c.d.*"), "names=[a.b.* c.d.*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("a.b.*;a=andy,c.d.*"), "names=[a.b.*] attrs=[a=andy]", "names=[c.d.*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("*;c.d.*"), "names=[* c.d.*]");
        checkDecls(getParser().parseDynamicImportPackageHeader("*,c.d.*"), "names=[*]", "names=[c.d.*]");
    }

    @Test
    public void testDynamicImportPackage() {
        checkDecls(getParser().parseDynamicImportPackageHeader("com.foo"), "names=[com.foo]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.foo.goo.boo,com.bib.bob.bub"), "names=[com.foo.goo.boo]",
            "names=[com.bib.bob.bub]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.a;com.b"), "names=[com.a com.b]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.a;com.bbbb,com.dd;com.eee"), "names=[com.a com.bbbb]", "names=[com.dd com.eee]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.springsource.server.osgi;version=1.2.3"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.springsource.server.osgi;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi] directives=[uses:=org.springframework.core]");
        checkDecls(getParser().parseDynamicImportPackageHeader("com.springsource.server.osgi;version=1.2.3;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
        checkDecls(getParser().parseDynamicImportPackageHeader(
            "com.springsource.server.osgi;com.springsource.server.kernel;version=1.2.3;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi com.springsource.server.kernel] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
    }

    @Test
    public void testDynamicImportPackageErrors() {
        checkDynamicImportPackageFailure("a.b .*", HeaderProblemKind.ILLEGAL_SPACE);
        checkDynamicImportPackageFailure(".*", HeaderProblemKind.EXPECTED_IDENTIFIER);
        checkDynamicImportPackageFailure("a.b.*a.b", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkDynamicImportPackageFailure("a.c+b.*a.b", HeaderProblemKind.UNEXPECTED_CHARACTER);
        checkDynamicImportPackageFailure("a.b.*a.b;a=5.*", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkDynamicImportPackageFailure("", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkDynamicImportPackageFailure("a.b.c;f=5,a.d,a.e;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkDynamicImportPackageFailure("abc .goo", HeaderProblemKind.ILLEGAL_SPACE);
        checkDynamicImportPackageFailure("abc. goo", HeaderProblemKind.ILLEGAL_SPACE);
        checkDynamicImportPackageFailure("abc.", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkDynamicImportPackageFailure("a:=,foo", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkDynamicImportPackageFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.UNEXPECTED_SPACE_WARNING);
        checkDynamicImportPackageFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.EXPECTED_SEMICOLON_OR_COMMA);
        checkDynamicImportPackageFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5",
            HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkDynamicImportPackageFailure("com.goo.*;a:=1;b=3,com.*.wibble;f;a:= foo;com.foo.bar.;a==5", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
    }

    private void checkGeneralHeader(String headerText, String... expectedHeaders) {
        checkDecls(getParser().parseHeader(headerText), expectedHeaders);
    }

    @Test
    public void testParseGeneralHeader() {
        checkDecls(getParser().parseHeader("com.foo"), "names=[com.foo]");
        checkDecls(getParser().parseHeader("com.foo.goo.boo,com.bib.bob.bub"), "names=[com.foo.goo.boo]", "names=[com.bib.bob.bub]");
        checkDecls(getParser().parseHeader("com.a;com.b"), "names=[com.a com.b]");
        checkDecls(getParser().parseHeader("com.a;com.bbbb,com.dd;com.eee"), "names=[com.a com.bbbb]", "names=[com.dd com.eee]");
        checkDecls(getParser().parseHeader("com.springsource.server.osgi;version=1.2.3"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3]");
        checkDecls(getParser().parseHeader("com.springsource.server.osgi;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi] directives=[uses:=org.springframework.core]");
        checkDecls(getParser().parseHeader("com.springsource.server.osgi;version=1.2.3;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
        checkDecls(
            getParser().parseHeader("com.springsource.server.osgi;com.springsource.server.kernel;version=1.2.3;uses:=org.springframework.core"),
            "names=[com.springsource.server.osgi com.springsource.server.kernel] attrs=[version=1.2.3] directives=[uses:=org.springframework.core]");
    }

    /**
     * Go mad on testing import bundle (which effectively tests all header variants that want to consume
     * bundleDeclarations).
     */
    @Test
    public void testImportBundle() {
        checkDecls(checkImportBundle("a.b.c;f=5"), "names=[a.b.c] attrs=[f=5]");
        checkDecls(checkImportBundle("a.b.c,f"), "names=[a.b.c]", "names=[f]");
        checkDecls(checkImportBundle("a.b.c;uses:=a.b"), "names=[a.b.c] directives=[uses:=a.b]");
        checkDecls(checkImportBundle("a.b.c;f=5,a.d,a.e"), "names=[a.b.c] attrs=[f=5]", "names=[a.d]", "names=[a.e]");
        checkDecls(checkImportBundle("a.b.c;f=5,a.d,a.e"), "names=[a.b.c] attrs=[f=5]", "names=[a.d]", "names=[a.e]");
        checkDecls(checkImportBundle("com.123"), "names=[com.123]");
        checkDecls(checkImportBundle("com"), "names=[com]");
        checkDecls(checkImportBundle("1"), "names=[1]");
        checkDecls(checkImportBundle("a.b.c;f:=\"c" + aeDipthong + "m\",a.d,a.e"), "names=[a.b.c] directives=[f:=c" + aeDipthong + "m]",
            "names=[a.d]", "names=[a.e]");

        checkImportBundleFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkImportBundleFailure("1;uses:=c" + aeDipthong + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkImportBundleFailure("1;uses:=wibble.c" + aeDipthong + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkImportBundleFailure("1;uses:=wibble.c" + aeDipthong + "m.tribble", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
        checkImportBundleFailure("a.b.c;f=5,a.d,a.e,", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkImportBundleFailure("a.b.c;f=5,a.d,a.e;", HeaderProblemKind.UNEXPECTEDLY_OOD);
        checkImportBundleFailure("a.b.c.d;e.f.g.h;version=1.2.3;uses:=a.b.c", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkImportBundleFailure("a.b.c;a.b.d;foo=bar", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkImportBundleFailure("a.b.c;a.b.d", HeaderProblemKind.EXPECTED_ATTRIBUTE_OR_DIRECTIVE);
        checkImportBundleFailure("a.b.c;a=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkImportBundleFailure("a.b.c;a:=", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkImportBundleFailure("a.b.c;a:= ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkImportBundleFailure("  a.b.c ;  a := ", HeaderProblemKind.UNEXPECTEDLY_OOD_AT_ARGUMENT_VALUE);
        checkImportBundleFailure("c" + aeDipthong + "m.wibble", HeaderProblemKind.EXPECTED_TOKEN);

    }

    @Test
    public void testWebFilterMappings() {
        checkDecls(checkWebFilterMappings("a.b.c;f=5"), "names=[a.b.c] attrs=[f=5]");
        checkDecls(checkWebFilterMappings("a.b.c,f"), "names=[a.b.c]", "names=[f]");

        checkWebFilterMappingsFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkWebFilterMappingsFailure("1;uses:=c" + aeDipthong + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkWebFilterMappingsFailure("1;uses:=wibble.c" + aeDipthong + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
    }

    @Test
    public void testImportLibrary() {
        checkDecls(checkImportLibrary("a.b.c;f=5"), "names=[a.b.c] attrs=[f=5]");
        checkDecls(checkWebFilterMappings("a.b.c,f"), "names=[a.b.c]", "names=[f]");
        checkImportLibraryFailure("com .foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkImportLibraryFailure("com. foo", HeaderProblemKind.ILLEGAL_SPACE);
        checkImportLibraryFailure("1;uses:=c" + aeDipthong + "m.wibble", HeaderProblemKind.INVALID_ARGUMENT_VALUE);
        checkImportLibraryFailure("1;uses:=wibble.c" + aeDipthong + "m", HeaderProblemKind.EXTRANEOUS_DATA_AFTER_PARAMETER);
    }

    @Test
    public void testQuotedParameters() {
        String test = "com.springsource.server.osgi;version=\"1.2.3\";uses:=\"org.springframework.core\"";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("1.2.3", decl.getAttributes().get("version"));
        assertEquals("org.springframework.core", decl.getDirectives().get("uses"));
    }

    @Test
    public void testPackageHeader_MultiDeclarationMultiPackageWithAttributesAndDirectives() {
        String test = "com.springsource.server.osgi;version=1.2.3;uses:=org.springframework.core,com.springsource.server.osgi.framework;com.springsource.server.osgi.boggle;version=1.3.2";
        List<HeaderDeclaration> packageDeclarations = parseTestHeader(test);
        assertNotNull(packageDeclarations);
        assertEquals(2, packageDeclarations.size());

        HeaderDeclaration decl = packageDeclarations.get(0);
        assertEquals("com.springsource.server.osgi", decl.getNames().get(0));
        assertEquals("1.2.3", decl.getAttributes().get("version"));
        assertEquals("org.springframework.core", decl.getDirectives().get("uses"));

        decl = packageDeclarations.get(1);
        assertEquals("com.springsource.server.osgi.framework", decl.getNames().get(0));
        assertEquals("com.springsource.server.osgi.boggle", decl.getNames().get(1));
        assertEquals("1.3.2", decl.getAttributes().get("version"));
    }

    /**
     * Hyphens need a little bit of special treatment in order for tokens containing them to be recognized as path
     * elements. See HeaderTokenKind.canBeTreatedAsPathElement() for the fix that allows this test to pass. <br>
     * In general hyphens are not quite handled correctly throughout by the parser right now. They are currently
     * allowed in package headers although strictly by the spec they shouldn't be allowed. I believe they are being let
     * through because some users want to Export-Package META-INF. With strict checking according to the spec this
     * wouldn't be allowed. It appears to be one of those things where the spec implementors have decided to allow it
     * but the spec doesn't allow it. <br>
     * If more problems surface here then it may be time to have a new token kind for a pathelement, rather than trying
     * to reuse TOKEN for this.
     */
    @Test
    public void testParseHyphens() {
        List<HeaderDeclaration> decls = getParser().parseHeader("Alpha-*,*-Bravo,Charlie-*-Delta,*-Echo-*");
        checkDecls(decls, "names=[Alpha-*]", "names=[*-Bravo]", "names=[Charlie-*-Delta]", "names=[*-Echo-*]");
    }

    // -- PackageHeader: as used for Export-Package


    private List<HeaderDeclaration> parseTestHeader(String text) {
        return new StandardHeaderParser(tlogger).parsePackageHeader(text, "test");
    }

    // Performance tests


    // -- end of test methods


    // private void parse(String headerText) throws Exception {

    // // long stime = System.currentTimeMillis();

    // try {

    // headerParser = new StandardHeaderParser(tl);

    // headerParser.parseBundleSymbolicName(headerText);

    // } catch (Exception e) {

    // throw e;

    // }

    // // long etime = System.currentTimeMillis();

    // // System.out.println("Parsed in " + (etime - stime) + "ms");

    // // printProblems();

    // // printManifest();

    // }


    private void checkImportBundleFailure(String header, HeaderProblemKind expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseImportBundleHeader(header);
            fail();
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (!msg.contains(expected.getCode())) {
                System.out.println(msg);
                fail("Did not find problem " + expected.getCode() + " in exception text:\n" + msg);
            }
        }
    }

    private void checkWebFilterMappingsFailure(String header, HeaderProblemKind expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseWebFilterMappingsHeader(header);
            fail();
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (!msg.contains(expected.getCode())) {
                System.out.println(msg);
                fail("Did not find problem " + expected.getCode() + " in exception text:\n" + msg);
            }
        }
    }

    private void checkImportLibraryFailure(String header, HeaderProblemKind expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseImportLibraryHeader(header);
            fail();
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (!msg.contains(expected.getCode())) {
                System.out.println(msg);
                fail("Did not find problem " + expected.getCode() + " in exception text:\n" + msg);
            }
        }
    }

    private void checkRequireBundleFailure(String header, HeaderProblemKind expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        // org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser parser = new AntlrHeaderParser(tl);

        try {
            parser.parseRequireBundleHeader(header);
            // List<org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration> decls =
            // parser.parseRequireBundleHeader(header);

            // List<HeaderDeclaration> decls2 = new ArrayList<HeaderDeclaration>();

            // for (Iterator iterator = decls.iterator(); iterator.hasNext();) {

            // org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration hd =

            // (org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration) iterator

            // .next();

            // decls2.add(new ImmutableHeaderDeclaration(hd.getNames(), hd.getAttributes(), hd.getDirectives()));

            // }

            // StandardHeaderParser parser = new StandardHeaderParser(tl);

            // try {

            // parser.parseRequireBundleHeader(header);

            fail();
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (!msg.contains(expected.getCode())) {
                System.out.println(msg);
                fail("Did not find problem " + expected.getCode() + " in exception text:\n" + msg);
            }
        }
    }

    private void checkBundleActivationPolicyFailure(String header, HeaderProblemKind expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseBundleActivationPolicy(header);
            fail();
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (!msg.contains(expected.getCode())) {
                System.out.println(msg);
                fail("Did not find problem " + expected.getCode() + " in exception text:\n" + msg);
            }
        }
    }

    private StandardHeaderParser checkFragmentHostHeaderFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseFragmentHostHeader(header);
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
        return parser;
    }

    private void checkBundleSymbolicNameFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseBundleSymbolicName(header);
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
    }

    private void checkLibrarySymbolicNameFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseLibrarySymbolicName(header);
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
    }

    private void checkDynamicImportPackageFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseDynamicImportPackageHeader(header);
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
    }

    private void checkGeneralHeaderFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parseHeader(header);
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
    }

    private void checkPackageHeaderFailure(String header, HeaderProblemKind... expected) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parsePackageHeader(header, "test");
            fail("Parsing should have failed for " + header);
        } catch (BundleManifestParseException bmpe) {
            String msg = bmpe.getMessage();
            if (expected == null || expected.length == 0) {
                bmpe.printStackTrace();
            } else {
                if (!msg.contains(expected[0].getCode())) {
                    System.out.println(msg);
                    fail("Did not find problem " + expected[0].getCode() + " in exception text:\n" + msg);
                }
            }
        }
    }
    
    private void checkPackageHeader(String header) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            parser.parsePackageHeader(header, "test");
            assertFalse(parser.foundProblems(Severity.ERROR));
        } catch (BundleManifestParseException bmpe) {
            System.out.println(bmpe.getMessage());
            fail("Unexpected exception: " + bmpe.getMessage());
        }
    }

    private List<HeaderDeclaration> checkImportBundle(String header) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            return parser.parseImportBundleHeader(header);
        } catch (BundleManifestParseException bmpe) {
            System.out.println(bmpe.getMessage());
            fail("Unexpected exception: " + bmpe.getMessage());
        }
        return null;
    }

    private List<HeaderDeclaration> checkWebFilterMappings(String header) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            return parser.parseWebFilterMappingsHeader(header);
        } catch (BundleManifestParseException bmpe) {
            System.out.println(bmpe.getMessage());
            fail("Unexpected exception: " + bmpe.getMessage());
        }
        return null;
    }

    private List<HeaderDeclaration> checkImportLibrary(String header) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        try {
            List<HeaderDeclaration> decls = parser.parseImportLibraryHeader(header);
            assertFalse(parser.foundProblems());
            assertFalse(parser.foundProblems(Severity.INFO));
            assertFalse(parser.foundProblems(Severity.WARNING));
            assertFalse(parser.foundProblems(Severity.ERROR));
            return decls;
        } catch (BundleManifestParseException bmpe) {
            System.out.println(bmpe.getMessage());
            fail("Unexpected exception: " + bmpe.getMessage());
        }
        return null;
    }

    private List<HeaderDeclaration> checkRequireBundle(String header) {
        StandardHeaderParser parser = new StandardHeaderParser(tlogger);
        // org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser parser = new AntlrHeaderParser(tl);

        try {
            return parser.parseRequireBundleHeader(header);
            // List<org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration> decls =
            // parser.parseRequireBundleHeader(header);

            // List<HeaderDeclaration> decls2 = new ArrayList<HeaderDeclaration>();

            // for (Iterator iterator = decls.iterator(); iterator.hasNext();) {

            // org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration hd =

            // (org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration) iterator

            // .next();

            // decls2.add(new ImmutableHeaderDeclaration(hd.getNames(), hd.getAttributes(), hd.getDirectives()));

            // }

            // return decls2;

        } catch (BundleManifestParseException bmpe) {
            System.out.println(bmpe.getMessage());
            fail("Unexpected exception: " + bmpe.getMessage());
        }
        return null;
    }

    private void checkDecls(List<HeaderDeclaration> decls, String... expectedHeaders) {
        assertEquals(expectedHeaders.length, decls.size());
        int i = 0;
        for (String expectedHeader : expectedHeaders) {
            checkDecl(decls.get(i++), expectedHeader);
        }
    }

    private void checkDecl(HeaderDeclaration parsedHeaderDecl, String expectedHeaderDecl) {
        assertEquals(expectedHeaderDecl, formatHeaderDeclaration(parsedHeaderDecl));
    }

    /**
     * @param hd HeaderDeclaration to be formatted
     * @return fixed formatted string containing the header information - attributes are sorted to ensure reliable
     *         string formats.
     */
    private String formatHeaderDeclaration(HeaderDeclaration hd) {
        StringBuilder sb = new StringBuilder();
        sb.append("names=[");
        List<String> names = hd.getNames();
        int i = 0;
        for (String name : names) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(name);
            i++;
        }
        sb.append("]");

        Map<String, String> attrs = hd.getAttributes();
        if (attrs != null && attrs.size() != 0) {
            sb.append(" attrs=[");
            i = 0;
            Set<String> keys = attrs.keySet();
            // Alphabetical order

            List<String> keysList = new ArrayList<String>();
            keysList.addAll(keys);
            Collections.sort(keysList);
            for (String key : keysList) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(key).append("=").append(attrs.get(key));
                i++;
            }
            sb.append("]");
        }
        Map<String, String> dirs = hd.getDirectives();
        if (dirs != null && dirs.size() != 0) {
            sb.append(" directives=[");
            i = 0;
            Set<String> keys = dirs.keySet();
            // Alphabetical order

            List<String> keysList = new ArrayList<String>();
            keysList.addAll(keys);
            Collections.sort(keysList);
            for (String key : keysList) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(key).append(":=").append(dirs.get(key));
                i++;
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private HeaderParser getParser() {
        return new StandardHeaderParser(tlogger);
    }

    private void checkVisited(ByteArrayOutputStream baos, String expected) {
        String visitorOutput = new String(baos.toByteArray());
        StringTokenizer st = new StringTokenizer(expected);
        int pos = -1;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int pos2 = visitorOutput.indexOf(tok);
            if (pos2 == -1) {
                fail("Could not find '" + tok + "' in data '" + visitorOutput + "'");
            }
            if (pos2 < pos) {
                // doesn't appear to be in the right order...

                System.out.println("VisitorOutput:\n" + visitorOutput + "\nExpected:" + expected);
                fail("Visitor report incorrect");
            }
            pos = pos2;
        }
    }
}
