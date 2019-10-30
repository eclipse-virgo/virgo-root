package org.eclipse.virgo.ebr;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

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
                // 3rd-party local
                okio(),
                okhttp3(),
                // specify junit bundles
                junitBundles()
        );
    }

    public static UrlProvisionOption okio() {
        return getUrlProvisionOption("oevm.com.squareup.okio", OKIO_VERSION_KEY);
    }

    public static UrlProvisionOption okhttp3() {
        return getUrlProvisionOption("oevm.com.squareup.okhttp3", OKHTTP3_VERSION_KEY);
    }

    @Test
    public void testOkio() {
        assertMirroredBundleActive(OKIO);
    }

    @Test
    public void testOkHttp3() {
        assertMirroredBundleActive(OKHTTP3);
    }

}
