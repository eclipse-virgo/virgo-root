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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.eclipse.virgo.util.parser.manifest.internal.RecoveringManifestLexer;
import org.eclipse.virgo.util.parser.manifest.internal.TestVisitor;

public class RecoveringManifestParserTests extends TestCase {

    private static final String ABC = "abcdefghijklmnopqrstuvwxyz";

    TestVisitor v;

    RecoveringManifestParser mp;

    /**
     * 
     * Example of how to basically create a parser, parse a file then check for the problems.
     * 
     * @throws Exception sometimes
     */

    public void testBasicParserUsage() throws Exception {

        ManifestParser mParser = new RecoveringManifestParser();

        try (InputStream broken = new FileInputStream("build/resources/test/broken001.mf")) {
            ManifestContents contents = mParser.parse(new InputStreamReader(broken, UTF_8));

            // has errors but recoverable

            assertTrue(mParser.foundProblems());

            Map<String, String> mainAttrs = contents.getMainAttributes();

            assertEquals("toys", mainAttrs.get("Bundle-Name"));

            assertEquals("1.0", contents.getVersion());

            Map<String, String> secondaryAttrs = contents.getAttributesForSection("secondSection");

            assertEquals("secondSection", secondaryAttrs.get("Name"));

            List<String> sectionNames = contents.getSectionNames();

            assertEquals("secondSection", sectionNames.get(0));
        }
    }

    public void testNameTooLong() {

        StringBuffer sb = new StringBuffer();

        sb.append("Manifest-Version: 1.0\n");

        while (sb.length() < (RecoveringManifestLexer.MAX_TOKEN_LENGTH + 10000)) {

            sb.append(ABC);

        }

        sb.append("abc: value\n");

        ManifestParser mParser = new RecoveringManifestParser();

        ManifestContents contents = mParser.parse(sb.toString());

        assertNotNull(contents);

        assertTrue(mParser.foundProblems());

        assertEquals(ManifestProblemKind.NAME_TOO_LONG, mParser.getProblems().get(0).getKind());

    }

    public void testValueTooLong() throws Exception {

        StringBuffer sb = new StringBuffer();

        sb.append("Manifest-Version: 1.0\nName:\n");

        while (sb.length() < (RecoveringManifestLexer.MAX_TOKEN_LENGTH + 10000)) {

            sb.append(" " + ABC).append("\n");

        }

        ManifestParser mParser = new RecoveringManifestParser();

        ManifestContents contents = mParser.parse(sb.toString());

        assertNotNull(contents);

        assertTrue(mParser.foundProblems());

        assertEquals(ManifestProblemKind.VALUE_TOO_LONG, mParser.getProblems().get(0).getKind());

    }

    // Checking the isXXX methods in the lexer now by passing chars with a value

    // > 256 (this means the lookup tables won't be used)

    public void testFunkyChars() {

        ManifestParser mParser = new RecoveringManifestParser();

        mParser.parse("Manifest-Version: 1.0\nName: " + (char) 257);

        mParser = new RecoveringManifestParser();

        mParser.parse("Manifest-Version: 1.0\n" + (char) 257 + "ame: value");

        mParser = new RecoveringManifestParser();

        mParser.parse("Manifest-Version: 1.0\nN" + (char) 257 + "ame: value");

    }

    public void testSkippingSections() {

        TestVisitor tv = new TestVisitor();

        tv.setTerminateAfterMainSection(true);

        ManifestParser mParser = new RecoveringManifestParser(tv);

        ManifestContents contents = mParser.parse("Manifest-Version: 1.0\na: b\n\nName: Second\nc: d\n");

        assertFalse(mParser.foundProblems());

        // Attributes are "Manifest-Version" and "a"

        assertEquals(2, contents.getMainAttributes().size());

        assertTrue(contents.getSectionNames().isEmpty());

    }

    public void testSkippingSections2() {

        ManifestParser mParser = new RecoveringManifestParser();

        mParser.setTerminateAfterMainSection(true);

        ManifestContents contents = mParser.parse("Manifest-Version: 1.0\na: b\n\nName: Second\nc: d\n\nName: Third\ne: f\n");

        assertFalse(mParser.foundProblems());

        // Attributes are "Manifest-Version" and "a"

        assertEquals(2, contents.getMainAttributes().size());

        // Nothing there:

        assertEquals(0, contents.getSectionNames().size());

    }

