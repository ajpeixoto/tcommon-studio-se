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
package org.talend.commons.utils.generation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.utils.JavaVersion;
import org.talend.utils.StudioKeysFileCheck;
import org.talend.utils.VersionException;

/**
 * Utilities around perl stuff. <br/>
 *
 * $Id: JavaUtils.java 1804 2007-02-04 09:50:15Z rli $
 *
 */
public final class JavaUtils {
    
    private static final String SYS_PROP_JAVA_COMPLIANCE_LEVEL = "job.compliance";

    public static final String JAVAMODULE_PLUGIN_ID = "org.talend.designer.codegen.javamodule"; //$NON-NLS-1$

    public static final String JAVA_LAUNCHCONFIGURATION = "org.talend.designer.runprocess.launchConfigurationJava"; //$NON-NLS-1$

    public static final String PROJECT_JAVA_VERSION_KEY = "talend.project.java.version"; //$NON-NLS-1$

    public static final String DEFAULT_VERSION = getComplianceLevel();

    public static final List<String> AVAILABLE_VERSIONS = Arrays.asList(DEFAULT_VERSION);

    public static final String ALLOW_JAVA_INTERNAL_ACCESS = "allow.java.internal.access"; //$NON-NLS-1$

    public static final String ALLOW_JAVA_INTERNAL_ACCESS_BACKUP = "allow.java.internal.access.backup";
    
    public static final String JOB_COMPLIANCE_SET = "job.compliance.set";
    
    public static final String CUSTOM_ACCESS_SETTINGS = "custom.access.settings"; //$NON-NLS-1$

    public static final String PROCESSOR_TYPE = "javaProcessor"; //$NON-NLS-1$

    public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    public static final String JAVA_APP_NAME = "java";//$NON-NLS-1$

    /** added by rxl. */
    public static final String JAVATIP = "//The function of generating Java code haven't achive yet" //$NON-NLS-1$
            + System.getProperty("line.separator") + "public class JavaTest extends Test {}"; //$NON-NLS-1$ //$NON-NLS-2$

    /** Java File Extension. */
    public static final String JAVA_EXTENSION = ".java"; //$NON-NLS-1$

    /** drl File Extension. */
    public static final String DRL_EXTENSION = ".drl"; //$NON-NLS-1$

    /** Item File Extension. */
    public static final String ITEM_EXTENSION = ".item"; //$NON-NLS-1$

    /** Java sqltemplate Extension. */
    public static final String JAVA_SQLPATTERN_EXTENSION = ".sqltemplate"; //$NON-NLS-1$

    /** Java Context Extension. */
    public static final String JAVA_CONTEXT_EXTENSION = ".properties"; //$NON-NLS-1$

    /** Java Directory. */
    public static final String JAVA_DIRECTORY = "java"; //$NON-NLS-1$

    /** Java Routines Directory. */
    public static final String JAVA_ROUTINES_DIRECTORY = "routines"; //$NON-NLS-1$

    // TODO check refrerences of JAVA_ROUTINES_DIRECTORY
    /** Java Routines Jar Directory. */
    public static final String JAVA_ROUTINESJAR_DIRECTORY = "routinesjar"; //$NON-NLS-1$

    /** Java Pig Directory. */
    public static final String JAVA_PIG_DIRECTORY = "pig"; //$NON-NLS-1$

    /** Java Pig UDF Directory. */
    public static final String JAVA_PIGUDF_DIRECTORY = "pigudf"; //$NON-NLS-1$

    /** Java Beans Directory. */
    public static final String JAVA_BEANS_DIRECTORY = "beans"; //$NON-NLS-1$

    /** Java Beans Jar Directory. */
    public static final String JAVA_BEANSJAR_DIRECTORY = "beansjar"; //$NON-NLS-1$

    /** Java SQLTemplate Directory. */
    public static final String JAVA_SQLPATTERNS_DIRECTORY = "sqltemplates"; //$NON-NLS-1$

    /** Java system Directory. */
    public static final String JAVA_SYSTEM_DIRECTORY = "system"; //$NON-NLS-1$

    /** Java Routines api Directory. */
    public static final String JAVA_SYSTEM_ROUTINES_API_DIRECTORY = "api"; //$NON-NLS-1$

    /** Java UserDefined Directory */
    public static final String JAVA_USER_DEFINED = "user defined"; //$NON-NLS-1$

