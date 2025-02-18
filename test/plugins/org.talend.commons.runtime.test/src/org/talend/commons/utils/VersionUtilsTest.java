// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.utils.ProductVersion;
import org.talend.utils.io.FilesUtils;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class VersionUtilsTest {

    private File mojo_properties;

    private File eclipseproductFile;

    private Map<String, String> originalMojoVersions;

    @Before
    public void setUp() throws Exception {
        originalMojoVersions = new HashMap<>();
        Stream.of(MojoType.values()).map(MojoType::getVersionKey).filter(v -> System.getProperty(v) != null)
                .forEach(v -> originalMojoVersions.put(v, System.getProperty(v)));
        mojo_properties = new Path(Platform.getConfigurationLocation().getURL().getPath()).append("mojo_version.properties") //$NON-NLS-1$
                .toFile();
        backupEcilpseproductFile();
    }

    /**
     * Test method for {@link org.talend.commons.utils.VersionUtils#getTalendVersion()}.
     */
    @Test
    public void testGetTalendVersion() {
        ProductVersion talendVersion = ProductVersion.fromString(VersionUtils.getTalendVersion());
        ProductVersion studioVersion = ProductVersion.fromString(VersionUtils.getDisplayVersion());
        assertEquals(studioVersion, talendVersion);
    }

    @Test
    public void testGetMojoVersion() throws Exception {
        testMojoVersion(MojoType.CI_BUILDER, "-SNAPSHOT");
        testMojoVersion(MojoType.CI_BUILDER, "-M3");
        testMojoVersion(MojoType.CI_BUILDER, "");
    }

    private void testMojoVersion(MojoType mojoType, String testVersion) throws Exception {
        System.setProperty(mojoType.getVersionKey(), "");

        String talendVersion = VersionUtils.getTalendVersion();
        File artifactIdFolder = new File(mojoType.getMojoArtifactIdFolder());
        String majorVersion = StringUtils.substringBeforeLast(talendVersion, ".");
        String minorVersion = StringUtils.substringAfterLast(talendVersion, ".");
        minorVersion = (Integer.valueOf(minorVersion) + 1000) + "";
        testVersion = majorVersion + "." + minorVersion + testVersion;
        File versionFolder = new File(artifactIdFolder, testVersion);
        versionFolder.mkdir();
        new File(versionFolder, mojoType.getArtifactId() + "-" + testVersion + ".jar").createNewFile();
        new File(versionFolder, mojoType.getArtifactId() + "-" + testVersion + ".pom").createNewFile();

        assertEquals(testVersion, VersionUtils.getMojoVersion(mojoType));

        FilesUtils.deleteFolder(versionFolder, true);
    }

    private void backupEcilpseproductFile() throws Exception {
        File installFolder = URIUtil.toFile(URIUtil.toURI(Platform.getInstallLocation().getURL()));
        eclipseproductFile = new File(installFolder, ".eclipseproduct");//$NON-NLS-1$
        File targetFile = new File(installFolder, "bak.eclipseproduct");//$NON-NLS-1$
        if (targetFile.exists()) {
            targetFile.delete();
        }
        FilesUtils.copyFile(eclipseproductFile, targetFile);
    }

    private void restoreEclipseproductFile() throws Exception {
        File installFolder = URIUtil.toFile(URIUtil.toURI(Platform.getInstallLocation().getURL()));
        File backupFile = new File(installFolder, "bak.eclipseproduct");//$NON-NLS-1$
        File targetFile = eclipseproductFile;
        try {
            if (targetFile.exists()) {
                targetFile.delete();
            }
            FilesUtils.copyFile(backupFile, targetFile);
        } finally {
            backupFile.delete();
        }
    }

    @Test
    public void testGetTalendPureVersion() {
        String expect = "7.2.1";
        String test = "Talend Cloud Big Data-7.2.1.20190620_1446";
        String result = VersionUtils.getTalendPureVersion(test);
        assertEquals(expect,result);
        
        test = "Talend Cloud Real-Time Big Data Platform-7.2.1.20190620_1446";
        result = VersionUtils.getTalendPureVersion(test);
        assertEquals(expect, result);

        expect = "7.3.1";
        test = "Talend Cloud Big Data-7.3.1.20190917_1941-SNAPSHOT";
        result = VersionUtils.getTalendPureVersion(test);
        assertEquals(expect, result);

        test = "Talend Cloud Real-Time Big Data Platform-7.3.1.20190917_1941-SNAPSHOT";
        result = VersionUtils.getTalendPureVersion(test);
        assertEquals(expect,result);
    }

    @Test
    public void testGetProductVersionWithoutBranding() {
        String expect = "7.3.1.20190620_1446";
        String test = "Talend Cloud Big Data-7.3.1.20190620_1446";
        String result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect,result);
        
        test = "Talend Cloud Real-Time Big Data Platform-7.3.1.20190620_1446";
        result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect, result);

        expect = "7.3.1.20190917_1941-SNAPSHOT";
        test = "Talend Cloud Big Data-7.3.1.20190917_1941-SNAPSHOT";
        result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect, result);

        test = "Talend Cloud Real-Time Big Data Platform-7.3.1.20190917_1941-SNAPSHOT";
        result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect,result);

        expect = "7.3.1.20190919_1941-patch";
        test = "Talend Cloud Big Data-7.3.1.20190919_1941-patch";
        result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect, result);

        test = "Talend Cloud Real-Time Big Data Platform-7.3.1.20190919_1941-patch";
        result = VersionUtils.getProductVersionWithoutBranding(test);
        assertEquals(expect, result);
    }

    @Test
    public void testIsInvalidProductVersion() {
        // test nightly/milestone build
        // do not check
        assertFalse(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-SNAPSHOT",
                "Talend Cloud Big Data-7.3.1.20200209_1446-SNAPSHOT"));
        assertFalse(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-M1",
                "Talend Cloud Big Data-7.3.1.20200209_1446-SNAPSHOT"));
        assertFalse(
                VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-M1", "Talend Cloud Big Data-7.3.1.20200209_1446-M2"));
        assertFalse(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-SNAPSHOT",
                "Talend Cloud Big Data-7.3.1.20200209_1446-M2"));
        // do check
        assertTrue(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-SNAPSHOT",
                "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertTrue(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-M1",
                "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));

        // test release build
        assertTrue(
                VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941", "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertTrue(VersionUtils.isInvalidProductVersion("7.3.1.20200201_1941-patch",
                "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertFalse(
                VersionUtils.isInvalidProductVersion("7.3.1.20200209_1941-patch", "Talend Cloud Big Data-7.3.1.20200201_1446"));
    }

    @Test
    public void testProductVersionIsNewer() {
        assertTrue(
                VersionUtils.productVersionIsNewer("7.3.1.20200211_1941-M1", "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertTrue(VersionUtils.productVersionIsNewer("7.3.1.20200211_1941", "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertTrue(VersionUtils.productVersionIsNewer("7.3.1.20200211_1941-patch",
                "Talend Cloud Big Data-7.3.1.20200209_1446-patch"));
        assertFalse(VersionUtils.productVersionIsNewer("7.3.1.20200219_1941-patch", "Talend Cloud Big Data-7.3.1.20200221_1446"));

        // test nightly/milestone build
        assertFalse(VersionUtils.productVersionIsNewer("7.3.1.20200201_1941-SNAPSHOT",
                "Talend Cloud Big Data-7.3.1.20200209_1446-SNAPSHOT"));
        assertFalse(VersionUtils.productVersionIsNewer("7.3.1.20200201_1941-M1",
                "Talend Cloud Big Data-7.3.1.20200209_1446-SNAPSHOT"));
        assertFalse(
                VersionUtils.productVersionIsNewer("7.3.1.20200201_1941-M1", "Talend Cloud Big Data-7.3.1.20200209_1446-M2"));
        assertFalse(VersionUtils.productVersionIsNewer("7.3.1.20200201_1941-SNAPSHOT",
                "Talend Cloud Big Data-7.3.1.20200209_1446-M2"));
    }

    @Test
    public void testGetSimplifiedPatchName() {
        String expect0 = "R2020-11-7.3.1";
        assertEquals(expect0, VersionUtils.getSimplifiedPatchName("Patch_20201114_R2020-11_v1-7.3.1"));
        String expect1 = "R2020-11-7.3.1";
        assertEquals(expect1, VersionUtils.getSimplifiedPatchName("Patch_20201114_R2020-11_v2-7.3.1"));
        String expect2 = "R2020-11-7.4.1";
        assertEquals(expect2, VersionUtils.getSimplifiedPatchName("Patch_20201114_R2020-11_v1-7.4.1"));
    }

    @Test
    public void testGetDisplayPatchVersion() {
        assertEquals("R2021-11", VersionUtils.getDisplayPatchVersion("R2021-11v1"));
        assertEquals("R2021-11v2", VersionUtils.getDisplayPatchVersion("R2021-11v2"));
        assertEquals("TPS-8888", VersionUtils.getDisplayPatchVersion("TPS-8888v1"));
        assertEquals("TPS-8888v2", VersionUtils.getDisplayPatchVersion("TPS-8888v2"));
    }

    @After
    public void tearDown() throws Exception {
        if (mojo_properties != null && mojo_properties.exists()) {
            mojo_properties.delete();
        }
        originalMojoVersions.forEach(System::setProperty);

        restoreEclipseproductFile();
    }

}