    public void testTheMostBasic() throws Exception {

        parse("Manifest-Version: 1.0");

        assertEquals("1.0", v.getVersion());

        parse("Manifest-Version:\n 1.0");

        assertEquals("1.0", v.getVersion());

    }

    // public void testRogueNewlines() throws Exception {

    // parse("\n\nManifest-Version: 1.0");

    // assertEquals("1.0", v.getVersion());

    // assertTrue(mp.foundProblems());

    // assertEquals(1, mp.getProblems().size());

    // }

    public void testErrorScenarios() throws Exception {

        parse("Manifest-Version\n 1.0");

        assertEquals("1.0", v.getVersion());

        // MP002:[line 1, col 0]: Name header ended prematurely when a newline

        // was encountered. Expected form is Name: Value

        assertTrue(mp.foundProblems());

        assertEquals(1, mp.getProblems().size());

    }

    public void testErrorScenarios2() throws Exception {

        // Here the manifest-version is not found, because there is no space on

        // the second line ahead of 1.0 - so we assume it is a

        // new header

        parse("Manifest-Version\n1.0");

        // assertEquals("1.0", v.getVersion());

        // Printing problems: #5

        // Manifest-Version

        // ^ ^

        // MP002:[line 1, col 0]: Name header ended prematurely when a newline

        // was encountered. Expected form is Name: Value

        // Manifest-Version

        // ^ ^

        // MP004:[line 1, col 0]: The value appears to be missing for the header

        // name 'Manifest-Version'

        // 1.0

        // ^

        // MP005:[line 2, col 1]: The name of a header cannot contain the

        // character '.'

        // 1.0

        // ^ ^

        // MP002:[line 2, col 0]: Name header ended prematurely when a newline

        // was encountered. Expected form is Name: Value

        // 1.0

        // ^ ^

        // MP004:[line 2, col 0]: The value appears to be missing for the header

        // name '1.0'

        assertTrue(mp.foundProblems());

        assertEquals(5, mp.getProblems().size());

    }

    public void testErrorScenarios3() throws Exception {

        // Now we work out that they have missed the colon and the value is on

        // the second line

        parse("Manifest-Version\n 1.0");

        // Manifest-Version

        // ^ ^

        // MP002:[line 1, col 0]: Name header ended prematurely when a newline

        // was encountered. Expected form is Name: Value

        assertEquals("1.0", v.getVersion());

        assertTrue(mp.foundProblems());

        assertEquals(1, mp.getProblems().size());

    }

    public void testSimpleSetOfNameValueHeaders() throws Exception {

        parse("Manifest-Version: 1.0\na: b\nc: d");

        v.assertHeaderCount(3);

    }

    public void testTwoSectionManifest() throws Exception {

        parse("Manifest-Version: 1.0\na: b\nc: d\n\nfoo: bar\nWibble: wobble");

        v.assertSecondarySectionsCount(1);

    }

    private void readFromJar(String manifestName) {

        try (ZipFile manifestTestDataZip = new ZipFile("build/resources/test/manifests.zip")) {

            ZipEntry manifestZipEntry = manifestTestDataZip.getEntry(manifestName);

            InputStreamReader inputStreamReader = new InputStreamReader(manifestTestDataZip.getInputStream(manifestZipEntry), UTF_8);
            parse(inputStreamReader);

        } catch (IOException e) {

            e.printStackTrace();
            fail("Unexpected IOException " + e.getMessage());

        }
    }

    // --- tests involving testdata on the disk

    public void testBasicOsgiManifest() throws Exception {

        readFromJar("manifest0000.mf");

        assertFalse(mp.foundProblems());
        assertContainsHeaderNameValuePair("Bundle-Localization", "plugin");

    }

    // public void testBasicOsgiManifest0011() throws Exception {
    // parse(readFromJar("manifest0011.mf"));
    // assertTrue(mp.foundProblems());
    // assertProblem(ManifestProblemKind.UNEXPECTED_NAME);
    // }

