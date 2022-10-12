// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.pendo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.pendo.properties.IPendoDataProperties;
import org.talend.core.pendo.properties.PendoLoginProperties;
import org.talend.core.service.ICloudSignOnService;
import org.talend.core.service.IStudioLiteP2Service;
import org.talend.core.service.IStudioLiteP2Service.UpdateSiteConfig;
import org.talend.core.ui.IInstalledPatchService;
import org.talend.repository.ProjectManager;
import org.talend.utils.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoTrackDataUtil {

    private static final String FEATURE_PREFIX = "org.talend.lite.";

    private static final String FEATURE_TAIL = ".feature.feature.group";

    public static String generateTrackData(String pendoInfo, TrackEvent event, IPendoDataProperties properties) throws Exception {
        JSONObject infoJson = new JSONObject(pendoInfo);
        String visitorId = ((JSONObject) infoJson.get("visitor")).getString("id");
        String accountId = ((JSONObject) infoJson.get("account")).getString("id");

        PendoEventEntity entity = new PendoEventEntity();
        entity.setType("track");
        entity.setEvent(event.getEvent());
        entity.setVisitorId(visitorId);
        entity.setAccountId(accountId);
        entity.setTimestamp(new Date().getTime());
        entity.setProperties(properties);

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(entity);
        return content;
    }

    public static IPendoDataProperties getLoginEventProperties() {
        String studioPatch = getLatestPatchInstalledVersion();
        PendoLoginProperties loginEvent = new PendoLoginProperties();
        IStudioLiteP2Service studioLiteP2Service = IStudioLiteP2Service.get();
        try {
            if (studioLiteP2Service != null) {
                List<String> enabledFeatures = new ArrayList<String>();
                List<String> enabledFeaturesList = studioLiteP2Service.getCurrentProjectEnabledFeatures();
                enabledFeaturesList.stream().forEach(feature -> {
                    String result = feature;
                    if (result.startsWith(FEATURE_PREFIX)) {
                        result = result.substring(FEATURE_PREFIX.toCharArray().length);
                    }
                    if (result.endsWith(FEATURE_TAIL)) {
                        result = result.substring(0, result.lastIndexOf(FEATURE_TAIL));
                    }
                    enabledFeatures.add(result);
                });
                loginEvent.setEnabledFeatures(enabledFeatures);
            }
            setUpRefProjectsStructure(loginEvent);
            loginEvent.setIsOneClickLogin(Boolean.FALSE.toString());
            if (ICloudSignOnService.get() != null && ICloudSignOnService.get().isSignViaCloud()) {
                loginEvent.setIsOneClickLogin(Boolean.TRUE.toString());
            }
            loginEvent.setManagedUpdate(Boolean.FALSE.toString());
            if (IStudioLiteP2Service.get() != null) {
                IProgressMonitor monitor = new NullProgressMonitor();
                UpdateSiteConfig config = IStudioLiteP2Service.get().getUpdateSiteConfig(monitor);
                if (config.isEnableTmcUpdateSettings(monitor) && !config.isOverwriteTmcUpdateSettings(monitor)) {
                    loginEvent.setManagedUpdate(Boolean.TRUE.toString());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        loginEvent.setStudioVersion(VersionUtils.getInternalMajorVersion());
        loginEvent.setStudioPatch(studioPatch);
        return loginEvent;
    }

    public static String getLatestPatchInstalledVersion() {
        String studioPatch = "";
        IInstalledPatchService installedPatchService = IInstalledPatchService.get();
        if (installedPatchService != null) {
            studioPatch = installedPatchService.getLatestInstalledVersion(true);
        }
        return studioPatch;
    }

    private static void setUpRefProjectsStructure(PendoLoginProperties loginEvent) {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project currentProject = projectManager.getCurrentProject();
        Map<String, Project> teclabelProjectMap = new HashMap<String, Project>();
        List<Project> allReferencedProjects = projectManager.getAllReferencedProjects();
        allReferencedProjects.forEach(refProject -> {
            String technicalLabel = refProject.getTechnicalLabel();
            if (StringUtils.isNotBlank(technicalLabel)) {
                teclabelProjectMap.put(technicalLabel, refProject);
            }
        });

        int[] refCount = new int[] {0};
        List<String> resultList = new ArrayList<String>();
        Map<String, String> desensitiveLabelMap = new HashMap<String, String>();
        findReferencePorjectPath(currentProject, "Main", resultList, refCount, desensitiveLabelMap, teclabelProjectMap);
        loginEvent.setRefProjectList(resultList);
        loginEvent.setRefProjectCount(String.valueOf(desensitiveLabelMap.keySet().size()));
    }

    public static void findReferencePorjectPath(Project currentProject, String path, List<String> resultList, int[] refCount,
            Map<String, String> desensitiveLabelMap, Map<String, Project> teclabelProjectMap) {
        List<ProjectReference> projectReferenceList = currentProject.getProjectReferenceList();
        for (ProjectReference projectReference : projectReferenceList) {
            String structPath = path;
            if (projectReference.getReferencedProject() == null) {
                continue;
            }
            String technicalLabel = projectReference.getReferencedProject().getTechnicalLabel();
            Project refProject = teclabelProjectMap.get(technicalLabel);
            if (StringUtils.isBlank(technicalLabel) || refProject == null) {
                continue;
            }
            String desensitiveLabel = desensitiveLabelMap.get(technicalLabel);
            if (StringUtils.isBlank(desensitiveLabel)) {
                refCount[0] = refCount[0] + 1;
                desensitiveLabel = "Ref" + refCount[0];
                desensitiveLabelMap.put(technicalLabel, desensitiveLabel);
            }
            structPath = structPath + "/" + desensitiveLabel;
            resultList.add(structPath);
            findReferencePorjectPath(refProject, structPath, resultList, refCount, desensitiveLabelMap, teclabelProjectMap);
        }

    }

    public static String convertEntityJsonString(Object entity) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String content = mapper.writeValueAsString(entity);
            if (StringUtils.isNotBlank(content)) {
                return content;
            }
        } catch (JsonProcessingException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    public enum TrackEvent {

        PROJECT_LOGIN("Project Login"),
        IMPORT_API_DEF("Import API Definition"),
        UPDATE_API_DEF("Update API Definition"),
        USE_API_DEF("Use API Definition"),
        OPEN_IN_APIDesigner("Open in API Designer"),
        OPEN_IN_APITester("Open in API Tester"),
        OPEN_API_DOCUMENTATION("Open API Documentation"),
        AUTOMAP("tMap Automap"),
        TMAP("tMap"),
        ITEM_IMPORT("Import items"),
        ITEM_SIGNATURE("Item Signature");

        private String event;

        TrackEvent(String event) {
            this.event = event;
        }

        public String getEvent() {
            return event;
        }

    }

}
