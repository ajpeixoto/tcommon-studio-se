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
package org.talend.librariesmanager.librarydata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.designer.maven.aether.util.MavenLibraryResolverProvider;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;

public class LibraryDataService {

    private static Logger logger = Logger.getLogger(LibraryDataService.class);

    /**
     * {@value}
     * <p>
     * System property of build studio library license file, the default value is <b>false</b>.
     */
    public static final String KEY_LIBRARIES_BUILD_LICENSE = "talend.libraries.buildLicense"; //$NON-NLS-1$

    /**
     * {@value}
     * <p>
     * System property of build studio library file, the default value is <b>false</b>.
     */
    public static final String KEY_LIBRARIES_BUILD_JAR = "talend.libraries.buildJar"; //$NON-NLS-1$

    /**
     * {@value}
     * <p>
     * System property of studio library data file folder path, the default value is Studio install
     * folder/configuration.
     */
    public static final String KEY_LIBRARIES_DATA_FOLDER = "talend.libraries.folder"; //$NON-NLS-1$

    /**
     * {@value}
     * <p>
     * System property of retrieve library data, the default value is <b>false</b>.
     */
    public static final String KEY_BUILD_LIBRARY_IF_LICENSE_MISSING = "talend.libraries.buildIfLicenseMissing"; //$NON-NLS-1$

    private static final String LIBRARIES_DATA_FILE_NAME = "library_data.index"; //$NON-NLS-1$

    private static final String UNRESOLVED_LICENSE_NAME = "UNKNOWN"; //$NON-NLS-1$

    private static boolean buildLibraryIfFileMissing = true;

    /**
     * make it as static to avoid call of getInstance, so that we can avoid dead lock in some cases
     */
    private static boolean buildLibraryLicense = Boolean
            .valueOf(System.getProperty(KEY_LIBRARIES_BUILD_LICENSE, Boolean.FALSE.toString()));

    private static boolean buildLibraryIfLicenseMissing = Boolean
            .valueOf(System.getProperty(KEY_BUILD_LIBRARY_IF_LICENSE_MISSING, Boolean.FALSE.toString()));

    private static boolean buildLibraryJarFile = Boolean
            .valueOf(System.getProperty(KEY_LIBRARIES_BUILD_JAR, Boolean.FALSE.toString()));

    private static final Map<String, Library> mvnToLibraryMap = new ConcurrentHashMap<String, Library>();

    private static final Map<String, Library> dynamicDisLibraryMap = new ConcurrentHashMap<String, Library>();

    private static LibraryDataService instance;

    private LibraryDataJsonProvider dataProvider;

    private Map<String, Library> retievedCache = new HashMap<String, Library>();

    private int repeatTime = 3;

    private LibraryLicense unknownLicense;;

    private LibraryDataService() {
        File studioLibraryDataFile = getStudioLibraryDataFile();
        if (buildLibraryLicense) {
            if (studioLibraryDataFile.exists()) {
                studioLibraryDataFile.delete();
            }
        }
        unknownLicense = new LibraryLicense();
        unknownLicense.setName(UNRESOLVED_LICENSE_NAME);
        dataProvider = new LibraryDataJsonProvider(studioLibraryDataFile);
        File currentUserDataFile = getCurrentUserLibraryDataFile();
        Map<String, Library> studioLibraryDataMap = dataProvider.loadLicenseData();

        if (!StringUtils.equals(currentUserDataFile.getAbsolutePath(), studioLibraryDataFile.getAbsolutePath())) {
            dataProvider = new LibraryDataJsonProvider(currentUserDataFile);
            Map<String, Library> userLibraryDataMap = dataProvider.loadLicenseData();
            if (userLibraryDataMap.size() == 0) {
                mvnToLibraryMap.putAll(studioLibraryDataMap);
                dynamicDisLibraryMap.putAll(getCacheForDynamicDist(studioLibraryDataMap));
            } else {
                mvnToLibraryMap.putAll(userLibraryDataMap);
                dynamicDisLibraryMap.putAll(getCacheForDynamicDist(userLibraryDataMap));
            }
        } else {
            mvnToLibraryMap.putAll(studioLibraryDataMap);
            dynamicDisLibraryMap.putAll(getCacheForDynamicDist(studioLibraryDataMap));
        }
    }