    // public void testBasicOsgiManifest0210() throws Exception {

    // parse(readFromJar("manifest0210.mf"));

    // assertTrue(mp.foundProblems());

    // assertProblem(ManifestProblemKind.UNEXPECTED_BLANK_LINES_AT_START_OF_MANIFEST);

    // // assertProblem(ManifestProblemKind.UNEXPECTED_TOKEN_KIND);

    // // assertProblem(ManifestProblemKind.UNEXPECTED_EOM);

    // }

    public void testStreamParsing() throws IOException {

        Reader iStream = new StringReader(

        "Manifest-Version: 1.0\r\nGrobble: gribble\r\n\r\n\r\nName: wobble\r\nFlibble:\r\n Fl\r\n ob\r\n le");

        mp = new RecoveringManifestParser(v = new TestVisitor());

        mp.parse(iStream);

        // printProblems();

        assertFalse(mp.foundProblems());

    }

    public void testBrokenManifests001() throws Exception {

        parse(new File("build/resources/test/broken001.mf"));

        assertTrue(mp.foundProblems());

        // printProblems();

        assertProblem(ManifestProblemKind.NAME_ENDED_WITH_SPACE_RATHER_THAN_COLON);

    }

    public void testMessageFormatting() {

        String fmtd = ManifestProblemKind.NAME_MUST_START_WITH_ALPHANUMERIC.format(0, 0, "test");

        assertNotNull(fmtd);

    }

    public void testBrokenManifests002() {

        parse(getTestFile("broken002.mf"));

        assertTrue(mp.foundProblems());

        // printProblems();

        // printHeaders();

        assertProblem(ManifestProblemKind.NAME_ENDED_WITH_SPACE_RATHER_THAN_COLON);

        assertProblem(ManifestProblemKind.VALUE_MUST_START_WITH_SPACE);

        assertProblem(ManifestProblemKind.VALUE_MUST_IMMEDIATELY_FOLLOW_NAME);

        assertProblem(ManifestProblemKind.ILLEGAL_NAME_CHAR);

    }

    public void testBrokenManifests003() {

        parse(getTestFile("broken003.mf"));

        assertTrue(mp.foundProblems());

        // Name = secondSection

        // intendedValueContinuationForLastLine =

        // MissingColon = abcde

        // OrdinaryName = OrdinaryValue

        assertTrue(mp.getManifestContents().getAttributesForSection("secondSection").size() == 4);

        assertProblem(ManifestProblemKind.NAME_ENDED_PREMATURELY_WITH_NEWLINE);

        assertProblem(ManifestProblemKind.MISSING_VALUE);

    }

    // Funky package name char

    public void testBasicOsgiManifest004() {

        parse(getTestFile("broken004.mf"));

        String myway = v.getMainAttributes().get("Export-Package");

        assertEquals(".,p!yuck, a^f", myway);

        assertFalse(mp.foundProblems());

    }

    public void testAllManifestTestData() throws Exception {

        // few warmups before the monitored run

        runPerformanceTest(false);

        runPerformanceTest(false);

        runPerformanceTest(false);

        runPerformanceTest(true);

    }

