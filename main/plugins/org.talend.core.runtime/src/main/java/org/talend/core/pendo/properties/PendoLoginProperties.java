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
package org.talend.core.pendo.properties;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoLoginProperties implements IPendoDataProperties {

    @JsonProperty("studio_version")
    private String studioVersion;

    @JsonProperty("studio_patch")
    private String studioPatch;

    @JsonProperty("enabled_features")
    private List<String> enabledFeatures;

    @JsonProperty("referenced_project_number")
    private String refProjectCount;

    @JsonProperty("referenced_projects")
    private List<String> refProjectList;

    @JsonProperty("one_click_login")
    private String isOneClickLogin;

    @JsonProperty("managed_update")
    private String managedUpdate;

    /**
     * Getter for studio_version.
     * 
     * @return the studio_version
     */
    public String getStudioVersion() {
        return studioVersion;
    }

    /**
     * Sets the studio_version.
     * 
     * @param studio_version the studio_version to set
     */
    public void setStudioVersion(String studioVersion) {
        this.studioVersion = studioVersion;
    }

    /**
     * Getter for studio_patch.
     * 
     * @return the studio_patch
     */
    public String getStudioPatch() {
        return studioPatch;
    }

    /**
     * Sets the studio_patch.
     * 
     * @param studio_patch the studio_patch to set
     */
    public void setStudioPatch(String studioPatch) {
        this.studioPatch = studioPatch;
    }

    /**
     * Getter for enabled_features.
     * 
     * @return the enabled_features
     */
    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    /**
     * Sets the enabled_features.
     * 
     * @param enabled_features the enabled_features to set
     */
    public void setEnabledFeatures(List<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    /**
     * Getter for refProjectCount.
     * 
     * @return the refProjectCount
     */
    public String getRefProjectCount() {
        return refProjectCount;
    }

    /**
     * Sets the refProjectCount.
     * 
     * @param refProjectCount the refProjectCount to set
     */
    public void setRefProjectCount(String refProjectCount) {
        this.refProjectCount = refProjectCount;
    }

    /**
     * Getter for refProjectList.
     * 
     * @return the refProjectList
     */
    public List<String> getRefProjectList() {
        return refProjectList;
    }

    /**
     * Sets the refProjectList.
     * 
     * @param refProjectList the refProjectList to set
     */
    public void setRefProjectList(List<String> refProjectList) {
        this.refProjectList = refProjectList;
    }

    /**
     * Getter for isOneClickLogin.
     * 
     * @return the isOneClickLogin
     */
    public String getIsOneClickLogin() {
        return isOneClickLogin;
    }

    /**
     * Sets the isOneClickLogin.
     * 
     * @param isOneClickLogin the isOneClickLogin to set
     */
    public void setIsOneClickLogin(String isOneClickLogin) {
        this.isOneClickLogin = isOneClickLogin;
    }

    /**
     * Getter for managedUpdate.
     * 
     * @return the managedUpdate
     */
    public String getManagedUpdate() {
        return managedUpdate;
    }

    /**
     * Sets the managedUpdate.
     * 
     * @param managedUpdate the managedUpdate to set
     */
    public void setManagedUpdate(String managedUpdate) {
        this.managedUpdate = managedUpdate;
    }

}
