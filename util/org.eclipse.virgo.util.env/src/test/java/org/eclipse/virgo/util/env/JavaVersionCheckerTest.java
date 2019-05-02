package org.eclipse.virgo.util.env;

import org.junit.Test;

import static org.junit.Assert.*;

public class JavaVersionCheckerTest {

    @Test
    public void checkOldJavaVersions() {
        // https://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
        assertFalse(JavaVersionChecker.isJavaEightOrLater("1.3.0-b24"));
        // https://docs.oracle.com/javase/1.5.0/docs/relnotes/version-5.0.html
        assertFalse(JavaVersionChecker.isJavaEightOrLater("1.5.0-b64"));
        // https://www.oracle.com/technetwork/java/javase/version-6-141920.html
        assertFalse(JavaVersionChecker.isJavaEightOrLater("1.6.0"));
        // https://www.oracle.com/technetwork/java/javase/jdk7-naming-418744.html
        assertFalse(JavaVersionChecker.isJavaEightOrLater("1.7.0"));
    }

    @Test
    public void checkJavaEight() {
        // https://www.oracle.com/technetwork/java/javase/jdk8-naming-2157130.html
        assertTrue(JavaVersionChecker.isJavaEightOrLater("1.8.0"));
    }

    @Test
    public void checkJavaNine() {
        // https://openjdk.java.net/jeps/223 - JDK 9
        assertTrue(JavaVersionChecker.isJavaEightOrLater("9-ea"));
        assertTrue(JavaVersionChecker.isJavaEightOrLater("9.0.1"));
        assertTrue(JavaVersionChecker.isJavaEightOrLater("9.0.4"));
    }

    @Test
    public void checkJavaElevenAndBeyond() {
        // https://openjdk.java.net/jeps/322 - JDK 10+
        assertTrue(JavaVersionChecker.isJavaEightOrLater("10.0.2"));
        assertTrue(JavaVersionChecker.isJavaEightOrLater("11.0.3"));
        assertTrue(JavaVersionChecker.isJavaEightOrLater("12.0.1"));
        assertTrue(JavaVersionChecker.isJavaEightOrLater("18.1.4"));
    }
}