    public void runPerformanceTest(boolean measure) throws Exception {

        ZipFile manifestTestDataZip = new ZipFile("build/resources/test/manifests.zip");

        try {

            Enumeration<? extends ZipEntry> manifestsFromZip = manifestTestDataZip.entries();

            int c = 0;

            long stime = System.currentTimeMillis();

            while (manifestsFromZip.hasMoreElements()) {

                ZipEntry manifestEntry = manifestsFromZip.nextElement();

                String manifestName = manifestEntry.getName();

                if (!manifestName.endsWith(".mf") || !manifestName.startsWith("manifest")) {

                    continue;

                }

                InputStream fis = manifestTestDataZip.getInputStream(manifestEntry);

                try {

                    c++;

                    Manifest mf = new Manifest(fis);

                    mf.getEntries();

                } catch (IOException e) {

                    e.printStackTrace();

                }

                fis.close();

            }

            long etime = System.currentTimeMillis();

            if (measure) {

                System.out.println("JDK processed " + c + " manifests in " + (etime - stime) + "ms");

            }

            boolean pauseToAttachProfiler = false;

            if (measure && pauseToAttachProfiler) {

                System.in.read();

            }

            manifestsFromZip = manifestTestDataZip.entries();

            stime = System.currentTimeMillis();

            c = 0;

            while (manifestsFromZip.hasMoreElements()) {

                ZipEntry manifestEntry = manifestsFromZip.nextElement();

                String manifestName = manifestEntry.getName();

                if (!manifestName.endsWith(".mf") || !manifestName.startsWith("manifest")) {

                    continue;

                }

                c++;

                Reader r = new InputStreamReader(manifestTestDataZip.getInputStream(manifestEntry));

                try {

                    parse(r);

                } catch (Exception e) {

                    e.printStackTrace();

                    fail("Failed on manifest " + manifestName);

                }

                r.close();

            }

            etime = System.currentTimeMillis();

            if (measure) {

                System.out.println("RecoverableParser processed " + c + " manifests in " + (etime - stime) + "ms");

            }

            if (measure && pauseToAttachProfiler) {

                try {

                    Thread.sleep(10000);

                } catch (Exception e) {

                }

            }

        } finally {

            manifestTestDataZip.close();

        }

    }

    // ---

    private void assertContainsHeaderNameValuePair(String name, String value) {

        Map<String, String> headers = v.getAllHeaders();

        boolean found = false;

        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {

            String headerName = headerEntry.getKey();

            String headerValue = headerEntry.getValue();

            if (headerName.equals(name) && headerValue.equals(headerValue)) {

                found = true;

                break;

            }

        }

        if (!found) {

            fail("Failed to find header name value pair: " + name + ": " + value);

        }

    }

    private void parse(Reader r) {

        try {

            StringBuilder fileData = new StringBuilder(512);

            char[] buf = new char[4096];

            int numRead = 0;

            while ((numRead = r.read(buf)) != -1) {

                fileData.append(new String(buf, 0, numRead));

            }

            r.close();

            parse(fileData.toString());

        } catch (Exception e) {

            throw new RuntimeException("Problem during parsing", e);

        }

    }

    private void parse(File f) {

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {

            StringBuilder fileData = new StringBuilder(512);

            byte[] buf = new byte[4096];

            int numRead = 0;

            while ((numRead = bis.read(buf)) != -1) {

                fileData.append(new String(buf, 0, numRead));

            }

            parse(fileData.toString());

        } catch (Exception e) {
            throw new RuntimeException("Problem during parsing", e);
        }

    }

    private void parse(String manifest) throws Exception {

        // long stime = System.currentTimeMillis();

        try {

            mp = new RecoveringManifestParser(v = new TestVisitor());

            mp.parse(manifest);

        } catch (Exception e) {

            printProblems();

            throw e;

        }

        // long etime = System.currentTimeMillis();

        // System.out.println("Parsed in " + (etime - stime) + "ms");

        // printProblems();

        // printManifest();

    }

    private void printProblems() {

        List<ManifestProblem> problems = mp.getProblems();

        System.out.println("Printing problems:  #" + problems.size());

        for (ManifestProblem manifestProblem : problems) {

            System.out.println(manifestProblem.toStringWithContext());

        }

    }

    private File getTestFile(String name) {

        return new File("build/resources/test/" + name);

    }

    private ManifestProblem assertProblem(ManifestProblemKind expectedProblem) {

        List<ManifestProblem> problems = mp.getProblems();

        for (ManifestProblem manifestProblem : problems) {

            if (manifestProblem.getKind() == expectedProblem) {

                return manifestProblem;

            }

        }

        printProblems();

        fail("Did not find the expected problem " + expectedProblem);

        return null;

    }

    // private void printManifest() {

    // Map<String, String> allHeaders = v.getAllHeaders();

    // for (Map.Entry<String, String> header : allHeaders.entrySet()) {

    // System.out.println(header.getKey() + ": " + header.getValue());

    // }

    // }

    // private void printHeaders() {

    // Map<String, String> attrs = v.getAttributesForSection("secondSection");

    // for (Map.Entry<String, String> attr : attrs.entrySet()) {

    // System.out.println(attr.getKey() + " = " + attr.getValue());

    // }

    // }

}
