package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test class testing OkHttp3 bundle resolution.
 * <p>
 * Created by dam on 6/14/17.
 */
public class OkHttp3Test extends AbstractBaseTest {

    private static final String OKIO = "com.squareup.okio";
    private static final String OKIO_VERSION = "1.13.0";

    private static final String OKHTTP3 = "com.squareup.okhttp3";
    private static final String OKHTTP3_VERSION = "3.8.0";

    @Configuration
    @Override
    public Option[] config() {
        return options(
                // maven local
                mavenBundle(MIRROR_GROUP, OKIO, OKIO_VERSION),
                mavenBundle(MIRROR_GROUP, OKHTTP3, OKHTTP3_VERSION),
                // specify junit bundles
                junitBundles()
        );
    }

    @Test
    public void testOkio() throws Exception {
        assertBundleActive(OKIO);
    }

    @Test
    public void testOkHttp3() throws Exception {
        assertBundleActive(OKHTTP3);
    }
}