    /** Java Lib Directory. */
    public static final String JAVA_LIB_DIRECTORY = "lib"; //$NON-NLS-1$

    /** Java internal Directory. */
    public static final String JAVA_INTERNAL_DIRECTORY = "internal"; //$NON-NLS-1$

    /** Java DB Mapping Directory , and must be same as MetadataTalendType.MAPPING_FOLDER */
    public static final String JAVA_XML_MAPPING = "xmlMappings"; //$NON-NLS-1$

    /** Java Rules Directory. */
    public static final String JAVA_RULES_DIRECTORY = "rules"; //$NON-NLS-1$

    /** Java Rules Template Directory. */
    public static final String JAVA_RULES_TEMPLATE_DIRECTORY = "template"; //$NON-NLS-1$

    /** Java Metadata Directory. */
    public static final String JAVA_METADATA_DIRECTORY = "metadata"; //$NON-NLS-1$

    /** Java contexts Directory. */
    public static final String JAVA_CONTEXTS_DIRECTORY = "contexts"; //$NON-NLS-1$

    /** Java datass Directory. */
    public static final String JAVA_DATAS_DIRECTORY = "datas"; //$NON-NLS-1$

    /** Java ClassPath Separator. */
    public static final String JAVA_CLASSPATH_SEPARATOR = (Platform.getOS().compareTo(Platform.WS_WIN32) == 0) ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String JAVA_CP = "-cp"; //$NON-NLS-1$

    public static final String ROUTINE_JAR_DEFAULT_VERSION = "1.0";//$NON-NLS-1$

    public static final String ROUTINE_JAR_NAME = "routines"; //$NON-NLS-1$

    public static final String BEANS_JAR_NAME = "beans"; //$NON-NLS-1$

    public static final String PIGUDFS_JAR_NAME = "pigudfs"; //$NON-NLS-1$

    public static final String ROUTINES_JAR = ROUTINE_JAR_NAME + FileExtensions.JAR_FILE_SUFFIX;

    public static final String BEANS_JAR = BEANS_JAR_NAME + FileExtensions.JAR_FILE_SUFFIX;

    public static final String PIGUDFS_JAR = PIGUDFS_JAR_NAME + FileExtensions.JAR_FILE_SUFFIX;

    /*
     * for old build system JobJavaScriptsManager
     */
    public static final String SYSTEM_ROUTINE_JAR = "systemRoutines" + FileExtensions.JAR_FILE_SUFFIX; //$NON-NLS-1$

    public static final String USER_ROUTINE_JAR = "userRoutines" + FileExtensions.JAR_FILE_SUFFIX; //$NON-NLS-1$

    public static final String USER_BEANS_JAR = "userBeans" + FileExtensions.JAR_FILE_SUFFIX; //$NON-NLS-1$

    public static final String USER_PIGUDF_JAR = "pigudf" + FileExtensions.JAR_FILE_SUFFIX; //$NON-NLS-1$

    /**
     * DOC ycbai Get default jvm name.
     *
     * @return
     */
    public static String getDefaultJVMName() {
        IVMInstall install = JavaRuntime.getDefaultVMInstall();
        if (install != null) {
            return install.getName();
        } else {
            return "nothing!";
        }
    }

    /**
     * DOC ycbai Get default javaEE name.
     *
     * @return
     */
    public static String getDefaultEEName() {
        IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();

        IExecutionEnvironment[] environments = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
        if (defaultVM != null) {
            for (IExecutionEnvironment environment : environments) {
                IVMInstall eeDefaultVM = environment.getDefaultVM();
                if (eeDefaultVM != null && defaultVM.getId().equals(eeDefaultVM.getId())) {
                    return environment.getId();
                }
            }
        }

        String defaultCC;
        if (defaultVM instanceof IVMInstall2) {
            defaultCC = getCompilerCompliance((IVMInstall2) defaultVM, JavaCore.VERSION_1_4);
        } else {
            defaultCC = JavaCore.VERSION_1_4;
        }

        for (IExecutionEnvironment environment : environments) {
            String eeCompliance = getExecutionEnvironmentCompliance(environment);
            if (defaultCC.endsWith(eeCompliance)) {
                return environment.getId();
            }
        }

        return "J2SE-1.6"; //$NON-NLS-1$
    }