    private Map<String, Library> getCacheForDynamicDist(Map<String, Library> studioLibraryDataMap) {
        Map<String, Library> dynamicDistCacheMap = new HashMap<String, Library>();
        if (studioLibraryDataMap != null) {
            for (String key : studioLibraryDataMap.keySet()) {
                Library library = studioLibraryDataMap.get(key);
                String version = library.getVersion();
                String firstVersion = version.indexOf(".")==-1 ? version : version.substring(0, version.indexOf(".")); 
                String dynamicDistKey = library.getGroupId() + "/" + library.getArtifactId() + "/"
                        + firstVersion;
                dynamicDistCacheMap.put(dynamicDistKey, library);
            }
        }
        return dynamicDistCacheMap;
    }
    public static LibraryDataService getInstance() {
        if (instance == null) {
            synchronized (LibraryDataService.class) {
                if (instance == null) {
                    instance = new LibraryDataService();
                }
            }
        }
        return instance;
    }

    public void buildLibraryLicenseData(Set<String> mvnUrlList) {
        buildLibraryLicenseData(mvnUrlList, null);
    }

    public void buildLibraryLicenseData(Set<String> mvnUrlList, Map<String, List<String[]>> licenseMap) {
        for (String mvnUrl : mvnUrlList) {
            Library libraryObj = getLicenseDataFromMap(mvnUrl, licenseMap);
            if (libraryObj == null)
                libraryObj = resolve(mvnUrl);
            if (!libraryObj.isLicenseMissing() || !libraryObj.isPomMissing()) {
                mvnToLibraryMap.put(getShortMvnUrl(mvnUrl), libraryObj);
            }
        }
        for (String key : mvnToLibraryMap.keySet()) {
            Library lib = mvnToLibraryMap.get(key);
            List<LibraryLicense> licenses = lib.getLicenses();
            Iterator it = licenses.iterator();
            boolean isRemoved = false;
            while (it.hasNext()) {
                LibraryLicense l = (LibraryLicense) it.next();
                if (StringUtils.isEmpty(l.getName()) && StringUtils.isEmpty(l.getUrl())) {
                    it.remove();
                    isRemoved = true;
                }
            }
            if (lib.isPomMissing()) {
                lib.getLicenses().clear();
                lib.getLicenses().add(unknownLicense);
            } else {
                if ((isRemoved && licenses.size() == 0)) {
                    licenses.add(unknownLicense);
                    lib.setLicenseMissing(true);
                }
            }
        }
        dataProvider.saveLicenseData(mvnToLibraryMap);
    }
    
