// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.utils;

import java.io.File;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

/*
* Created by bhe on Sep 25, 2019
*/
public class StudioKeysFileCheck {

    public static final String ENCRYPTION_KEY_FILE_SYS_PROP = "encryption.keys.file";

    public static final String ENCRYPTION_KEY_FILE_JVM_PARAM = "-D" + ENCRYPTION_KEY_FILE_SYS_PROP;

    public static final String ENCRYPTION_KEY_FILE_NAME = "studio.keys";

    private static final Logger LOGGER = Logger.getLogger(StudioKeysFileCheck.class);

    private static final String JAVA_VERSION_PROP = "java.version";

    public static final String JAVA_VERSION_MINIMAL_STRING = "1.8.0_161";

    private static final JavaVersion JAVA_VERSION_MINIMAL = new JavaVersion(JAVA_VERSION_MINIMAL_STRING);

    public static final String JAVA_VERSION_MAXIMUM_STRING = "11";

    private static final JavaVersion JAVA_VERSION_MAXIMUM = new JavaVersion(JAVA_VERSION_MAXIMUM_STRING);

    private StudioKeysFileCheck() {

    }

    /**
     * Check whether system property: encryption.keys.file is set, if not then set value of encryption.keys.file
     */
    public static void check(File confDir) {
        if (confDir == null) {
            IllegalArgumentException e = new IllegalArgumentException("Encryption keys file path invalid");
            LOGGER.error(e);
            throw e;
        }
        String keyFile = System.getProperty(ENCRYPTION_KEY_FILE_SYS_PROP);
        if (keyFile == null || keyFile.isEmpty() || !new File(keyFile).exists()) {
            keyFile = Paths.get(confDir.getAbsolutePath(), ENCRYPTION_KEY_FILE_NAME).toString();
            System.setProperty(ENCRYPTION_KEY_FILE_SYS_PROP, keyFile);
        }
        LOGGER.info("encryptionKeyFilePath: " + keyFile);
    }

    /**
     * Validate java version, throw runtime exception if not satisfied.
     */
    public static void validateJavaVersion() {
        String currentVersion = System.getProperty(JAVA_VERSION_PROP);
        JavaVersion cv = new JavaVersion(currentVersion);
        if (cv.compareTo(JAVA_VERSION_MINIMAL) < 0) {
            VersionException e = new VersionException(VersionException.ERR_JAVA_VERSION_UPGRADE_REQUIRED,
                    "Java upgrade required, minimal required java version is " + JAVA_VERSION_MINIMAL_STRING
                            + ", current version is " + currentVersion);
            LOGGER.error(e.getMessage(), e);
            throw e;
        }

        JavaVersion currentMajor = new JavaVersion(String.valueOf(cv.getMajor()));
        if (currentMajor.compareTo(JAVA_VERSION_MAXIMUM) > 0) {
            VersionException e = new VersionException(VersionException.ERR_JAVA_VERSION_NOT_SUPPORTED,
                    "The maximum Java version supported by Studio is " + JAVA_VERSION_MAXIMUM_STRING
                            + ". Your current version is " + currentVersion);
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    public static String getJavaVersion() {
        return System.getProperty(JAVA_VERSION_PROP);
    }
}