    /**
     * DOC ycbai Get compiler compliance via vminstall and default compliance.
     *
     * @param vMInstall
     * @param defaultCompliance
     * @return
     */
    public static String getCompilerCompliance(IVMInstall2 vMInstall, String defaultCompliance) {
        String version = vMInstall.getJavaVersion();
        return getJavaVersion(defaultCompliance, version);
    }

    public static String getProjectJavaVersion() {
        String javaVersion = CoreRuntimePlugin.getInstance().getProjectPreferenceManager().getValue(PROJECT_JAVA_VERSION_KEY);
        if (javaVersion != null && javaVersion.trim().equals("")) { //$NON-NLS-1$
            javaVersion = null;
        }
        return javaVersion;
    }

    public static void updateProjectJavaVersion(String javaVersion) {
        setProjectJavaVserion(javaVersion);
        applyCompilerCompliance(javaVersion);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
            service.updateProjectPomWithTemplate();
        }
    }

    private static void setProjectJavaVserion(String javaVersion) {
        if (javaVersion != null) {
            ProjectPreferenceManager manager = CoreRuntimePlugin.getInstance().getProjectPreferenceManager();
            manager.setValue(PROJECT_JAVA_VERSION_KEY, javaVersion);
            manager.save();
        }
    }

    private static void applyCompilerCompliance(String compliance) {
        if (compliance != null) {
            IEclipsePreferences eclipsePreferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
            Map<String, String> complianceOptions = new HashMap<String, String>();
            JavaCore.setComplianceOptions(compliance, complianceOptions);
            if (!complianceOptions.isEmpty()) {
                Set<Entry<String, String>> entrySet = complianceOptions.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    eclipsePreferences.put(entry.getKey(), entry.getValue());
                }
            }
            try {
                eclipsePreferences.flush();
            } catch (BackingStoreException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private static String getJavaVersion(String defaultCompliance, String version) {
        if (version == null) {
            return defaultCompliance;
        }

        JavaVersion ver = new JavaVersion(version);
        if (ver.getMajor() > 8) {
            return String.valueOf(ver.getMajor());
        }
        if (version.startsWith(JavaCore.VERSION_1_8)) {
            return JavaCore.VERSION_1_8;
        }
        if (version.startsWith(JavaCore.VERSION_1_7)) {
            return JavaCore.VERSION_1_7;
        }
        if (version.startsWith(JavaCore.VERSION_1_6)) {
            return JavaCore.VERSION_1_6;
        }
        if (version.startsWith(JavaCore.VERSION_1_5)) {
            return JavaCore.VERSION_1_5;
        }
        if (version.startsWith(JavaCore.VERSION_1_4)) {
            return JavaCore.VERSION_1_4;
        }
        if (version.startsWith(JavaCore.VERSION_1_3)) {
            return JavaCore.VERSION_1_3;
        }
        if (version.startsWith(JavaCore.VERSION_1_2)) {
            return JavaCore.VERSION_1_3;
        }
        if (version.startsWith(JavaCore.VERSION_1_1)) {
            return JavaCore.VERSION_1_3;
        }
        return defaultCompliance;
    }

    /**
     * DOC ycbai Get execution environment compliance via execution environment.
     *
     * @param executionEnvironment
     * @return
     */
    public static String getExecutionEnvironmentCompliance(IExecutionEnvironment executionEnvironment) {
        @SuppressWarnings("rawtypes")
        Map complianceOptions = executionEnvironment.getComplianceOptions();
        if (complianceOptions != null) {
            Object compliance = complianceOptions.get(JavaCore.COMPILER_COMPLIANCE);
            if (compliance instanceof String) {
                return (String) compliance;
            }
        }

        String desc = executionEnvironment.getId();
        if (desc.indexOf(JavaCore.VERSION_1_6) != -1) {
            return JavaCore.VERSION_1_6;
        } else if (desc.indexOf(JavaCore.VERSION_1_6) != -1) {
            return JavaCore.VERSION_1_6;
        } else if (desc.indexOf(JavaCore.VERSION_1_5) != -1) {
            return JavaCore.VERSION_1_5;
        } else if (desc.indexOf(JavaCore.VERSION_1_4) != -1) {
            return JavaCore.VERSION_1_4;
        }
        return JavaCore.VERSION_1_3;
    }

    /**
     * DOC ycbai Add the java nature to project.
     *
     * @param project
     * @param monitor
     * @throws CoreException
     */
    public static void addJavaNature(IProject project, IProgressMonitor monitor) throws CoreException {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        if (project != null && !project.hasNature(JavaCore.NATURE_ID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = JavaCore.NATURE_ID;
            description.setNatureIds(newNatures);
            project.setDescription(description, monitor);
        }
        if (monitor != null) {
            monitor.worked(1);
        }
    }
    
    private static String getComplianceLevel() {
        if (isComplianceLevelSet()) {
            return System.getProperty(SYS_PROP_JAVA_COMPLIANCE_LEVEL, JavaCore.VERSION_1_8);
        }
        return JavaCore.VERSION_1_8;
    }
    
    
    /**
     * When allow java internal access, need to set compliance to java 11, if current complier's version>=11, otherwise
     * set to Java 8.
     * 
     * @return
     */
    public static String getCompatibleComplianceLevel() {
        String ver = getDefaultComplianceLevel();
        if (VersionUtils.compareTo(ver, JavaCore.VERSION_11) < 0) {
            ver = JavaCore.VERSION_1_8;
        }
        return JavaCore.VERSION_11;
    }
    
    private static String getDefaultComplianceLevel() {
        return getCompilerCompliance((IVMInstall2) JavaRuntime.getDefaultVMInstall(), JavaCore.VERSION_1_8);
    }

    public static boolean isComplianceLevelSet() {
        boolean isSystemPropSet = System.getProperty(SYS_PROP_JAVA_COMPLIANCE_LEVEL) == null ? false : true;
        if (!isSystemPropSet) {
            return isSystemPropSet;
        }
        String complianceLevel = System.getProperty(SYS_PROP_JAVA_COMPLIANCE_LEVEL);
        String complierComplianceLevel = getDefaultComplianceLevel();
        if (!StringUtils.equals(complianceLevel, complierComplianceLevel)) {
            ExceptionHandler
                    .log("Not compatible, complianceLevel set by system property: " + complianceLevel
                            + ", jvm's complierComplianceLevel: " + complierComplianceLevel);
            return false;
        }
        
        if (!isAllowInternalAccess()) {
            // set
            getJavaVersionProjectSettingPrefStore().setValue(ALLOW_JAVA_INTERNAL_ACCESS, true);
        }
        ExceptionHandler
                .log("complianceLevel set by system property: " + complianceLevel + ", complierComplianceLevel: "
                        + complierComplianceLevel);
        return isSystemPropSet;
    }

    public static boolean isAllowInternalAccess() {
        return getJavaVersionProjectSettingPrefStore().getBoolean(ALLOW_JAVA_INTERNAL_ACCESS);
    }

    private static IPreferenceStore getJavaVersionProjectSettingPrefStore() {
        ProjectPreferenceManager projectPreferenceManager = new ProjectPreferenceManager(
                ProjectManager.getInstance().getCurrentProject(), CoreRuntimePlugin.PLUGIN_ID, false);
        // set the project preference
        return projectPreferenceManager.getPreferenceStore();
    }
    
    public static void validateJavaVersion() {
        try {
            // validate jvm which is used to start studio
            StudioKeysFileCheck.validateJavaVersion();

            // added for master to avoid junit failure
            if (CommonsPlugin.isHeadless() || CommonsPlugin.isJUnitTest() || PluginChecker.isSWTBotLoaded()
                    || CommonsPlugin.isTUJTest() || CommonsPlugin.isJunitWorking()) {
                return;
            }
            // validate default complier's compliance level
            IVMInstall install = JavaRuntime.getDefaultVMInstall();
            String ver = getCompilerCompliance((IVMInstall2) install, JavaCore.VERSION_1_8);
            if (new JavaVersion(ver).compareTo(new JavaVersion(StudioKeysFileCheck.JAVA_VERSION_MAXIMUM_STRING)) > 0) {
                VersionException e = new VersionException(VersionException.ERR_JAVA_VERSION_NOT_SUPPORTED,
                        "The maximum Java version supported by Studio is " + StudioKeysFileCheck.JAVA_VERSION_MAXIMUM_STRING + ". Your compiler's compliance level is " + ver);
                throw e;
            }
        } catch (Exception e1) {
            if (e1 instanceof VersionException) {
                throw e1;
            }
            ExceptionHandler.process(e1);
        }
    }

}
