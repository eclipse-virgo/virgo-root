
package org.eclipse.virgo.kernel.install.artifact.internal.scoping;

import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateBundleSymbolicNameException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.DuplicateExportException;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.Scoper.UnsupportedBundleManifestVersionException;
import org.eclipse.virgo.util.osgi.manifest.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScoperTests {

    private static final String SCOPE_NAME = "test_scope";

    private static BundleManifest manifest = null;

    private static BundleManifest unscopedManifest = null;

    private static List<BundleManifest> bundleManifests = new ArrayList<>();

    private static final File bundleFile = new File("src/test/resources/scoping/bundles/bug331767");

    private static final Object SCOPING_ATTRIBUTE_NAME = "module_scope";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try (FileReader reader = new FileReader(new File(bundleFile, "META-INF/MANIFEST.MF"))) {
            manifest = BundleManifestFactory.createBundleManifest(reader);
            reader.close();
            unscopedManifest = BundleManifestFactory.createBundleManifest(manifest.toDictionary());
            bundleManifests.add(manifest);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Test
    public void testScoping() throws UnsupportedBundleManifestVersionException, DuplicateExportException, DuplicateBundleSymbolicNameException {
        Scoper scoper = new Scoper(bundleManifests, SCOPE_NAME);
        scoper.scope();
        checkImports();
        checkDynamicImports();
        checkExports();
    }

    private void checkImports() {
        ImportPackage importPackage = manifest.getImportPackage();
        ImportPackage unscopedImportPackage = unscopedManifest.getImportPackage();
        List<ImportedPackage> ipList = importPackage.getImportedPackages();
        List<ImportedPackage> uipList = unscopedImportPackage.getImportedPackages();
        for (int i = 0; i < ipList.size(); i++) {
            assertEquals(ipList.get(i).getPackageName(), uipList.get(i).getPackageName());
            assertEquals(ipList.get(i).getVersion(), uipList.get(i).getVersion());
            for (String scopedAttribute : uipList.get(i).getAttributes().keySet()) {
                assertTrue(ipList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            assertTrue(ipList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            assertEquals(uipList.get(i).getAttributes().size() + 1, ipList.get(i).getAttributes().size());
        }
    }

    private void checkDynamicImports() {
        DynamicImportPackage dynamicImportPackage = manifest.getDynamicImportPackage();
        DynamicImportPackage unscopedDynamicImportPackage = unscopedManifest.getDynamicImportPackage();
        List<DynamicallyImportedPackage> dipList = dynamicImportPackage.getDynamicallyImportedPackages();
        List<DynamicallyImportedPackage> udipList = unscopedDynamicImportPackage.getDynamicallyImportedPackages();
        for (int i = 0; i < udipList.size(); i++) {
            assertEquals(dipList.get(i).getPackageName(), udipList.get(i).getPackageName());
            assertEquals(dipList.get(i).getVersion(), udipList.get(i).getVersion());
            for (String scopedAttribute : udipList.get(i).getAttributes().keySet()) {
                assertTrue(dipList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            assertTrue(dipList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            assertEquals(udipList.get(i).getAttributes().size() + 1, dipList.get(i).getAttributes().size());
        }
        assertEquals(dipList.size(), udipList.size() * 2);
    }

    private void checkExports() {
        ExportPackage exportPackage = manifest.getExportPackage();
        ExportPackage unscopedExportPackage = unscopedManifest.getExportPackage();
        List<ExportedPackage> epList = exportPackage.getExportedPackages();
        List<ExportedPackage> uepList = unscopedExportPackage.getExportedPackages();
        for (int i = 0; i < epList.size(); i++) {
            assertEquals(epList.get(i).getPackageName(), uepList.get(i).getPackageName());
            assertEquals(epList.get(i).getVersion(), uepList.get(i).getVersion());
            for (String scopedAttribute : uepList.get(i).getAttributes().keySet()) {
                assertTrue(epList.get(i).getAttributes().containsKey(scopedAttribute));
            }
            assertTrue(epList.get(i).getAttributes().containsKey(SCOPING_ATTRIBUTE_NAME));
            assertEquals(uepList.get(i).getAttributes().size() + 1, epList.get(i).getAttributes().size());
        }
    }

}