    private Library getLicenseDataFromMap(String mvnUrl, Map<String, List<String[]>> licenseMap) {
        if (licenseMap == null)
            return null;
        MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(mvnUrl);
        String shourtMvnUrl = getShortMvnUrl(mvnUrl);
        String gav = String.format("%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        List<String[]> licenses = licenseMap.get(gav);
        Library libraryObj = null;
        if (licenses != null && licenses.size() > 0) {
            libraryObj = new Library();
            libraryObj.setGroupId(artifact.getGroupId());
            libraryObj.setArtifactId(artifact.getArtifactId());
            libraryObj.setVersion(artifact.getVersion());
            libraryObj.setMvnUrl(shourtMvnUrl);
            libraryObj.setType(artifact.getType());
            libraryObj.setClassifier(artifact.getClassifier());
            libraryObj.setPomMissing(false);
            for (String[] licenseContent : licenses) {
                LibraryLicense license = new LibraryLicense();
                if (licenseContent != null && licenseContent.length == 2) {
                    license.setName(licenseContent[0]);
                    license.setUrl(licenseContent[1]);
                }
                libraryObj.getLicenses().add(license);
            }
        } else {
            return null;
        }

        return libraryObj;
    }

    private Library resolve(String mvnUrl) {
        MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(mvnUrl);
        String shourtMvnUrl = getShortMvnUrl(mvnUrl);
        Library libraryObj = new Library();
        libraryObj.setGroupId(artifact.getGroupId());
        libraryObj.setArtifactId(artifact.getArtifactId());
        libraryObj.setVersion(artifact.getVersion());
        libraryObj.setMvnUrl(shourtMvnUrl);
        libraryObj.setType(artifact.getType());
        libraryObj.setClassifier(artifact.getClassifier());
        libraryObj.setPomMissing(true);
        boolean isRetry = false;
        logger.debug("Resolving artifact descriptor:" + shourtMvnUrl); //$NON-NLS-1$
        if (!retievedCache.containsKey(shourtMvnUrl)) {
            for (int repeated = 0; repeated < repeatTime; repeated++) {
                try {
                    if (repeated > 0) {
                        Thread.sleep(1000); // To avoid server connection pool shut down
                    }
                    Map<String, Object> properties = resolveDescProperties(artifact, false);
                    if (properties != null && properties.size() > 0) {
                        parseDescriptorResult(libraryObj, properties, false);
                        if (libraryObj.getLicenses().size() == 0) {
                            libraryObj.setLicenseMissing(true);
                            libraryObj.getLicenses().add(unknownLicense);
                        }
                        libraryObj.setPomMissing(false);
                        if (null == properties.get("type") || "".equals(properties.get("type"))) {
                            libraryObj.setType(MavenConstants.PACKAGING_POM);
                        }
                    }
                    isRetry = false;
                } catch (Exception ex) {
                    if (isBuildLibrariesData()) {
                        isRetry = true;
                    }
                    logger.info(ex);
                }
                if (!isRetry) {
                    break;
                }
            }
            logger.debug("Resolved artifact descriptor:" + getShortMvnUrl(mvnUrl)); //$NON-NLS-1$
            if (buildLibraryJarFile) {
                boolean jarMissing = false;
                try {
                    MavenLibraryResolverProvider.getInstance().resolveArtifact(artifact, false);
                } catch (Exception ex) {
                    jarMissing = true;
                } finally {
                    libraryObj.setJarMissing(jarMissing);
                }
            }
            retievedCache.put(shourtMvnUrl, libraryObj);
        } else {
            libraryObj = retievedCache.get(shourtMvnUrl);
        }
        return libraryObj;
    }

    public void setJarMissing(String mvnUrl) {
        String shortMvnUrl = getShortMvnUrl(mvnUrl);
        Library libraryObj = mvnToLibraryMap.get(shortMvnUrl);
        if (libraryObj != null) {
            libraryObj.setJarMissing(true);
        }
        saveData();
    }

    public void saveData() {
        dataProvider.saveLicenseData(mvnToLibraryMap);
    }

    private Map<String, Object> resolveDescProperties(MavenArtifact artifact, boolean is4Parent) throws Exception {
        Map<String, Object> properties = MavenLibraryResolverProvider.getInstance().resolveDescProperties(artifact, is4Parent);
        return properties;
    }

    public void fillLibraryDataByRemote(String mvnUrl, MavenArtifact artifact) {
        Library libraryObj = resolve(mvnUrl);
        fillLibraryData(libraryObj, artifact);
        mvnToLibraryMap.put(getShortMvnUrl(mvnUrl), libraryObj);
        String version = libraryObj.getVersion();
        String firstVersion = version.indexOf(".")==-1 ? version : version.substring(0, version.indexOf("."));
        dynamicDisLibraryMap.put(
                libraryObj.getGroupId() + "/" + libraryObj.getArtifactId() + "/" + firstVersion,
                libraryObj);
    }

    public boolean fillLibraryDataUseCache(String mvnUrl, MavenArtifact artifact) {
        boolean isExist = false;
        String shortMvnUrl = getShortMvnUrl(mvnUrl);
        Library object = mvnToLibraryMap.get(shortMvnUrl);
        if (object != null) {
            fillLibraryData(object, artifact);
            isExist = !isPackagePom(object);
        }
        return isExist;
    }

    public boolean fillLibraryDatabyDynamicDistCache(MavenArtifact artifact) {
        boolean isExist = false;
        String version = artifact.getVersion();
        String firstVersion = version.indexOf(".") == -1 ? version : version.substring(0, version.indexOf("."));
        String key = artifact.getGroupId() + "/" + artifact.getArtifactId() + "/"
                + firstVersion;
        Library object = dynamicDisLibraryMap.get(key);
        if (object != null) {
            fillLibraryData(object, artifact);
            isExist = !isPackagePom(object);
        }
        return isExist;
    }
    private boolean isPackagePom(Library libraryObj) {
        if (libraryObj != null) {
            if ("pom".equalsIgnoreCase(libraryObj.getType())) {
                return true;
            }
            if (libraryObj.isJarMissing() || libraryObj.isPomMissing()) {
                return true;
            }
            for (LibraryLicense license : libraryObj.getLicenses()) {
                if (MavenConstants.DOWNLOAD_MANUAL.equalsIgnoreCase(license.getDistribution())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void parseDescriptorResult(Library libraryObj, Map<String, Object> properties, boolean is4Parent) throws Exception {
        if (properties.size() == 0) {
            libraryObj.setPomMissing(true);
        }
        if (!is4Parent) {
            String type = String.valueOf(properties.get("type"));
            libraryObj.setType(MavenConstants.PACKAGING_BUNDLE.equalsIgnoreCase(type) ? MavenConstants.PACKAGING_JAR : type); //$NON-NLS-1$
        }
        int licenseCount = 0;
        if (properties.containsKey("license.count")) { //$NON-NLS-1$
            licenseCount = (int) properties.get("license.count"); //$NON-NLS-1$
        }
        if (licenseCount > 0) {
            if (libraryObj.getLicenses() != null) {
                libraryObj.getLicenses().clear();
            }
        }

        for (int i = 0; i < licenseCount; i++) {
            String name = getStringValue(properties.get("license." + i + ".name")); //$NON-NLS-1$ //$NON-NLS-2$
            String url = getStringValue(properties.get("license." + i + ".url")); //$NON-NLS-1$ //$NON-NLS-2$
            String comments = getStringValue(properties.get("license." + i + ".comments")); //$NON-NLS-1$ //$NON-NLS-2$
            String distribution = getStringValue(properties.get("license." + i + ".distribution")); //$NON-NLS-1$ //$NON-NLS-2$

            LibraryLicense license = new LibraryLicense();
            license.setName(name);
            license.setUrl(url);
            license.setComments(comments);
            license.setDistribution(distribution);
            if (!"".equals(name) || !"".equals(url)) {
                libraryObj.getLicenses().add(license);
            }
        }

        if (libraryObj.getLicenses().size() == 0) {
            if (properties.containsKey("parent.groupId")) {
                fetchFromParent(libraryObj, (String) properties.get("parent.groupId"),
                        (String) properties.get("parent.artifactId"), (String) properties.get("parent.version"));
            }
        }
    }

    private void fetchFromParent(Library libraryObj, String parentGroupId, String parentArtifactId, String parentVersion)
            throws Exception {
        MavenArtifact parent = new MavenArtifact();
        parent.setGroupId(parentGroupId);
        parent.setArtifactId(parentArtifactId);
        parent.setVersion(parentVersion);
        parent.setClassifier(libraryObj.getClassifier());
        parent.setType(libraryObj.getType());
        Map<String, Object> properties = resolveDescProperties(parent, true);
        parseDescriptorResult(libraryObj, properties, true);
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static boolean isBuildLibrariesData() {
        /**
         * make the method as static to avoid call of getInstance, so that we can avoid dead lock in some cases
         */

        if (buildLibraryLicense) {
            return true;
        }
        if (buildLibraryIfFileMissing && !getStudioLibraryDataFile().exists()) {
            return true;
        }
        return false;
    }

    /**
     * For shared studio, user's library data file not same with studio one
     * 
     * @return
     */
    private File getCurrentUserLibraryDataFile() {
        File folder = null;
        try {
            folder = new File(FileLocator
                    .toFileURL(FileLocator
                            .find(Platform.getBundle(LibrariesManagerUtils.BUNDLE_DI), new Path("/resources"), null))
                    .getFile());
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return new File(folder, LIBRARIES_DATA_FILE_NAME);
    }

    private static File getStudioLibraryDataFile() {
        String folder = System.getProperty(KEY_LIBRARIES_DATA_FOLDER);
        if (folder == null) {
            try {
                folder = new File(FileLocator
                        .toFileURL(FileLocator
                                .find(Platform.getBundle(LibrariesManagerUtils.BUNDLE_DI), new Path("/resources"),
                                        null))
                        .getFile()).getAbsolutePath();
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
        return new File(folder, LIBRARIES_DATA_FILE_NAME);
    }

    private void fillLibraryData(Library object, MavenArtifact artifact) {
        if (object != null && artifact != null) {
            List<LibraryLicense> objLicenseList = object.getLicenses();
            LibraryLicense bestLicense = null;
            if (objLicenseList.size() >= 1) {
                bestLicense = objLicenseList.get(0);
            }
            if (bestLicense != null) {
                String licenseName = bestLicense.getName();
                if (licenseName == null || "null".equalsIgnoreCase(licenseName)) {
                    licenseName = UNRESOLVED_LICENSE_NAME;
                }
                artifact.setLicense(licenseName);
                artifact.setLicenseUrl(bestLicense.getUrl());
            }
            artifact.setType(object.getType());
            // URL
            if (StringUtils.isEmpty(artifact.getUrl()) && StringUtils.isNotEmpty(object.getUrl())) {
                artifact.setUrl(object.getUrl());
            }
            if (isPackagePom(object)) {
                artifact.setDistribution(MavenConstants.DOWNLOAD_MANUAL);
            }
        } else {
            artifact.setDistribution(MavenConstants.DOWNLOAD_MANUAL);
        }
    }

    private String getShortMvnUrl(String mvnUrl) {
        if (mvnUrl.indexOf("@") > 0 && mvnUrl.indexOf("!") > 0) {
            mvnUrl = "mvn:" + mvnUrl.substring(mvnUrl.indexOf("!") + 1);
        }
        return mvnUrl;
    }
}
