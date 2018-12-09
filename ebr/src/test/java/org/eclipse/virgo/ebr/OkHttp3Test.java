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
    private static final String OKIO_VERSION_KEY = "okioVersion";

    private static final String OKHTTP3 = "com.squareup.okhttp3";
    private static final String OKHTTP3_VERSION_KEY = "okhttp3Version";

    @Configuration
    @Override
    public Option[] config() {
        return options(
                // maven local
                mavenBundle(MIRROR_GROUP, OKIO, resolveVersionFromGradleProperties(OKIO_VERSION_KEY)),
                mavenBundle(MIRROR_GROUP, OKHTTP3, resolveVersionFromGradleProperties(OKHTTP3_VERSION_KEY)),
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
